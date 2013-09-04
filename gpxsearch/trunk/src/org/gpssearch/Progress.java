package org.gpssearch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.geoscrape.Cache;
import org.geoscrape.CacheLog;
import org.geoscrape.ListSearcher;
import org.geoscrape.Login;
import org.geoscrape.SearchCallback;
import org.gpssearch.gui.ParsingErrorDialog;

/**
 * Keep track of the search progress, display info to the user and filter
 * incoming caches according to the search parameters.
 * 
 */
public abstract class Progress implements IRunnableWithProgress, SearchCallback
{
	protected Integer totalCaches = null;
	protected int found = 0;
	protected int maxFind = 0;
	protected ListSearcher searcher;
	protected Login login;
	protected IProgressMonitor monitor;
	protected int lastPercentage = 0;
	protected UserIdManager idManager;
	protected Properties properties;
	protected boolean ignoreErrors = false;
	protected int count = 0;
	private ObjectOutputStream outputStream;
	
	private Shell appShell;

	// format options
	protected boolean includeLogs;


	public Progress(ListSearcher search, File outputFile, Login login, UserIdManager man, Properties props, Shell appShell) 
	{
		this.searcher = search;
		this.login = login;
		this.idManager = man;
		this.searcher.registerSearchCallback(this);
		this.properties = props;
		this.appShell = appShell;
		try
		{
			outputStream = new ObjectOutputStream(new FileOutputStream(outputFile));
		}
		catch (Exception e)
		{
			//TODO: Figure out what the hell we do now
			e.printStackTrace();
		}
	}


	/**
	 * @see org.geoscrape.SearchCallback#found(org.geoscrape.Cache)
	 */
	@Override
	public void found(final Cache cache)
	{
		if (this.monitor.isCanceled())
		{
			searcher.abort();
		}
		else
		{
			found++;
			// calculate percentage, if we can
			if (totalCaches != null)
			{
				int countSoFar = found;
				if(maxFind!=0)
				{
					countSoFar = count+1;
				}
				int percentage = (int) Math.round(100.0 * countSoFar / totalCaches);
				int diff = percentage - lastPercentage;
				if (diff > 0)
				{
					monitor.worked(diff);
					lastPercentage = percentage;
				}
				monitor.setTaskName("Checking cache " + countSoFar + "/" + totalCaches);
			}
			// check if it fits pre-download criteria
			if (checkPreDownload(cache))
			{
				try
				{
					// if so, download cache details
					monitor.subTask("Downloading " + cache.getCacheCode());
					try
					{
						cache.populate(login, false);
						// check if we fulfil post-download criteria
						if (checkPostDownload(cache))
						{
							// if so, save cache to list
							outputStream.writeObject(cache);
							//increment counter
							count++;
							// put all id logs in cache
							for (CacheLog log : cache.getLogs())
							{
								if (log.getLoggedBy().getId() != null)
								{
									idManager.setId(log.getLoggedBy(), log.getLoggedBy().getId());
								}
							}

							if (maxFind > 0 && count >= maxFind)
							{
								searcher.abort();
							}
						}
					}
					catch (Exception e)
					{
						System.out.println("Problem with cache " + cache.getCacheCode());
						e.printStackTrace();
						if (!ignoreErrors)
						{
							Display.getDefault().syncExec(new Runnable()
							{
								public void run()
								{
									ParsingErrorDialog ped = new ParsingErrorDialog(appShell, cache.getCacheCode());
									ped.open();
									if (ped.isDoCancel())
									{
										// user wants to terminate search
										searcher.abort();
									}
									else if (ped.isDoContinue())
									{
										// user don't want to cancel
										ignoreErrors = ped.isDoIgnore();
									}
								}
							});
						}
					}
					monitor.subTask("Found " + count + " matching caches.");
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}


	/**
	 * @param cache
	 * @return
	 */
	protected abstract boolean checkPostDownload(Cache cache);


	/**
	 * @param cache
	 * @return
	 */
	protected abstract boolean checkPreDownload(Cache cache);
	
	/**
	 * Close the object output stream.
	 */
	public void closeFile()
	{
		try
		{
			this.outputStream.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}


	/**
	 * @see org.geoscrape.SearchCallback#totalNumber(int)
	 */
	@Override
	public void totalNumber(int n)
	{
		if(maxFind == 0)
		{
			totalCaches = n;
		}
		else
		{
			totalCaches = Math.min(maxFind, n);
			if(n < maxFind)
			{
				maxFind = 0;
			}
		}
		monitor.setTaskName("Populating caches...");
	}

	/**
	 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public abstract void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException;
	



	/**
	 * @return
	 */
	public int getCount()
	{
		return count;
	}

}
