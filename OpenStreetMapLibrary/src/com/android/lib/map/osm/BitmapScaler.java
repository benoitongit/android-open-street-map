package com.android.lib.map.osm;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

public class BitmapScaler {
	
//	public static Bitmap scaleTo(Bitmap bitmap, int offsetX, int offsetY,float scaleFactor, int newWidth, int newHeight) {
//        
//		if (bitmap == null)
//			return bitmap;
//		
//		int width = bitmap.getWidth();
//        int height = bitmap.getHeight();
//
//        try {
//        	
//			Bitmap resizedBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.RGB_565);
//			Rect src = new Rect(0, 0, width, height);
//			Rect dest = new Rect(0, 0, newWidth, newHeight);
//			Canvas c = new Canvas(resizedBitmap);
//			c.drawBitmap(bitmap, src, dest, null);
//        	
//        	return resizedBitmap;
//        
//        } catch (OutOfMemoryError e) {
//        	e.printStackTrace();
//        } catch (Exception e) {
//        	e.printStackTrace();
//        }
//        
//		return null;
//	}
	
	public static Bitmap scaleTo(Bitmap bitmap, float scaleFactor, int xIncrement, int yIncrement) {
		
		if (bitmap == null)
			return bitmap;
				
        try {
    		
        	int width = (int) ((bitmap.getWidth() / 2) / scaleFactor);
            int height = (int) ((bitmap.getHeight() / 2) / scaleFactor);
            
			Bitmap resizedBitmap = Bitmap.createBitmap(Tile.TILE_SIZE, Tile.TILE_SIZE, Bitmap.Config.RGB_565);
			int x = width * xIncrement;
			int y = height * yIncrement;
			Rect src = new Rect(x, y, x + width, y + height);
			Rect dest = new Rect(0, 0, Tile.TILE_SIZE, Tile.TILE_SIZE);
			Canvas c = new Canvas(resizedBitmap);
			c.drawBitmap(bitmap, src, dest, null);
        	
        	return resizedBitmap;
        
        } catch (OutOfMemoryError e) {
        	e.printStackTrace();
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
		return null;
		
	}
	
	public static PipedOutputStream convertBitmapToStream(Bitmap src) 
    { 
	PipedOutputStream os=new PipedOutputStream();
    src.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, os); 
    return os; 
    } 
	public static Bitmap scaleToCompress(Bitmap bitmap,float scaleFactor) {
		
	    try{
	    PipedInputStream pin = new PipedInputStream(convertBitmapToStream(bitmap));  
	    return decodeFile(pin,scaleFactor);
	    }catch(Exception e ){
	    	e.printStackTrace();
	    }
	    return bitmap;
	}
	private static Bitmap decodeFile(PipedInputStream is,float scaleFactor){
	    try {
	        //Decode image size
	        BitmapFactory.Options o = new BitmapFactory.Options();
	        o.inJustDecodeBounds = true;
	        BitmapFactory.decodeStream(is,null,o);

	     
	        //Decode with inSampleSize
	        BitmapFactory.Options o2 = new BitmapFactory.Options();
	        o2.inSampleSize=(int)scaleFactor;
	        return BitmapFactory.decodeStream(is, null, o2);
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    return null;
	}
	
}
