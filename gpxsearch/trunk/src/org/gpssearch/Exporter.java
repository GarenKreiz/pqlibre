package org.gpssearch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.geoscrape.Login;
import org.gpssearch.gui.LoginDialog;
import org.gpssearch.gui.LoginFailedDialog;
import org.gpssearch.gui.MainDialog;

/**
 * Program entry point class.
 * This class is responsible for opening dialog, saving authentication settings.
 * 
 */
public class Exporter
{

	public static final String DT_COMBO_STRING = "^[1-5](\\.5)?/[1-5](\\.5)?$";
	private static final String SETTINGS_FILENAME = ".gpxexporter.settings";
	
	private Login login = null;
	private boolean loggedin = false;

	public Exporter()
	{

	}

	public void doExport()
	{

		try
		{
			Display display = new Display();
			Shell shell = new Shell(display);
			try
			{
				String[] imageNames = new String[] { "icon16.png", "icon32.png", "icon48.png", "icon64.png" };
				Image[] images = new Image[imageNames.length];
				for (int i = 0; i < imageNames.length; i++)
				{
					InputStream is = this.getClass().getResourceAsStream("/" + imageNames[i]);
					if (is == null)
					{
						is = new FileInputStream(imageNames[i]);
					}
					images[i]=new Image(display, is);
				}
				shell.setImages(images);
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			while (!loggedin)
			{
				LoginDialog dialog = new LoginDialog(shell);
				dialog.setUsername(loadUsername());
				dialog.setPassword(loadPassword());
				dialog.setRemember(loadSaveCredentials());

				dialog.open();
				if (dialog.isSaveCredentials())
				{
					saveCredentials(dialog.getUsername(), dialog.getPassword());
				}
				else
				{
					eraseCredentials();
				}
				if (dialog.isLogin())
				{
					ProgressMonitorDialog progdialog = new ProgressMonitorDialog(shell);
					login = new Login(dialog.getUsername(), dialog.getPassword());
					progdialog.run(true, true, new IRunnableWithProgress()
					{

						public void run(IProgressMonitor monitor)
						{
							monitor.beginTask("Logging in...", 100);
							monitor.worked(29);
							try
							{
								login.authenticate();
								monitor.worked(34);
								if (login.isAuthenticated())
								{
									// force update of the date format
									login.getDateFormat();
									loggedin = true;
									
								}
								monitor.done();
							}
							catch (IOException e)
							{
								monitor.done();
							}
						}
					});
					if (!loggedin)
					{
						LoginFailedDialog loginfail = new LoginFailedDialog(shell, 0);
						loginfail.open();
					}
				}
				else
				{
					break;
				}
			}
			if (loggedin)
			{
				MainDialog md = new MainDialog(shell, login);
				md.open();
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void eraseCredentials()
	{
		try
		{
			String fileName = System.getProperty("user.home") + File.separator + SETTINGS_FILENAME;
			FileWriter fw = new FileWriter(fileName, false);
			fw.write("C=false");
			fw.close();
		}
		catch (IOException e)
		{
			// TODO: Pop up error message
		}
	}

	/**
	 * @param username
	 * @param password
	 */
	private void saveCredentials(String username, String password)
	{
		try
		{
			String fileName = System.getProperty("user.home") + File.separator + SETTINGS_FILENAME;
			FileWriter fw = new FileWriter(fileName, false);
			fw.write("U=" + username + "\n");
			fw.write("P=" + password + "\n");
			fw.write("C=true");
			fw.close();
		}
		catch (IOException e)
		{
			// TODO: Pop up error message
		}
	}

	/**
	 * @return
	 */
	private String loadPassword()
	{
		String res = null;
		String fileName = System.getProperty("user.home") + File.separator + SETTINGS_FILENAME;
		File f = new File(fileName);
		try
		{
			if (f.exists())
			{
				BufferedReader br = new BufferedReader(new FileReader(f));
				String line = br.readLine();
				while (line != null)
				{
					if (line.startsWith("P="))
					{
						String[] tmp = line.split("=");
						if (tmp.length > 1)
						{
							res = tmp[1];
						}
					}
					line = br.readLine();
				}
			}
		}
		catch (IOException e)
		{
			// TODO: Pop up error message
		}
		return res;
	}

	/**
	 * @return
	 */
	private String loadUsername()
	{
		String res = null;
		String fileName = System.getProperty("user.home") + File.separator + SETTINGS_FILENAME;
		File f = new File(fileName);
		try
		{
			if (f.exists())
			{
				BufferedReader br = new BufferedReader(new FileReader(f));
				String line = br.readLine();
				while (line != null)
				{
					if (line.startsWith("U="))
					{
						String[] tmp = line.split("=");
						if (tmp.length > 1)
						{
							res = tmp[1];
						}
					}
					line = br.readLine();
				}
			}
		}
		catch (IOException e)
		{
			// TODO: Pop up error message
		}
		return res;
	}

	private boolean loadSaveCredentials()
	{
		boolean res = true;
		String fileName = System.getProperty("user.home") + File.separator + SETTINGS_FILENAME;
		File f = new File(fileName);
		try
		{
			if (f.exists())
			{
				BufferedReader br = new BufferedReader(new FileReader(f));
				String line = br.readLine();
				while (line != null)
				{
					if (line.startsWith("C="))
					{
						String[] tmp = line.split("=");
						if (tmp.length > 1)
						{
							res = Boolean.parseBoolean(tmp[1]);
						}
					}
					line = br.readLine();
				}
			}
		}
		catch (IOException e)
		{
			// TODO: Pop up error message
		}
		return res;
	}


	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		new Exporter().doExport();
	}
}
