package com.android.lib.map.osm.models;

import java.io.ByteArrayInputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.lib.map.osm.Tile;
import com.android.lib.map.osm.helpers.OsmDatabaseHelper;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;



public class MapTile extends OsmModel {

	public final static String SQL_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String TABLE_TILE_NAME = "tiles";
	
	private final static BitmapFactory.Options BITMAP_OPTIONS = new BitmapFactory.Options();
	private final static String[] COLUMN_IMAGE = {"image"};
	
	private static int mMapMinZoomLevel = -1;

	public static Map<Tile, Bitmap> getTiles(List<Tile> tiles) {
				
		
		String[] columnsSelected = {"row", "col", "zoom", "image"};
		Map<Tile, Bitmap> tileBitmapMap = new HashMap<Tile, Bitmap>();
		
		for (Tile t : tiles) {
			tileBitmapMap.put(t, null);
		}
		
		if (mDbHelper == null)
			return tileBitmapMap;
		
		try {

			String tileSql = new String();
			for (Tile tile : tiles) {
				if (tileSql.length() > 0)
					tileSql += " OR ";
				tileSql += "(" + getOsmTileSQLRequest(tile.mapY, tile.mapX, tile.zoom) + ")";
			}
			
			Cursor c = mDbHelper.get(TABLE_TILE_NAME, tileSql, columnsSelected, null, tiles.size()+""); 
			
			while (c.moveToNext()) {
			
				byte[] tileBitmapByteArray = c.getBlob(c.getColumnIndex("image"));
				ByteArrayInputStream bitmapStream = new ByteArrayInputStream(tileBitmapByteArray);
				
				BITMAP_OPTIONS.inPreferredConfig = Bitmap.Config.RGB_565;
				Bitmap tileBitmap = BitmapFactory.decodeStream(bitmapStream, null, BITMAP_OPTIONS);
		
				int row = c.getInt(c.getColumnIndex("row"));
				int col = c.getInt(c.getColumnIndex("col"));
				int zoom = c.getInt(c.getColumnIndex("zoom"));
				
				for (Tile t : tiles) {
					if (t.zoom == zoom && t.mapX == col && t.mapY == row) {
						tileBitmapMap.put(t, tileBitmap);
						break;
					}
				}	
			}
			
			c.close();
			
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return tileBitmapMap;
	}
	
	public static Bitmap getTile(Tile tile) {
		
		Bitmap tileBitmap = null;
				
		if (mDbHelper == null)
			return tileBitmap;
		
		try {

			Cursor c = mDbHelper.get(TABLE_TILE_NAME, 
					getOsmTileSQLRequest(tile.mapY, tile.mapX, tile.zoom), 
					COLUMN_IMAGE, null, "1"); 
			
			if (c.moveToNext()) {
			
				byte[] tileBitmapByteArray = c.getBlob(c.getColumnIndex("image"));
				ByteArrayInputStream bitmapStream = new ByteArrayInputStream(tileBitmapByteArray);
				
				BITMAP_OPTIONS.inPreferredConfig = Bitmap.Config.RGB_565;
				tileBitmap = BitmapFactory.decodeStream(bitmapStream, null, BITMAP_OPTIONS);
		
			}
			c.close();
			
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		}

		//Log.i("getTile", "get tile from DB    tile = " + tile.mapX+tile.mapY+tile.zoom );
		
		return tileBitmap;
	}
	
	public static int getMinZoomLevel() {
		
		if (mMapMinZoomLevel > -1)
			return mMapMinZoomLevel;
		
		OsmDatabaseHelper osmDb = mDbHelper;	
		
		String sql = "select value from preferences where name ='map.minZoom' " ;
		
		Cursor c = osmDb.query(sql);
		
		if (c.moveToNext()) {
			try {
				mMapMinZoomLevel = Integer.parseInt(c.getString(0));
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		c.close();
		
		return mMapMinZoomLevel;
	}
	
	public static boolean hasTile(Tile tile) {
		if (getTileId(tile) != null)
			return true;
		return false;
	}
	
	public static String getTileId(Tile tile) {
		String tileId = null;
		String[] columnsSelected = {"tilekey"};
		
		OsmDatabaseHelper osmDb = mDbHelper;		
		if (osmDb == null)
			return tileId;
		
		Cursor c = osmDb.get(TABLE_TILE_NAME, 
				getOsmTileSQLRequest(tile.mapY, tile.mapX, tile.zoom),
				columnsSelected, null, "1");
		if (c.moveToNext()) {

			tileId = c.getString(c.getColumnIndex("tilekey"));
		}
		
		c.close();

		return tileId;
	}
	
	public static void insertTile(final Tile tile, byte[] bitmapData) {
		
		//long time = Calendar.getInstance().getTimeInMillis();
		try{
			
			boolean tileAlreadyInDb = false;
			
			OsmDatabaseHelper osmDb = mDbHelper;
			if (osmDb == null)
				return;
			
			String[] columnsSelected = {"tilekey"};
			
		    Cursor c = osmDb.get(TABLE_TILE_NAME, 
					getOsmTileSQLRequest(tile.mapY, tile.mapX, tile.zoom), 
					columnsSelected, null, "1");
				
			if (c.getCount() > 0) {
				//Log.i("getTile", "x= " +tile.mapX+ " y=" +tile.mapY+ " zoom=" + tile.zoom + " mapTypeId=" + tile.mapTypeId + " ALREADY IN DB!!");
				tileAlreadyInDb = true;					
			}
			
			if (!tileAlreadyInDb) {
				// INSERT
				ContentValues tileValues = new ContentValues();
				tileValues.put("row", tile.mapY);  // OSM format: row -> Y
				tileValues.put("col", tile.mapX);  // OSM format: col -> X
				tileValues.put("zoom", tile.zoom);
				//tileValues.put("mapTypeId", tile.mapTypeId);
				tileValues.put("image", bitmapData);
				//tileValues.put("creationDate", DateUtil.longToSqlDateFormat(
				//		Calendar.getInstance().getTimeInMillis()));
				
				osmDb.insert(TABLE_TILE_NAME, tileValues);	
				//Log.i("getTile", "x= " +tile.mapX+ " y=" +tile.mapY+ " zoom=" + tile.zoom + " mapTypeId=" + tile.mapTypeId + " INSERTED!!");
			}
			
			//Log.i("insertTile", "INSERTED time=" + (Calendar.getInstance().getTimeInMillis() - time) + "ms");
			
			c.close();

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private static String getOsmTileSQLRequest(int row, int col, int zoom) {
		return "row=" + row + " AND col=" + col + " AND zoom=" + zoom ;
	}
	
	public static Tile getTileFromCursor(Cursor c) {
		int row = c.getInt(c.getColumnIndex("row"));
		int col= c.getInt(c.getColumnIndex("col"));
		int zoom = c.getInt(c.getColumnIndex("zoom"));
		//int mapTypeId = c.getInt(c.getColumnIndex("mapTypeId"));

		return new Tile( col,row , zoom);
	}
	
	/**
	 * Delete tiles above a certain limit (first in first out)
	 * @param limit in Mb
	 */
	public static void deleteTilesAboveLimitThread(final int limit) {
		
		final int limitKb = limit * 1024; //put limit in Kb 
		
		Thread t = new Thread() {
			@Override
			public void run() {
				
				long time = Calendar.getInstance().getTimeInMillis();
				
				OsmDatabaseHelper db = mDbHelper;
				if (db == null)
					return;
				
				String sql = "SELECT tilekey FROM "+TABLE_TILE_NAME +
								" WHERE tilekey NOT IN " +
								"(SELECT tilekey FROM "+MapTileEntity.TABLE_TILE_ENTITY_NAME+" GROUP BY tilekey) " ;
								//"ORDER BY creationDate ASC;"; REMOVED from this version due to change in schema
				Cursor c = db.query(sql);
				
				if (c == null)
					return;
				
				int tilesSizeKb = Tile.AVERAGE_TILE_SIZE * c.getCount();
				
				if (tilesSizeKb > limitKb) {
					
					int nbFilesToDeleted = (tilesSizeKb - limitKb) / Tile.AVERAGE_TILE_SIZE;
					
					db.mDb.beginTransaction();
					try {

						while (c.moveToNext()) {
							
							db.delete(TABLE_TILE_NAME, "tilekey=" + c.getInt(c.getColumnIndex("tilekey")));
							
							if (nbFilesToDeleted <= 0)
								break;
							
							nbFilesToDeleted--;
						}
						
						db.mDb.setTransactionSuccessful();
					} finally {
						db.mDb.endTransaction();
					}	
				}
				
				c.close();
				
				Log.i("deleteTilesAboveLimitThread", "deleteTilesAboveLimitThread time =" + (Calendar.getInstance().getTimeInMillis() - time) + "ms");
			}
		};
		t.start();
	}
	
	public static void deleteTilesWithNoEntity() {
		String sql = "DELETE FROM "+TABLE_TILE_NAME+" " +
				"WHERE tilekey NOT IN (SELECT tilekey FROM "+MapTileEntity.TABLE_TILE_ENTITY_NAME+" GROUP BY tilekey);";
		
		OsmDatabaseHelper osmDb = mDbHelper;
		if (osmDb == null)
			return;
		
		osmDb.mDb.execSQL(sql);
	}

	public static int[] getMinMaxZoomLevelForTiles(List<Tile> tiles) {
		int minMaxZoom[] = new int[2];
		minMaxZoom[0] = 18;
		minMaxZoom[1] = 0;
		for (Tile tile : tiles) {
			if (tile.zoom < minMaxZoom[0])
				minMaxZoom[0] = tile.zoom;
			if (tile.zoom > minMaxZoom[1]);
				minMaxZoom[1] = tile.zoom;
		}
		return minMaxZoom;
	}
}
