package org.gpssearch.gui;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
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
import org.gpssearch.GpxWriter;
import org.gpssearch.UserIdManager;

/**
 * Displays the result of a search operation and presents the user with options of what to do.
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
			if(desktop!=null)
			{
				btnOpenInBrowser.setEnabled(true);
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
					for(Cache c:caches)
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

	}
}
