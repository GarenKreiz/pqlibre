package org.gpssearch.gui;

import static org.junit.Assert.*;

import java.io.File;

import org.geoscrape.Attribute;
import org.junit.Test;

/**
 * 
 *
 */
public class AttributeImageMapTest
{

	/**
	 * Test method for {@link org.gpssearch.gui.AttributeImageMap#getAttributFileName(org.geoscrape.Attribute)}.
	 */
	@Test
	public void testGetAttributFileName()
	{
		Attribute [] all= Attribute.values();
		for(Attribute a:all)
		{
			String name = AttributeImageMap.getAttributFileName(a);
			
			//check that the name is found
			assertNotNull(name);
			
			//check that the image file exists
			File f  = new File("res"+File.separator+name+".png");
			assertTrue(f.exists());
			//check that the image file is not empty
			assertTrue(f.length()>0);
		}
	}

}
