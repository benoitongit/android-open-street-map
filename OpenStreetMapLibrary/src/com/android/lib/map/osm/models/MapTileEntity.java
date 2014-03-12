package com.android.lib.map.osm.models;

import java.util.ArrayList;
import java.util.List;

import com.android.lib.map.osm.Tile;
import com.android.lib.map.osm.helpers.OsmDatabaseHelper;



import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

public class MapTileEntity extends OsmModel {

	public static final String TABLE_TILE_ENTITY_NAME = "mapTilesEntities";
	
	
	public static void insertTilesForEntity(List<Tile> tiles, int entityId) {
		
		OsmDatabaseHelper db = mDbHelper;
		
		List<ContentValues> values = new ArrayList<ContentValues>();
				
		db.mDb.beginTransaction();
		try {

			for (Tile tile : tiles) {
				
				String tileId = MapTile.getTileId(tile);
				if (tileId != null) {
					ContentValues contentValue = new ContentValues();
					contentValue.put("tilekey", tileId);
					contentValue.put("entityId", entityId);
					values.add(contentValue);
				}
			}
			
			for (ContentValues value : values) {
				db.insert(TABLE_TILE_ENTITY_NAME, value);
			}
			
			db.mDb.setTransactionSuccessful();
		} finally {
			db.mDb.endTransaction();
		}	
	}
	
	public static List<Tile> getTilesForEntity(int entityId) {
		
		OsmDatabaseHelper db = mDbHelper;
		
		List<Tile> tiles = new ArrayList<Tile>();
		
		String mapTileTable = MapTile.TABLE_TILE_NAME;
		String sql = "SELECT row, col, zoom FROM " + mapTileTable + ", " + TABLE_TILE_ENTITY_NAME +
		" WHERE "+mapTileTable+".tilekey="+TABLE_TILE_ENTITY_NAME+".tilekey" +
		" AND "+TABLE_TILE_ENTITY_NAME+".entityId=" + entityId + ";";
		
		Cursor c = db.query(sql);
	
		Log.i("request", sql + "       count=" + c.getCount());
		
		while (c.moveToNext()) {
			tiles.add(MapTile.getTileFromCursor(c));
		}
		
		c.close();
		
		return tiles;
	}
	
	public static int deleteByEntityId(int entityId) {
		return mDbHelper.delete(TABLE_TILE_ENTITY_NAME, "entityId=" + entityId);
	}
}
