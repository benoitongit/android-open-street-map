package com.android.lib.map.osm;

import java.util.ArrayList;
import java.util.List;

import com.android.lib.map.osm.models.MapTile;

import android.os.Handler;


public class TilesDownloader {

	private RemoteTileLoader mRemoteTileLoader;
	
	public TilesDownloader(Handler handler) {
		mRemoteTileLoader = new RemoteTileLoader(handler, 0);
	}
	
	public static List<Tile> getTilesForBoundaryBox(int mapTypeId, int minZoom, int maxZoom, final double north, final double south, 
			final double east, final double west) {
		
		List<Tile> tiles = new ArrayList<Tile>();
		
		for(int z = minZoom; z <= maxZoom; z++){
            final Tile upperLeft = Projection.getMapTileFromCoordinates(north, west, z);
            final Tile lowerRight = Projection.getMapTileFromCoordinates(south, east, z);
            
            for(int x = upperLeft.mapX; x <= lowerRight.mapX; x++){
                for(int y = upperLeft.mapY; y <= lowerRight.mapY; y++){
                	
                	tiles.add(new Tile(x, y, z, mapTypeId));
                	
                }
            }
		}
		return tiles;
	}

	public static int getNbTilesForBoundaryBox(int minZoom, int maxZoom, final double north, final double south, 
			final double east, final double west) {
		
		int count = 0;
		
		for(int z = minZoom; z <= maxZoom; z++){
            final Tile upperLeft = Projection.getMapTileFromCoordinates(north, west, z);
            final Tile lowerRight = Projection.getMapTileFromCoordinates(south, east, z);
            
            for(int x = upperLeft.mapX; x <= lowerRight.mapX; x++){
                for(int y = upperLeft.mapY; y <= lowerRight.mapY; y++){
                	
                	count++;
                	
                }
            }
		}
		return count;
	}
	
	
  /**
   * Check if tiles are in Db, if not download them 
   * @param Tiles list
   * @return number of tiles added for download
   */
	public int download(List<Tile> tiles) {
		int tileAdded = 0;
		for (Tile tile : tiles) {
        	if (!MapTile.hasTile(tile)) {
        		mRemoteTileLoader.queueTileRequest(tile);
        		tileAdded++;
        	}
		}
		return tileAdded;
	}	
}
