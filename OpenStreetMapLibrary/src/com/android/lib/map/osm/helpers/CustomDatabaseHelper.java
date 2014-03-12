package com.android.lib.map.osm.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.android.lib.map.osm.R;

public class CustomDatabaseHelper {
		
	public SQLiteDatabase mDb;

	//private boolean mAlreadyTriedToOpenDb;
	protected Context mContext;
	
	
	public CustomDatabaseHelper(Context context) {
		//mAlreadyTriedToOpenDb = false;
		mContext = context;
	}

	public boolean openDatabase(Context context, File dbFile) {
		
		try {

			Log.i("SQLiteHelper", "Opening database at " + dbFile);
			mDb = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
			return true;
				
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public boolean openOrCreateDatabase(Context context, File dbFile) {
	
		try {

			if (dbFile.exists()) {
				Log.i("SQLiteHelper", "Opening database at " + dbFile);
				mDb = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
				return true;
				
				// Test if DB works properly
				//get(MapTile.TABLE_TILE_NAME, "tilekey");
				//---
				
				//if (DATABASE_VERSION > db.getVersion())
					//upgrade();
			} else {
				Log.i("SQLiteHelper", "Creating database at " + dbFile);
				mDb = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
				Log.i("SQLiteHelper", "Opened database at " + dbFile);
				upgradeFromFile(mDb, R.raw.sql_osm_maptile);
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}

	private void upgradeFromFile(SQLiteDatabase db, int ressourceId) {
		InputStream sqlFile = null;
		
		try {
			sqlFile = mContext.getResources().openRawResource(ressourceId);
		} catch (Resources.NotFoundException e) {
			e.printStackTrace();
			return;
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(sqlFile));
		String line = null;

		try {
			while ((line = br.readLine()) != null) {
				db.execSQL(line);
			}
		} catch (SQLException se) {
		} catch (IOException e) {
		}	
	}
	
	public void close() {
		if (mDb != null && mDb.isOpen())
			mDb.close();
	}
	
	public boolean isOpen() {
		if (mDb == null)
			return false;
		return mDb.isOpen();
	}
	
	public long insert(String table, ContentValues values)  {
		try {
			return mDb.insertOrThrow(table, null, values);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
    public Cursor get(String table, String select){
        Cursor cursor = mDb.query(true, table, null, select, null, null, null, null, null);
        return cursor;
    }

    public Cursor get(String table, String select, String[] columns, String orderBy){
        return mDb.query(true, table, columns, select, null, null, null, orderBy, null);
    }

    public Cursor get(String table, String select, String[] columns, String orderBy, String limit){
        return mDb.query(true, table, columns, select, null, null, null, orderBy, limit);
    }
    
    public Cursor get(String table, String select, String orderBy, String limit){
        return mDb.query(true, table, null, select, null, null, null, orderBy, limit);
    }

    public Cursor get(String table, String select, String limit){
        return mDb.query(true, table, null, select, null, null, null, null, limit);
    }
    
    public Cursor query(String sql) {
    	try {
    		Cursor cursor = mDb.rawQuery(sql, null);
    		return cursor;
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return null;
    }
    
    public int delete(String table, String where) {
        return mDb.delete(table, where, null);
    }

    public int update(String table,ContentValues values, String where) {
        return mDb.update(table, values, where, null);
    }
    
}
