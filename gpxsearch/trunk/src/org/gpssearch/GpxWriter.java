package org.gpssearch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.geoscrape.Attribute;
import org.geoscrape.Cache;
import org.geoscrape.CacheLog;
import org.geoscrape.WayPoint;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * Generates .gpx formatted output from a list of caches.
 * 
 */
public class GpxWriter
{
	private List<Cache> caches;
	private UserIdManager idManager;

	public GpxWriter(List<Cache> caches, UserIdManager man)
	{
		this.caches = caches;
		this.idManager = man;

	}

	public void write(String fileName) throws IOException
	{
		write(new File(fileName));
	}

	/**
	 * @param file
	 * @throws IOException
	 */
	public void write(File file) throws IOException
	{
		OutputStream os = new FileOutputStream(file);
		try
		{
			write(os);
		}
		catch (SAXException e)
		{
			throw new IOException(e);
		}
		finally
		{
			os.close();
		}

	}

	/**
	 * @param os
	 * @throws IOException
	 * @throws SAXException
	 */
	public void write(OutputStream os) throws IOException, SAXException
	{
		OutputFormat of = new OutputFormat("XML", "UTF-8", true);
		of.setIndent(1);
		of.setIndenting(true);
		XMLSerializer serializer = new XMLSerializer(os, of);
		// SAX2.0 ContentHandler.
		ContentHandler hd = serializer.asContentHandler();
		hd.startDocument();
		AttributesImpl atts = new AttributesImpl();
		// gpx tag.
		atts.addAttribute("", "", "xmlns:xsi", "", "http://www.w3.org/2001/XMLSchema-instance");
		atts.addAttribute("", "", "xmlns:xsd", "", "http://www.w3.org/2001/XMLSchema");
		atts.addAttribute("", "", "version", "", "1.0");
		atts.addAttribute("", "", "creator", "", "Groundspeak Pocket Query");
		atts.addAttribute(
				"",
				"",
				"xsi:schemaLocation",
				"",
				"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd http://www.groundspeak.com/cache/1/0/1 http://www.groundspeak.com/cache/1/0/1/cache.xsd");
		atts.addAttribute("", "", "xmlns", "", "http://www.topografix.com/GPX/1/0");
		hd.startElement("", "", "gpx", atts);

		// preamble
		putElement(hd, "name", "Pocket Query");
		putElement(hd, "desc", "Geocache file generated by GpxWriter");
		putElement(hd, "author", "GpxWriter");
		putElement(hd, "email", "foo@bar.com");
		Date d = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
		dateFormat.setTimeZone(TimeZone.getTimeZone("Z"));
		putElement(hd, "time", dateFormat.format(d));
		putElement(hd, "keywords", "cache, geocache, groundspeak");

		dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		dateFormat.setTimeZone(TimeZone.getTimeZone("Z"));

		// find the ranges of latitude and longitude
		double minLat = Double.POSITIVE_INFINITY;
		double minLon = Double.POSITIVE_INFINITY;
		double maxLat = Double.NEGATIVE_INFINITY;
		double maxLon = Double.NEGATIVE_INFINITY;
		for (Cache c : caches)
		{
			if (!c.isUnavailableToUs())
			{
				double lat = c.getLocation().getLatitude().getDegreeWithFraction();
				double lon = c.getLocation().getLongitude().getDegreeWithFraction();
				minLat = Math.min(minLat, lat);
				minLon = Math.min(minLon, lon);
				maxLat = Math.max(maxLat, lat);
				maxLon = Math.max(maxLon, lon);
			}
		}
		atts.clear();
		atts.addAttribute("", "", "minlat", "", Double.toString(minLat));
		atts.addAttribute("", "", "minlon", "", Double.toString(minLon));
		atts.addAttribute("", "", "maxlat", "", Double.toString(maxLat));
		atts.addAttribute("", "", "maxlon", "", Double.toString(maxLon));
		hd.startElement("", "", "bounds", atts);
		hd.endElement("", "", "bounds");

		// add all the logs
		for (Cache c : caches)
		{
			if (c.isUnavailableToUs())
			{
				// skip premium caches if we are not a premium user
				continue;
			}
			// add coordinates
			atts.clear();
			atts.addAttribute("", "", "lat", "", c.getLocation().getLatitude().toDecimalString());
			atts.addAttribute("", "", "lon", "", c.getLocation().getLongitude().toDecimalString());

			// add the cache details
			String cacheType = c.getCacheType().getDescription();
			hd.startElement("", "", "wpt", atts);
			putElement(hd, "time", dateFormat.format(new Date(c.getPlacedAt())));
			putElement(hd, "name", c.getCacheCode());

			String desc = c.getName() + " by " + c.getHider().getName();
			desc += ", " + cacheType + " (";
			desc += Double.toString(c.getDifficultyRating()) + "/" + Double.toString(c.getTerrainRating()) + ")";
			putElement(hd, "desc", desc);
			putElement(hd, "url", "http://www.geocaching.com/seek/cache_details.aspx?guid=" + c.getGuid());
			putElement(hd, "urlname", c.getName());
			if (c.isFound())
			{
				putElement(hd, "sym", "Geocache Found");
			}
			else
			{
				putElement(hd, "sym", "Geocache");
			}
			putElement(hd, "type", "Geocache|" + cacheType);

			atts.clear();
			atts.addAttribute("", "", "id", "", Long.toString(c.getId()));
			if (c.isDisabled())
			{
				atts.addAttribute("", "", "available", "", "False");
			}
			else
			{
				atts.addAttribute("", "", "available", "", "True");
			}
			if (c.isArchived())
			{
				atts.addAttribute("", "", "archived", "", "True");
			}
			else
			{
				atts.addAttribute("", "", "archived", "", "False");
			}
			atts.addAttribute("", "", "xmlns:groundspeak", "", "http://www.groundspeak.com/cache/1/0/1");
			hd.startElement("", "", "groundspeak:cache", atts);
			putElement(hd, "groundspeak:name", c.getName());
			putElement(hd, "groundspeak:placed_by", c.getHider().getName());
			atts.clear();
			atts.addAttribute("", "", "id", "", Long.toString(idManager.getId(c.getHider())));
			putElement(hd, "groundspeak:owner", c.getHider().getName(), atts);
			putElement(hd, "groundspeak:type", cacheType);
			putElement(hd, "groundspeak:container", c.getCacheSize().toString());
			hd.startElement("", "", "groundspeak:attributes", null);

			for (Attribute a : c.getAttributes())
			{
				atts.clear();
				atts.addAttribute("", "", "id", "", Integer.toString(a.getId()));
				atts.addAttribute("", "", "inc", "", Integer.toString(a.getInc()));
				putElement(hd, "groundspeak:attribute", a.toString(), atts);
			}
			hd.endElement("", "", "groundspeak:attributes");

			putElement(hd, "groundspeak:difficulty", Double.toString(c.getDifficultyRating()));
			putElement(hd, "groundspeak:terrain", Double.toString(c.getTerrainRating()));

			String country = "";
			String state = "";
			String locDesc = c.getLocationDescription();
			if (locDesc.contains(","))
			{
				String[] parts = locDesc.split(",");
				state = parts[0].trim();
				country = parts[1].trim();
			}
			else
			{
				country = locDesc;
			}
			putElement(hd, "groundspeak:country", country);
			putElement(hd, "groundspeak:state", state);

			atts.clear();
			atts.addAttribute("", "", "html", "", "True");
			putElement(hd, "groundspeak:short_description", c.getShortDescription(), atts);
			putElement(hd, "groundspeak:long_description", c.getLongDescription(), atts);
			putElement(hd, "groundspeak:encoded_hints", c.getHint());

			// add the log details
			hd.startElement("", "", "groundspeak:logs", null);
			for (CacheLog log : c.getLogs())
			{
				atts.clear();
				atts.addAttribute("", "", "id", "", Long.toString(log.getId()));
				hd.startElement("", "", "groundspeak:log", atts);
				putElement(hd, "groundspeak:date", dateFormat.format(new Date(log.getLogTime())));
				putElement(hd, "groundspeak:type", log.getLogType().getText());
				atts.clear();

				if(log.getLoggedBy().getId()!=null)
				{
					atts.addAttribute("", "", "id", "", Long.toString(log.getLoggedBy().getId()));
					idManager.setId(log.getLoggedBy(),log.getLoggedBy().getId());
				}
				else
				{
					atts.addAttribute("", "", "id", "", "");
				}

				putElement(hd, "groundspeak:finder", log.getLoggedBy().getName(), atts);
				atts.clear();
				atts.addAttribute("", "", "encoded", "", "False");
				putElement(hd, "groundspeak:text", log.getText(), atts);

				hd.endElement("", "", "groundspeak:log");
			}

			hd.endElement("", "", "groundspeak:logs");
			hd.endElement("", "", "groundspeak:cache");
			hd.endElement("", "", "wpt");

			// save the waypoints
			for (WayPoint wp : c.getWaypoints())
			{
				atts.clear();
				if (wp.getLocation() != null)
				{
					atts.addAttribute("", "", "lat", "", wp.getLocation().getLatitude().toDecimalString());
					atts.addAttribute("", "", "lon", "", wp.getLocation().getLongitude().toDecimalString());
				}

				// add the waypoint details
				hd.startElement("", "", "wpt", atts);
				putElement(hd, "name", wp.getWaypointCode());
				putElement(hd, "sym", wp.getType());
				putElement(hd, "type", "Waypoint|" + wp.getType());
				hd.endElement("", "", "wpt");

			}
		}
		hd.endElement("", "", "gpx");
		hd.endDocument();
	}

	private void putElement(ContentHandler hd, String name, String content) throws SAXException
	{
		putElement(hd, name, content, null);
	}

	private void putElement(ContentHandler hd, String name, String content, AttributesImpl atts) throws SAXException
	{
		hd.startElement("", "", name, atts);
		if (content != null)
		{
			hd.characters(content.toCharArray(), 0, content.length());
		}
		hd.endElement("", "", name);
	}
}
