package org.gpssearch;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Shell;
import org.geoscrape.Cache;
import org.geoscrape.Cacher;
import org.geoscrape.ListSearcher;
import org.geoscrape.Login;

/**
 * Progress bar monitor for downloading "my hides".
 */
public class MyHidesProgress extends Progress
{
	private String targetUser;
	private long lowestFindTime;
	private boolean fullLogs;

	/**
	 * @param search
	 * @param login
	 * @param man
	 */
	public MyHidesProgress(ListSearcher search, Login login, UserIdManager man, Properties props,Shell shell)
	{
		super(search, login, man, props,shell);
		// check if we're doing a search for my caches or the caches of another
		// user
		if (Boolean.parseBoolean(properties.getProperty("btnMineHides")))
		{
			this.targetUser = login.getUserName();
		}
		else
		{
			this.targetUser = properties.getProperty("textOtherUsernameHides");
		}
		if (Boolean.parseBoolean(properties.getProperty("btnLastHides")))
		{
			// only the last caches
			maxFind = Integer.parseInt(properties.getProperty("cacheCountHides"));
		}
		else if (Boolean.parseBoolean(properties.getProperty("btnFoundAfterHides")))
		{
			try
			{
				SimpleDateFormat df = new SimpleDateFormat(login.getDateFormat());
				lowestFindTime = df.parse(properties.getProperty("textFoundSinceHides")).getTime();
			}
			catch (ParseException e)
			{
				lowestFindTime = 0;
				e.printStackTrace();
			}
		}
		fullLogs = Boolean.parseBoolean(properties.getProperty("btnFullLogHides","false"));
	}

	/**
	 * @see org.gpssearch.Progress#checkPostDownload(org.geoscrape.Cache)
	 */
	@Override
	protected boolean checkPostDownload(Cache cache)
	{
		// make sure the logs contain the target user's log
		if (!cache.isUnavailableToUs())
		{
			// load the full logs if the user asked for it and there are more to be found
			try
			{
				while (fullLogs && cache.retrieveMoreLogs(login))
				{
				}
			}
			catch (Exception e)
			{
				// TODO Notify user with popup
				e.printStackTrace();
			}
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * @see org.gpssearch.Progress#checkPreDownload(org.geoscrape.Cache)
	 */
	@Override
	protected boolean checkPreDownload(Cache cache)
	{
		if (lowestFindTime > 0)
		{
			if(cache.getLastFoundDate()<=lowestFindTime)
			{
				//cache hasn't been found since before lowestFindTime
				return false;
			}
		}
		return true;
	}

	/**
	 * @see org.gpssearch.Progress#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
	{
		this.monitor = monitor;
		monitor.beginTask("Preparing search...", 100);
		try
		{
			searcher.findCachesOwnedBy(new Cacher(this.targetUser),this.maxFind);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
		monitor.done();
	}
}
