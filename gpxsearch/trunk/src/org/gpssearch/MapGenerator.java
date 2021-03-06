package org.gpssearch;

import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.geoscrape.Cache;
import org.geoscrape.CacheType;
import org.geoscrape.Location;
import org.geoscrape.util.Tools;
import org.geoscrape.util.UserAgentFaker;
import org.geoscrape.util.WebClient;

/**
 * Generates a comma separated value description of the listed caches, uploads
 * them to gpsvisualiser.com and displays the result.
 * 
 */
public class MapGenerator implements IRunnableWithProgress
{

	private String ourname;
	private Desktop desktop;
	private File cacheFile;
	private int cacheCount;

	/**
	 * Create a new map generator.
	 * 
	 * @param cacheCount the number of caches to generate a map for.
	 * @param cacheFile the file containing the serialised caches.
	 * @param ourname
	 *            the name of the user that owns the caches to be marked with an
	 *            "our" icon.
	 * @param desktop
	 */
	public MapGenerator(int cacheCount, File cacheFile, String ourname, Desktop desktop)
	{
		this.cacheCount = cacheCount;
		this.cacheFile = cacheFile;
		this.ourname = ourname;
		this.desktop = desktop;
	}
	

	/**
	 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
	{
		monitor.beginTask("Preparing map.", 100);

		try
		{
			monitor.subTask("Generating description...");
			ObjectInputStream input = new ObjectInputStream(new FileInputStream(cacheFile));
			String newline = "\n";
			StringBuilder url = new StringBuilder("name,desc,lat,lon,icon_size,sym");
			for(int x = 0;x<cacheCount;x++)
			{
				Cache c = (Cache)input.readObject();
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
			input.close();
			monitor.subTask("Compressing description...");
			monitor.worked(10);
			// compress the string
			ByteArrayOutputStream data = new ByteArrayOutputStream();
			ZipOutputStream out = new ZipOutputStream(data);
			out.setLevel(9);
			out.putNextEntry(new ZipEntry("file.csv"));
			byte[] tmp = url.toString().getBytes("UTF-8");
			out.write(tmp);
			out.closeEntry();
			out.close();
			monitor.subTask("Uploading description...");
			monitor.worked(20);

			// submit the string to the form
			WebClient wc = new WebClient();
			wc.setRequestMethod("POST");
			wc.setUserAgent(UserAgentFaker.getRandomUserAgent());
			Map<String, String> params = new HashMap<String, String>();
			params.put("format", "google");
			params.put("convert_format", "");
			params.put("form", "google");
			params.put("google_full_screen", "1");
			params.put("zoom_control", "3d");
			//leaving the following in skips the output page and returns the results directly
			params.put("return_image", "1");
			Map<String, byte[]> files = new HashMap<String, byte[]>();
			files.put("uploaded_file_1@tmp.zip", data.toByteArray());
			monitor.worked(1);

			wc.submitForm("http://www.gpsvisualizer.com/map?output_home", params, files);
			monitor.subTask("Reading map...");
			monitor.worked(50);

			// parse the result, get the address
			String contents = wc.getContentsAsString();
			//enable mouswheel zoom
			contents = contents.replace("gv_options.mousewheel_zoom = false;","gv_options.mousewheel_zoom = true;");
			//create a temporary file
			File tmpFile = File.createTempFile("gpxsearcher", ".html");
			tmpFile.deleteOnExit();
			String address =  tmpFile.getAbsolutePath();
			if(address.indexOf(":")==1)
			{
				//windows local path
				address = "/"+address;
			}
			//recode backslashes
			address = address.replaceAll("\\\\", "/");
			//spaces not allowed
			address = address.replaceAll(" ", "%20");
			address = "file://"+address;
			//write the map to temp file
			FileWriter fw = new FileWriter(tmpFile);
			fw.write(contents);
			fw.close();
			
			
			monitor.subTask("Redirecting browser window...");
			monitor.worked(10);

			//open temp file in browser
			desktop.browse(new URI(address));
			//keep dialog open for a little bit longer
			Tools.sleep(500);

		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
		monitor.done();
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
		if (c.isFoundByUser())
		{
			res = "found";
		}
		else if (c.getHider().getName().equals(ourname))
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
}
