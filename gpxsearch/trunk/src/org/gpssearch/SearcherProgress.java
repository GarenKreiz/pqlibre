package org.gpssearch;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geoscrape.Attribute;
import org.geoscrape.Cache;
import org.geoscrape.CacheSize;
import org.geoscrape.CacheType;
import org.geoscrape.ListSearcher;
import org.geoscrape.Location;
import org.geoscrape.Login;

/**
 * Progress bar monitor for general cache search.
 */
public class SearcherProgress extends Progress
{

	// selection criteria
	private boolean checkFavouritePoints;
	private int minFavPoints;
	private boolean checkPlacedOnOrAfter;
	private long afterTime;
	private boolean checkPlacedOnOrBefore;
	private long beforeTime;
	private boolean checkFoundDate;
	private long lastFoundDate;
	private boolean notFound;
	private boolean checkTrackables;
	private boolean checkDisabled;
	private boolean ignoreOwn;
	private boolean ignoreFound;
	private boolean checkDT;
	private List<String> acceptedDTCombos = new ArrayList<String>();
	private boolean checkSize;
	private List<String> acceptedSizes = new ArrayList<String>();
	private boolean checkType;
	private List<String> acceptedTypes = new ArrayList<String>();
	private boolean checkAttributes;
	private List<Attribute> excludedAttributes = new ArrayList<Attribute>();
	private List<Attribute> includedAttributes = new ArrayList<Attribute>();
	private boolean checkKeyword;
	private String keyWord;

	/**
	 * @param search
	 * @param login
	 * @param man
	 * @param props
	 */
	public SearcherProgress(ListSearcher search, Login login, UserIdManager man, Properties props)
	{
		super(search, login, man, props);
		parseProperties();
	}

	/**
	 * 
	 */
	private void parseProperties()
	{
		SimpleDateFormat dateformat = new SimpleDateFormat(login.getDateFormat());
		checkFavouritePoints = Boolean.parseBoolean(properties.getProperty("btnRequireFav", "false"));
		if (checkFavouritePoints)
		{
			minFavPoints = Integer.parseInt(properties.getProperty("favouritePoints"));
		}
		checkPlacedOnOrAfter = Boolean.parseBoolean(properties.getProperty("btnPlacedAfter", "false"));
		if (checkPlacedOnOrAfter)
		{
			try
			{
				afterTime = dateformat.parse(properties.getProperty("afterDateField")).getTime();
			}
			catch (ParseException e)
			{
				checkPlacedOnOrAfter = false;
			}
		}
		checkPlacedOnOrBefore = Boolean.parseBoolean(properties.getProperty("btnPlacedBefore", "false"));
		if (checkPlacedOnOrBefore)
		{
			try
			{
				beforeTime = dateformat.parse(properties.getProperty("beforeDateField")).getTime();
			}
			catch (ParseException e)
			{
				checkPlacedOnOrBefore = false;
			}
		}
		checkFoundDate = Boolean.parseBoolean(properties.getProperty("btnFoundInLast", "false"));
		if (checkFoundDate)
		{
			try
			{
				int days = Integer.parseInt(properties.getProperty("foundInlastDaysField"));
				Calendar c = new GregorianCalendar();
				c.add(Calendar.DATE, -days);
				lastFoundDate = c.getTimeInMillis();
			}
			catch (Exception e)
			{
				checkFoundDate = false;
			}
		}
		notFound = Boolean.parseBoolean(properties.getProperty("btnHasNotBeen", "false"));
		checkTrackables = Boolean.parseBoolean(properties.getProperty("btnHasTrackable", "false"));
		checkDisabled = Boolean.parseBoolean(properties.getProperty("btnIgnoreDisabled", "false"));
		ignoreOwn = Boolean.parseBoolean(properties.getProperty("btnIgnoreOwn", "false"));
		ignoreFound = Boolean.parseBoolean(properties.getProperty("btnIgnoreFound", "false"));
		checkDT = Boolean.parseBoolean(properties.getProperty("btnFilterDifficultyterrain", "false"));
		if (checkDT)
		{
			for (Object keyObj : properties.keySet())
			{
				String key = (String) keyObj;
				if (Pattern.matches(Exporter.DT_COMBO_STRING, key))
				{
					if (properties.getProperty(key).equals("true"))
					{
						this.acceptedDTCombos.add(key);
					}
				}
			}
		}

		checkSize = Boolean.parseBoolean(properties.getProperty("btnFilterSize", "false"));
		if (checkSize)
		{
			if (properties.getProperty("btnNotChosenSize").equals("true"))
			{
				acceptedSizes.add(CacheSize.NOT_CHOSEN.toString());
			}
			if (properties.getProperty("btnMicroSize").equals("true"))
			{
				acceptedSizes.add(CacheSize.MICRO.toString());
			}
			if (properties.getProperty("btnVirtualSize").equals("true"))
			{
				acceptedSizes.add(CacheSize.VIRTUAL.toString());
			}
			if (properties.getProperty("btnRegularSize").equals("true"))
			{
				acceptedSizes.add(CacheSize.REGULAR.toString());
			}
			if (properties.getProperty("btnLargeSize").equals("true"))
			{
				acceptedSizes.add(CacheSize.LARGE.toString());
			}
			if (properties.getProperty("btnUknownSize").equals("true"))
			{
				acceptedSizes.add(CacheSize.UKNOWN.toString());
			}
			if (properties.getProperty("btnOtherSize").equals("true"))
			{
				acceptedSizes.add(CacheSize.OTHER.toString());
			}
			if (properties.getProperty("btnSmallSize").equals("true"))
			{
				acceptedSizes.add(CacheSize.SMALL.toString());
			}
		}

		checkType = Boolean.parseBoolean(properties.getProperty("btnFilterType", "false"));
		if (checkType)
		{
			if (properties.getProperty("btnCito").equals("true"))
			{
				acceptedTypes.add(CacheType.CITO.toString());
			}

			if (properties.getProperty("btnEarthcache").equals("true"))
			{
				acceptedTypes.add(CacheType.EARTH_CACHE.toString());
			}
			if (properties.getProperty("btnWherigo").equals("true"))
			{
				acceptedTypes.add(CacheType.WHERIGO.toString());
			}
			if (properties.getProperty("btnGpsAdventure").equals("true"))
			{
				acceptedTypes.add(CacheType.GPSADVENTURE.toString());
			}
			if (properties.getProperty("btnLetterbox").equals("true"))
			{
				acceptedTypes.add(CacheType.LETTERBOX.toString());
			}
			if (properties.getProperty("btnEvent").equals("true"))
			{
				acceptedTypes.add(CacheType.EVENT.toString());
			}
			if (properties.getProperty("btnMegaEvent").equals("true"))
			{
				acceptedTypes.add(CacheType.MEGA_EVENT.toString());
			}
			if (properties.getProperty("btnMystery").equals("true"))
			{
				acceptedTypes.add(CacheType.MYSTERY.toString());
			}
			if (properties.getProperty("btnMulti").equals("true"))
			{
				acceptedTypes.add(CacheType.MULTI.toString());
			}
			if (properties.getProperty("btnWebcam").equals("true"))
			{
				acceptedTypes.add(CacheType.WEBCAM.toString());
			}
			if (properties.getProperty("btnVirtualType").equals("true"))
			{
				acceptedTypes.add(CacheType.VIRTUAL.toString());
			}
			if (properties.getProperty("btnTraditional").equals("true"))
			{
				acceptedTypes.add(CacheType.TRADITIONAL.toString());
			}
		}

		checkAttributes = Boolean.parseBoolean(properties.getProperty("btnFilterAttributes", "false"));
		if (checkAttributes)
		{
			for (Object keyObj : properties.keySet())
			{
				String key = (String) keyObj;
				if (key.startsWith("attribute/"))
				{
					String attr = key.split("/")[1];
					Attribute a = Attribute.parse(attr);
					if (a != null)
					{
						String value = properties.getProperty(key);
						if (value.equals("include"))
						{
							includedAttributes.add(a);
						}
						else if (value.equals("exclude"))
						{
							excludedAttributes.add(a);
						}
					}
				}
			}
		}

		checkKeyword = Boolean.parseBoolean(properties.getProperty("btnMatchKeyword", "false"));
		if (checkKeyword)
		{
			this.keyWord = properties.getProperty("keywordText", "");
		}

		includeLogs = Boolean.parseBoolean(properties.getProperty("btnIncludeLogs"));
	}

	/**
	 * @param cache
	 * @return
	 */
	protected boolean checkPostDownload(Cache cache)
	{
		// check things that can only be checked after complete cache download:
		// - check that it's not a premium cache that we don't have access to
		if (cache.isUnavailableToUs())
		{
			return false;
		}
		// - attributes
		if (checkAttributes)
		{
			Set<Attribute> attributes = cache.getAttributes();
			// check that none of the excluded attributes exist
			for (Attribute a : attributes)
			{
				if (excludedAttributes.contains(a))
				{
					return false;
				}
			}
			// check that all included attributes exist
			for (Attribute a : includedAttributes)
			{
				if (!attributes.contains(a))
				{
					return false;
				}
			}
		}

		try
		{
			if (includeLogs)
			{
				// get the full logs
				while (cache.retrieveMoreLogs(login))
				{
					// just loop until we run out of logs
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * @param cache
	 * @return
	 */
	protected boolean checkPreDownload(Cache cache)
	{

		if (maxFind > 0 && caches.size() >= maxFind)
		{
			searcher.abort();
			return false;
		}
		// check things that can be checked before complete cache download:
		// - number of favourite points
		if (checkFavouritePoints && cache.getFavourited() < minFavPoints)
		{
			return false;
		}
		// - placement date on or after
		if (checkPlacedOnOrAfter && cache.getPlacedAt() < afterTime)
		{
			return false;
		}
		// - placement date on or before
		if (checkPlacedOnOrBefore && cache.getPlacedAt() > beforeTime)
		{
			return false;
		}
		// - trackables
		if (checkTrackables && cache.getTrackableCount() == 0)
		{
			return false;
		}
		// - disabled or not
		if (checkDisabled && cache.isDisabled())
		{
			return false;
		}
		// - owned or not
		if (ignoreOwn && cache.getOwner().getName().equals(login.getUserName()))
		{
			return false;
		}
		// - found by us or not
		if (ignoreFound && cache.isFoundByUser())
		{
			return false;
		}
		// - last find date or not found at all
		if (notFound && cache.getLastFoundDate() != null)
		{
			return false;
		}
		if (checkFoundDate && cache.getLastFoundDate() < lastFoundDate)
		{
			return false;
		}
		// - type
		if (checkType)
		{
			String type = cache.getCacheType().toString();
			if (!this.acceptedTypes.contains(type))
			{
				return false;
			}
		}
		// check if the "needs maintenance" attribute is checked for
		if (checkAttributes)
		{
			if (this.excludedAttributes.contains(Attribute.FIRSTAID_YES))
			{
				if (cache.getAttributes().contains(Attribute.FIRSTAID_YES))
				{
					return false;
				}
			}
			if (this.includedAttributes.contains(Attribute.FIRSTAID_YES))
			{
				if (!cache.getAttributes().contains(Attribute.FIRSTAID_YES))
				{
					return false;
				}
			}
		}
		// keyword matching
		if (checkKeyword)
		{
			// check the title for keywords
			if (keyWord != null)
			{
				if (!cache.getName().toLowerCase().contains(keyWord.toLowerCase()))
				{
					return false;
				}
			}
		}
		// only populate the Difficulty/Terrain/Size image if we have to
		if (checkDT || checkSize)
		{
			searcher.populateDTfromImage(cache);
		}
		// - D/T
		if (checkDT)
		{
			String dt = cache.getDifficultyRating() + "/" + cache.getTerrainRating();
			dt = dt.replaceAll("\\.0", "");
			if (!this.acceptedDTCombos.contains(dt))
			{
				return false;
			}
		}
		// - size
		if (checkSize)
		{
			String size = cache.getCacheSize().toString();
			if (!this.acceptedSizes.contains(size))
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
	{
		this.monitor = monitor;
		monitor.beginTask("Preparing search...", 100);
		this.maxFind = Integer.parseInt(properties.getProperty("maximumresults"));
		Location loc = new Location(properties.getProperty("locationInput"));
		double radius = Double.parseDouble(properties.getProperty("searchradius")) / 10.0;
		// convert miles to km if we need to
		boolean useMiles = false;
		if (properties.getProperty("searchRadiusUnits") != null
				&& properties.getProperty("searchRadiusUnits").equals("1"))
		{
			useMiles = true;
		}
		if (useMiles)
		{
			radius *= 1.609344;
		}
		// check if we ignore our own caches
		boolean ignoreFoundAndOwn = ignoreFound & ignoreOwn;
		try
		{
			searcher.findCachesCloseTo(loc, radius, ignoreFoundAndOwn);
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
