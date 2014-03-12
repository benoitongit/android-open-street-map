package com.android.lib.map.osm.helpers;

import java.io.File;

import android.content.Context;


public class OsmDatabaseHelper extends CustomDatabaseHelper {
	
	private File mDbFile;
		
	
	public OsmDatabaseHelper(Context context) {
		super(context);
	}
	
	public boolean openOrCreateDatabase() {
		if (mDbFile != null)
			return super.openOrCreateDatabase(mContext, mDbFile);
	
		return false;
	}
	
	public boolean openDatabase() {
		if (mDbFile.exists())
			return super.openDatabase(mContext, mDbFile);
		
		return false;
	}
	
	public void setDatabaseFile(File dbFile) {
		mDbFile = dbFile;
	}
	
	public File getDatabaseFile() {
		return mDbFile;
	}
	
}
