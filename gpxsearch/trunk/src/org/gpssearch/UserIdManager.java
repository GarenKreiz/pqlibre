package org.gpssearch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import org.geoscrape.Cacher;
import org.geoscrape.Login;

/**
 * This class keeps track of the userids of named cachers. Persistent storage in
 * the user's homedir.
 */
public class UserIdManager
{

	private static final String ID_DATABASE_FILENAME = System.getProperty("user.home") + File.separator
			+ ".gpxexporter.id.db";
	private HashMap<String, Long> userIds;
	private HashMap<String, Long> userNames;
	private Login login;

	public UserIdManager(Login login)
	{
		this.login = login;
	}

	public Long getId(Cacher cacher) throws IOException
	{
		Long res = null;
		// check cache for id
		if (cacher.getProfilePage() != null)
		{
			res = userIds.get(cacher.getProfilePage());
		}
		if (res == null)
		{
			if (cacher.getName() != null)
			{
				res = userNames.get(cacher.getName());
			}
			if (res == null)
			{
				// find the id
				System.out.println("Getting id for user " + cacher.getName());
				cacher.populate(login);
				// store it
				res = cacher.getId();
				if (cacher.getProfilePage() != null)
				{
					userIds.put(cacher.getProfilePage(), res);
				}
				if (cacher.getName() != null)
				{
					userNames.put(cacher.getName(), res);
				}
			}
		}
		return res;
	}

	@SuppressWarnings("unchecked")
	public void loadDb() throws ClassNotFoundException
	{
		try
		{
			File f = new File(ID_DATABASE_FILENAME);
			if (f.exists())
			{
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
				userIds = (HashMap<String, Long>) ois.readObject();
				userNames = (HashMap<String, Long>) ois.readObject();
				ois.close();
			}
		}
		catch (IOException e)
		{
			// fail silently
		}
		finally
		{
			// don't leave this method without creating a user id database
			if (userIds == null)
			{
				userIds = new HashMap<String, Long>();
			}
			if (userNames == null)
			{
				userNames = new HashMap<String, Long>();
			}
		}
	}

	public void saveDb()
	{
		try
		{
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ID_DATABASE_FILENAME, false));
			oos.writeObject(userIds);
			oos.writeObject(userNames);
			oos.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @param c
	 * @param id
	 */
	public void setId(Cacher c, Long id)
	{
		if (c.getProfilePage() != null)
		{
			userIds.put(c.getProfilePage(), id);
		}
		if (c.getName() != null)
		{
			userNames.put(c.getName(), id);
		}
	}
}
