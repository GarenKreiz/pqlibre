package org.gpssearch.gui;

import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.geoscrape.Cache;
import org.geoscrape.CacheType;
import org.geoscrape.Location;
import org.geoscrape.util.HtmlParser;
import org.geoscrape.util.UserAgentFaker;
import org.geoscrape.util.WebClient;
import org.gpssearch.GpxWriter;
import org.gpssearch.SearcherProgress;
import org.gpssearch.UserIdManager;

/**
 * Displays the result of a search operation and presents the user with options
 * of what to do.
 * 
 */
public class ResultDialog extends Dialog
{

	protected Object result;
	protected Shell shlSearchResults;
	private Button btnSaveFile;
	private Button btnOpenInBrowser;
	private Label resultText;
	private List<Cache> caches;
	private UserIdManager idManager;
	private Desktop desktop;
	private Button btnDisplayMap;

	private String ourname;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public ResultDialog(Shell parent, int style)
	{
		super(parent, style);
		setText("SWT Dialog");
	}

	/**
	 * Open the dialog.
	 * 
	 * @return the result
	 */
	public Object open(List<Cache> caches, UserIdManager idManager, String ourname)
	{
		this.ourname = ourname;
		this.caches = caches;
		this.idManager = idManager;
		createContents();

		if (Desktop.isDesktopSupported())
		{
			desktop = Desktop.getDesktop();
		}
		if (caches.size() > 0)
		{
			btnSaveFile.setEnabled(true);
			if (desktop != null)
			{
				if (btnOpenInBrowser != null)
				{
					btnOpenInBrowser.setEnabled(true);
				}
				if (btnDisplayMap != null)
				{
					btnDisplayMap.setEnabled(true);
				}
			}
			if (caches.size() == 1)
			{
				resultText.setText("Found 1 cache.");
			}
			else
			{
				resultText.setText("Found " + caches.size() + " caches.");
			}
		}
		shlSearchResults.open();
		shlSearchResults.layout();
		Display display = getParent().getDisplay();
		while (!shlSearchResults.isDisposed())
		{
			if (!display.readAndDispatch())
			{
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents()
	{
		shlSearchResults = new Shell(getParent(), SWT.DIALOG_TRIM);
		shlSearchResults.setSize(450, 236);
		shlSearchResults.setText("Search results");

		Button btnDone = new Button(shlSearchResults, SWT.NONE);
		btnDone.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				shlSearchResults.close();
			}
		});
		btnDone.setGrayed(false);
		btnDone.setBounds(354, 174, 81, 24);
		btnDone.setText("Done");

		btnSaveFile = new Button(shlSearchResults, SWT.NONE);
		btnSaveFile.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{

				// Generate GPX, ask user to save it

				FileDialog fileDialog = new FileDialog(shlSearchResults, SWT.SAVE | SWT.DIALOG_TRIM);
				fileDialog.setText("Save .gpx file");
				fileDialog.setOverwrite(true);
				String[] filterExt = { "*.gpx" };
				fileDialog.setFilterExtensions(filterExt);
				String outputName = fileDialog.open();
				if (outputName != null)
				{
					if (!outputName.toLowerCase().endsWith(".gpx"))
					{
						outputName += ".gpx";
					}
					try
					{
						GpxWriter writer = new GpxWriter(caches, idManager,outputName);
						ProgressMonitorDialog progdialog = new ProgressMonitorDialog(getParent());
						progdialog.run(true, true, writer);
					}
					catch (InvocationTargetException e1)
					{
						e1.printStackTrace();
					}
					catch (InterruptedException e1)
					{
						e1.printStackTrace();
					}
				}
				idManager.saveDb();
			}
		});
		btnSaveFile.setEnabled(false);
		btnSaveFile.setBounds(264, 174, 81, 24);
		btnSaveFile.setText("Save file");

		btnOpenInBrowser = new Button(shlSearchResults, SWT.NONE);
		btnOpenInBrowser.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				boolean open = true;
				if (caches.size() > 10)
				{
					MessageBox messageBox = new MessageBox(shlSearchResults, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
					messageBox.setMessage("You have asked to open "
							+ caches.size()
							+ " caches.\nSuch a large number of open windows or tabs can cause your computer to slow down or even crash your browser.\nDo you still want do do it?");
					messageBox.setText("Opening a large number of caches.");
					int response = messageBox.open();
					if (response == SWT.NO)
					{
						open = false;
					}
				}
				if (open)
				{
					for (Cache c : caches)
					{
						try
						{
							desktop.browse(new URI("http://coord.info/" + c.getCacheCode()));
						}
						catch (Exception e1)
						{
							e1.printStackTrace();
						}
					}
				}
			}
		});
		btnOpenInBrowser.setEnabled(false);
		btnOpenInBrowser.setBounds(145, 174, 113, 24);
		btnOpenInBrowser.setText("Open in browser");

		resultText = new Label(shlSearchResults, SWT.NONE);
		resultText.setBounds(42, 45, 393, 15);
		resultText.setText("No caches found.");

		btnDisplayMap = new Button(shlSearchResults, SWT.NONE);
		btnDisplayMap.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				displayMap();
			}
		});
		btnDisplayMap.setEnabled(false);
		btnDisplayMap.setBounds(38, 174, 101, 24);
		btnDisplayMap.setText("Show on map");

	}

	/**
	 * Show all the caches in the display map.
	 */
	protected void displayMap()
	{
		try
		{
			String newline = "\n";
			StringBuilder url = new StringBuilder("name,desc,lat,lon,icon_size,sym");
			for (Cache c : caches)
			{
				StringBuilder tmp = new StringBuilder(newline);
				tmp.append(escape(c.getName()));
				tmp.append(",");
				StringBuilder desc = new StringBuilder();
				desc.append("<a href=\"http://coord.info/");
				desc.append(c.getCacheCode());
				desc.append("\" target=\"_blank\">");
				desc.append(c.getCacheCode());
				desc.append("</a><br>");
				desc.append(c.getCacheType());
				desc.append("<br>D/T: ");
				desc.append(c.getDifficultyRating());
				desc.append("/");
				desc.append(c.getTerrainRating());
				desc.append("<br>Size: ");
				desc.append(c.getCacheSize().toString());
				tmp.append(escape(desc.toString()));
				tmp.append(",");
				Location loc = c.getLocation();
				tmp.append(shortify(loc.getLatitude().toDecimalString()));
				tmp.append(",");
				tmp.append(shortify(loc.getLongitude().toDecimalString()));
				tmp.append(",16x16,");
				tmp.append(getIconUrlString(c));

				url.append(tmp);
			}
			// compress the string
			ByteArrayOutputStream data = new ByteArrayOutputStream();
			ZipOutputStream out = new ZipOutputStream(data);
			out.setLevel(9);
			out.putNextEntry(new ZipEntry("file.csv"));
			byte[] tmp = url.toString().getBytes("UTF-8");
			out.write(tmp);
			out.closeEntry();
			out.close();

			// submit the string to the form
			WebClient wc = new WebClient();
			wc.setRequestMethod("POST");
			wc.setUserAgent(UserAgentFaker.getRandomUserAgent());
			Map<String, String> params = new HashMap<String, String>();
			params.put("format", "google");
			params.put("convert_format", "");
			params.put("form", "google");
			Map<String, byte[]> files = new HashMap<String, byte[]>();
			files.put("uploaded_file_1@tmp.zip", data.toByteArray());

			wc.submitForm("http://www.gpsvisualizer.com/map?output_home", params, files);

			// parse the result, get the address
			String address = wc.getContentsAsString();
			address = HtmlParser.getContent("<div id=\"header_ad\">", address);
			address = HtmlParser.getContent("<a href=\"", "\">", address);
			address = "http://www.gpsvisualizer.com" + address;

			desktop.browse(new URI(address));

		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
	}

	/**
	 * Round decimal number to five digits after decimal point.
	 * 
	 * 
	 * @param decimalString
	 * @return
	 */
	private Object shortify(String decimalString)
	{
		String res = decimalString;
		int pointIndex = res.indexOf(".");
		if (pointIndex >= 0)
		{
			int digits = res.length() - pointIndex - 1;
			if (digits > 5)
			{
				// get the last six digits
				String sub = res.substring(0, pointIndex + 7);
				// round
				double d = Double.parseDouble(sub);
				d = Math.round(d * 100000.0) / 100000.0;
				// convert to string
				res = Double.toString(d);
				// get string to last 5 digits
				pointIndex = res.indexOf(".");
				if (pointIndex >= 0)
				{
					digits = res.length() - pointIndex - 1;
					if (digits > 5)
					{
						res = res.substring(0, pointIndex + 6);
					}
				}
			}
		}
		return res;
	}

	/**
	 * @param cacheType
	 * @return
	 */
	private String getIconUrlString(Cache c)
	{
		StringBuilder res = new StringBuilder("http://pqlibre.googlecode.com/svn/gpxsearch/trunk/img/");
		res.append(getId(c));
		res.append(".png");
		return res.toString();
	}

	/**
	 * @param cacheType
	 * @return
	 */
	private String getId(Cache c)
	{
		String res = "";
		if (c.isFound())
		{
			res = "found";
		}
		else if (c.getHider().getName().equals( ourname))
		{
			res = "own";
		}
		else
		{
			CacheType cacheType = c.getCacheType();
			switch (cacheType)
			{
				case TRADITIONAL:
					res = "trad";
					break;
				case MULTI:
					res = "multi";
					break;
				case VIRTUAL:
					res = "virtual";
					break;
				case LETTERBOX:
					res = "letter";
					break;
				case EVENT:
					res = "event";
					break;
				case MYSTERY:
					res = "myst";
					break;
				case WEBCAM:
					res = "webcam";
					break;
				case CITO:
					res = "cito";
					break;
				case WHERIGO:
					res = "wherigo";
					break;
				case EARTH_CACHE:
					res = "earth";
					break;
				case MEGA_EVENT:
					res = "megaevent";
					break;
				default:
					// use blank as the default
					res = "blank";
					break;
			}

		}
		if (c.isDisabled())
		{
			res += "d";
		}
		return res;
	}

	/**
	 * Format the string for inclusion in gpsvisualiser.com's URL format.
	 * 
	 * @param string
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private Object escape(String string) throws UnsupportedEncodingException
	{
		string = string.replaceAll(",", "");
		return string;
	}
}
