package com.android.lib.map.osm;

public class Tile {
	
	public final static int TILE_SIZE = 256;
	public static final int AVERAGE_TILE_SIZE = 35; // in Kb
	
	public int mapX;
	public int mapY;
	public int offsetX;
	public int offsetY;
	public int zoom;
	public String key;
	public int mapTypeId;
	public byte[] bitmap;
	
	public Tile() {
		
	}
	
	public Tile(int x, int y, int z) {
		mapX = x;
		mapY = y;
		zoom = z;
		key = z + "/" + x + "/" + y + ".png";
	}

	public Tile(int x, int y, int z, int aMapTypeId) {
		mapX = x;
		mapY = y;
		zoom = z;
		key = z + "/" + x + "/" + y + ".png";
		mapTypeId = aMapTypeId;
	}
	
	public Tile(Tile tile) {
		mapX = tile.mapX;
		mapY = tile.mapY;
		offsetX = tile.offsetX;
		offsetY = tile.offsetY;
		zoom = tile.zoom;
		key = tile.key;
		mapTypeId = tile.mapTypeId;
		bitmap = tile.bitmap;
	}
}
