package com.android.lib.map.osm.overlay;

import android.graphics.Point;
import android.graphics.drawable.Drawable;

import com.android.lib.map.osm.GeoPoint;

public class MapMarker {
	
	public static short BOUND_CENTER_BOTTOM = 1;
	public static short BOUND_CENTER = 2;
	public static short BOUND_CUSTOM = 3;
	public static short Z_INDEX_VERY_LOW = -2;
	public static short Z_INDEX_LOW = -1;
	public static short Z_INDEX_DEFAULT = 0;
	public static short Z_INDEX_HIGH = 1;
	public static short Z_INDEX_VERY_HIGH = 2;
	
	private String mTitle;
	private GeoPoint mCoordinate;
	private boolean mIsFocused;
	private Drawable mDrawable;
	private Drawable mDrawableFocused;
	private boolean mIsClickable;
	private short mZIndex;
	private int mPosition = 0;
	private short mDrawableBound = BOUND_CENTER_BOTTOM;
	private short mDrawableFocusedBound = BOUND_CENTER_BOTTOM;
	private int mAlpha = 255;
	private Object mTag;
	private Point mDrawableCustomBoundFocused;
	private Point mDrawableCustomBound;
	

	public MapMarker() {
		mIsFocused = false;
		mIsClickable = true;
		mZIndex = Z_INDEX_DEFAULT;
	}
	
	public String getTitle() {
		return mTitle;
	}


	public void setTitle(String title) {
		this.mTitle = title;
	}


	public GeoPoint getCoordinate() {
		return mCoordinate;
	}


	public void setCoordinate(GeoPoint coordinate) {
		this.mCoordinate = coordinate;
	}


	public boolean isFocused() {
		return mIsFocused;
	}


	public void setFocused(boolean isFocused) {
		this.mIsFocused = isFocused;
	}


	public Drawable getDrawable() {
		return mDrawable;
	}


	public void setDrawable(Drawable drawable) {
		this.mDrawable = drawable;
	}


	public Drawable getDrawableFocused() {
		return mDrawableFocused;
	}


	public void setDrawableFocused(Drawable drawableFocused) {
		this.mDrawableFocused = drawableFocused;
	}

	public void setClickable(boolean clickable) {
		mIsClickable = clickable;
	}
	
	public boolean isClickable() {
		return mIsClickable;
	}

	public void setZIndex(short zIndex) {
		mZIndex = zIndex;
	}
	
	public short getZIndex() {
		return mZIndex;
	}

	public void setPosition(int pos) {
		mPosition = pos;
	}
	
	public int getPosition() {
		return mPosition;
	}
	
	public void setDrawableBound(short bound) {
		mDrawableBound = bound;
	}

	public void setDrawableBound(Point customBoundOffset) {
		mDrawableBound = BOUND_CUSTOM;
		mDrawableCustomBound = customBoundOffset;
	}

	public Point getDrawableCustomBound() {
		return mDrawableCustomBound;
	}
	
	public void setDrawableFocusedBound(short bound) {
		mDrawableFocusedBound = bound;
	}

	public void setDrawableFocusedBound(Point customBoundOffset) {
		mDrawableFocusedBound = BOUND_CUSTOM;
		mDrawableCustomBoundFocused = customBoundOffset;
	}
	
	public Point getDrawableFocusedCustomBound() {
		return mDrawableCustomBoundFocused;
	}
	
	public short getDrawableBound() {
		return mDrawableBound;
	}

	public short getDrawableFocusedBound() {
		return mDrawableFocusedBound;
	}
	
	public void setAlpha(int alpha) {
		mAlpha = alpha;
	}
	
	public int getAlpha() {
		return mAlpha;
	}
	
	public Object getTag() {
		return mTag;
	}

	public void setTag(Object tag) {
		mTag = tag;
	}
		
}
