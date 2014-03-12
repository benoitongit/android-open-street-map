package com.android.lib.map.osm.models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.android.lib.map.osm.utils.DateUtil;

import android.content.ContentValues;
import android.database.Cursor;


public class MapEntity extends OsmModel {

	public static final String ENTITY_TYPE_REGION = "REGION";
	public static final String ENTITY_TYPE_TRIP = "TRIP";
	
	private static final String TABLE_ENTITY_NAME = "mapEntities";

	public int		c_entityId;
	public int		c_associatedEntityId;
	public String	c_associatedEntityType;
	public String	c_title;
	public double	c_maxLat;
	public double	c_maxLon;
	public double	c_minLat;
	public double	c_minLon;
	public Calendar	c_creationDate;
	
	
	public static MapEntity getById(int entityId) {
		
		MapEntity mapEntity = null;
		Cursor c = mDbHelper.get(TABLE_ENTITY_NAME, "entityId=" + entityId, 
				null, null, "1"); 
		while (c.moveToNext()) {
			
			mapEntity = getEntityFromCursor(c);
			
			break;
		}
		c.close();
		return mapEntity;
	}
	
	public static int deleteById(int entityId) {
		return mDbHelper.delete(TABLE_ENTITY_NAME, "entityId=" + entityId);
	}
	
	public static boolean hasEntity(int associatedEntityId, String associatedEntityType) {
		String[] columnsSelected = {"entityId"};
		Cursor c = mDbHelper.get(TABLE_ENTITY_NAME, "associatedEntityId=" + associatedEntityId 
				+ " AND associatedEntityType LIKE '" + associatedEntityType + "'", 
				columnsSelected, null, null); 
		
		if (c.getCount() > 0) {
			c.close();
			return true;
		}
		
		c.close();
		return false;
	}
	
	public static int insertEntity(int associatedEntityId, String associatedEntityType, String associatedEntityTitle,
			double maxLat, double minLat, double maxLon, double minLon) {
		
		boolean hasEntity = false;
		if (associatedEntityId != 0)
			hasEntity = hasEntity(associatedEntityId, associatedEntityType);
		
		long id = 0;
		
		if (!hasEntity) {
			//INSERT
			ContentValues tileValues = new ContentValues();
			tileValues.put("associatedEntityId", associatedEntityId);
			tileValues.put("associatedEntityType", associatedEntityType);
			tileValues.put("title", associatedEntityTitle);
			tileValues.put("maxLat", maxLat);
			tileValues.put("maxLon", maxLon);
			tileValues.put("minLat", minLat);
			tileValues.put("minLon", minLon);
			tileValues.put("creationDate", DateUtil.longToSqlDateFormat(
					Calendar.getInstance().getTimeInMillis()));
			id = mDbHelper.insert(TABLE_ENTITY_NAME, tileValues);
		}
		return (int) id;
	}
	
	public static List<MapEntity> getEntities() {

		List<MapEntity> mapEntities = new ArrayList<MapEntity>();
		
		Cursor c = mDbHelper.get(TABLE_ENTITY_NAME, null, null, "creationDate DESC", null);
		
		while (c.moveToNext()) {
			
			MapEntity mapEntity = getEntityFromCursor(c);
			mapEntities.add(mapEntity);
		}
		
		c.close();
		return mapEntities;
	}
	
	public static MapEntity getEntityFromCursor(Cursor c) {
		MapEntity mapEntity = new MapEntity();
		mapEntity.c_entityId = c.getInt(c.getColumnIndex("entityId"));
		mapEntity.c_associatedEntityId = c.getInt(c.getColumnIndex("associatedEntityId"));
		mapEntity.c_associatedEntityType = c.getString(c.getColumnIndex("associatedEntityType"));
		mapEntity.c_title = c.getString(c.getColumnIndex("title"));
		mapEntity.c_maxLon = c.getDouble(c.getColumnIndex("maxLon"));
		mapEntity.c_maxLat = c.getDouble(c.getColumnIndex("maxLat"));
		mapEntity.c_minLon = c.getDouble(c.getColumnIndex("minLon"));
		mapEntity.c_minLat = c.getDouble(c.getColumnIndex("minLat"));
		mapEntity.c_creationDate = DateUtil.stringToCalendar(c.getString(c.getColumnIndex("creationDate")));
		return mapEntity;
	}
}
