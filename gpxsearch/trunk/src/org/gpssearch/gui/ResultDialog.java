package org.gpssearch.gui;

import java.awt.Desktop;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;

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
import org.gpssearch.GpxWriter;
import org.gpssearch.UserIdManager;

/**
 * Displays the result of a search operation and presents the user with options
 * of what to do.
 * 
 */
public class ResultDialog extends Dialog
{
	private static final int URI_SIZE_LIMIT = 8192;

	protected Object result;
	protected Shell shlSearchResults;
	private Button btnSaveFile;
	private Button btnOpenInBrowser;
	private Label resultText;
	private List<Cache> caches;
	private UserIdManager idManager;
	private Desktop desktop;
	private Button btnDisplayMap;

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
	public Object open(List<Cache> caches, UserIdManager idManager)
	{
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
				btnOpenInBrowser.setEnabled(true);
				btnDisplayMap.setEnabled(true);
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
				GpxWriter writer = new GpxWriter(caches, idManager);

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
						writer.write(outputName);
					}
					catch (IOException e1)
					{
						// TODO Notify user if this fails.
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
			String newline = URLEncoder.encode("\n", "UTF-8");
			StringBuilder url = new StringBuilder(
					"http://www.gpsvisualizer.com/map_input?data=name,description,latitude,longitude,icon_size,symbol");
			int count = 0;
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
				tmp.append(loc.getLatitude().toDecimalString());
				tmp.append(",");
				tmp.append(loc.getLongitude().toDecimalString());
				tmp.append(",16x16,");
				tmp.append(getIconUrlString(c.getCacheType()));
				if(url.length()+tmp.length()>=URI_SIZE_LIMIT)
				{
					MessageBox messageBox = new MessageBox(shlSearchResults, SWT.ICON_INFORMATION | SWT.OK );
					messageBox.setMessage("We're sorry, but gpsvisualizer.com can't display that many caches at once.\n" +
							"We'll display the closest "+count+" caches instead.");
					messageBox.setText("Opening a large number of caches.");
					messageBox.open();
					break;
				}
				url.append(tmp);
				count++;
			}
			desktop.browse(new URI(url.toString()));

		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
	}

	/**
	 * @param cacheType
	 * @return
	 */
	private String getIconUrlString(CacheType cacheType)
	{
		StringBuilder res = new StringBuilder("http://www.geocaching.com/images/wpttypes/sm/");
		res.append(getId(cacheType));
		res.append(".gif");
		return res.toString();
	}

	/**
	 * @param cacheType
	 * @return
	 */
	private Object getId(CacheType cacheType)
	{
		String res = "";
		switch (cacheType)
		{
			case TRADITIONAL:
				res = "2";
				break;
			case MULTI:
				res = "3";
				break;
			case VIRTUAL:
				res = "4";
				break;
			case LETTERBOX:
				res = "5";
				break;
			case EVENT:
				res = "6";
				break;
			case MYSTERY:
				res = "8";
				break;
			case PROJECTAPE1:
			case PROJECTAPE2:
				res = "9";
				break;
			case WEBCAM:
				res = "11";
				break;
			case CITO:
				res = "13";
				break;
			case BENCHMARK:
				res = "27";
				break;
			case WHERIGO:
				res = "1858";
				break;
			case EARTH_CACHE:
				res = "earthcache";
				break;
			case LOST_AND_FOUND_EVENT:
				res = "3653";
				break;
			case MEGA_EVENT:
				res="453";
				break;
			case GPSADVENTURE:
				res = "1304";
				break;
			default:
				//use traditional as the default
				res = "2";
				break;

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
		string = URLEncoder.encode(string, "UTF-8");
		return string;
	}
}
