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
 * This class keeps track of the userids of named cachers. 
 * Persistent storage in the user's homedir.
 */
public class UserIdManager
{

	private static final String ID_DATABASE_FILENAME = System.getProperty("user.home") + File.separator + ".gpxexporter.id.db";
	private HashMap<String, Long> userIds;
	private Login login;
	
	public UserIdManager(Login login)
	{
		this.login = login;
	}

	public Long getId(Cacher cacher) throws IOException
	{
		// check cache for id
		Long res = userIds.get(cacher.getProfilePage());
		if (res == null)
		{
			// find the id
			System.out.println("Getting id for user " + cacher.getName());
			cacher.populate(login);
			// store it
			res = cacher.getId();
			userIds.put(cacher.getProfilePage(), res);
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
		}
	}
	public void saveDb()
	{
		try
		{
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
					ID_DATABASE_FILENAME, false));
			oos.writeObject(userIds);
			oos.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @param loggedBy
	 * @param id
	 */
	public void setId(Cacher loggedBy, Long id)
	{
		userIds.put(loggedBy.getProfilePage(), id);		
	}
}
