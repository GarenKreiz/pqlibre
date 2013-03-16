package org.gpssearch;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geoscrape.Cache;
import org.geoscrape.CacheLog;
import org.geoscrape.Cacher;
import org.geoscrape.ListSearcher;
import org.geoscrape.LogType;
import org.geoscrape.Login;

/**
 * Progress bar monitor for downloading "my caches".
 */
public class MyCachesProgress extends Progress
{
	private String targetUser;
	private long lowestFindTime;

	/**
	 * @param search
	 * @param login
	 * @param man
	 */
	public MyCachesProgress(ListSearcher search, Login login, UserIdManager man, Properties props)
	{
		super(search, login, man, props);
		// check if we're doing a search for my caches or the caches of another
		// user
		if (Boolean.parseBoolean(properties.getProperty("btnMine")))
		{
			this.targetUser = login.getUserName();
		}
		else
		{
			this.targetUser = properties.getProperty("textOtherUsername");
		}
		if (Boolean.parseBoolean(properties.getProperty("btnLast")))
		{
			// only the last caches
			maxFind = Integer.parseInt(properties.getProperty("cacheCount"));
		}
		else if (Boolean.parseBoolean(properties.getProperty("btnFoundAfter")))
		{
			try
			{
				SimpleDateFormat df = new SimpleDateFormat(login.getDateFormat());
				lowestFindTime = df.parse(properties.getProperty("textFoundSince")).getTime();
			}
			catch (ParseException e)
			{
				lowestFindTime = 0;
				e.printStackTrace();
			}

		}
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
			// check if "our" log was found
			while (!hasLog(cache, this.targetUser))
			{
				// log not found, load more logs
				boolean more;
				try
				{
					more = cache.retrieveMoreLogs(login);
					if (!more)
					{
						// no more logs available
						break;
					}
				}
				catch (Exception e)
				{
					// TODO: Notify user
					e.printStackTrace();
				}
			}
			if (lowestFindTime > 0)
			{
				// find the log of this user
				for (CacheLog log : cache.getLogs())
				{
					if (log.getLoggedBy().getName().equals(targetUser))
					{
						if (log.getLogType().equals(LogType.FOUND_IT)
								|| log.getLogType().equals(LogType.WEBCAM_PHOTO_TAKEN))
						{
							if (log.getLogTime() < this.lowestFindTime)
							{
								// the cache log is too old, ignore rest
								monitor.setCanceled(true);
								return false;
							}
						}
					}
				}
			}
			return true;
		}
		else
		{
			return false;
		}
	}

	private boolean hasLog(Cache c, String targetCacher)
	{
		for (CacheLog log : c.getLogs())
		{
			if (log.getLoggedBy().getName().equals(targetCacher))
			{
				if (log.getLogType().equals(LogType.FOUND_IT) || log.getLogType().equals(LogType.WEBCAM_PHOTO_TAKEN))
				{
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @see org.gpssearch.Progress#checkPreDownload(org.geoscrape.Cache)
	 */
	@Override
	protected boolean checkPreDownload(Cache cache)
	{
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
			searcher.findCachesFoundBy(new Cacher(this.targetUser), this.maxFind);
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
