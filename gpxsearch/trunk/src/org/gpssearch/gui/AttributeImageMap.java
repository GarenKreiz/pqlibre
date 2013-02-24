package org.gpssearch.gui;

import org.geoscrape.Attribute;

/**
 * Map between an attribute and an image.
 */
public class AttributeImageMap
{
	private static String [] names = 
	{
		null,//start with null to make indexes correct
		"attribute_dogs",
		"attribute_fee",
		"attribute_rappelling",
		"attribute_boat",
		"attribute_scuba",
		"attribute_kids",
		"attribute_onehour",
		"attribute_scenic",
		"attribute_hiking",
		"attribute_climbing",
		"attribute_wading",
		"attribute_swimming",
		"attribute_available",
		"attribute_night",
		"attribute_winter",
		null,//attribute 16 does not exist
		"attribute_poisonoak",
		"attribute_dangerousanimals",
		"attribute_ticks",
		"attribute_mine",
		"attribute_cliff",
		"attribute_hunting",
		"attribute_danger",
		"attribute_wheelchair",
		"attribute_parking",
		"attribute_public",
		"attribute_water",
		"attribute_restrooms",
		"attribute_phone",
		"attribute_picnic",
		"attribute_camping",
		"attribute_bicycles",
		"attribute_motorcycles",
		"attribute_quads",
		"attribute_jeeps",
		"attribute_snowmobiles",
		"attribute_horses",
		"attribute_campfires",
		"attribute_thorn",
		"attribute_stealth",
		"attribute_stroller",
		"attribute_firstaid",
		"attribute_cow",
		"attribute_flashlight",
		"attribute_landf",
		"attribute_rv",
		"attribute_field_puzzle",
		"attribute_uv",
		"attribute_snowshoes",
		"attribute_skiis",
		"attribute_s_tool",
		"attribute_nightcache",
		"attribute_parkngrab",
		"attribute_abandonedbuilding",
		"attribute_hike_short",
		"attribute_hike_med",
		"attribute_hike_long",
		"attribute_fuel",
		"attribute_food",
		"attribute_wirelessbeacon",
		"attribute_partnership",
		"attribute_seasonal",
		"attribute_touristok",
		"attribute_treeclimbing",
		"attribute_frontyard",
		"attribute_teamwork"
	};
	
	public static String getAttributFileName(Attribute a)
	{
		return names[a.getId()];
	}
}
