package com.android.lib.map.osm.helpers;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.FloatMath;
import android.view.MotionEvent;


public class ScaleGesturePreFroyoHelper {

	public final static int DRAG = 2;
	public final static int ZOOM = 1;
	public final static int NONE = 0;
	public int mMode = NONE;
	private float mOldDist = 1f;
	private final PointF start = new PointF();
	private final Matrix matrix = new Matrix();
	private final Matrix savedMatrix = new Matrix();
	private IScaleGesturePreFroyo mScaleGesture;
	private float mLastZoomDist;
	
	public final PointF mMid = new PointF();
	
	public ScaleGesturePreFroyoHelper(IScaleGesturePreFroyo scaleGesture) {
		mScaleGesture = scaleGesture;
	}
	
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	private void midPoint(PointF point, MotionEvent event) {
		if (event.getPointerCount() >= 2) {
			float x = event.getX(0) + event.getX(1);
			float y = event.getY(0) + event.getY(1);
			point.set(x / 2, y / 2);
		}
	}

	public boolean onTouchEvent(final MotionEvent event) {

		try {
		
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				savedMatrix.set(matrix);
				start.set(event.getX(), event.getY());
				mMode = DRAG;
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				mOldDist = spacing(event);
				if (mOldDist > 10f) {
					savedMatrix.set(matrix);
					midPoint(mMid, event);
					mMode = ZOOM;
					mScaleGesture.onScaleBegin(event);
				}
				break;
			case MotionEvent.ACTION_UP:
				if (mMode == ZOOM) {
					mMode = NONE;
					return true;
				}
				mMode = NONE;
				break;
			case MotionEvent.ACTION_POINTER_UP:
				if (mMode == ZOOM) {
					mScaleGesture.onScaleEnd(mOldDist, mLastZoomDist);
					return true;
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if (mMode == DRAG) {
					// ...
					matrix.set(savedMatrix);
					matrix.postTranslate(event.getX() - start.x, event.getY()
							- start.y);
				} else if (mMode == ZOOM) {
					
					float newDist = spacing(event);
					mScaleGesture.onScale(event, mOldDist, newDist);
					mLastZoomDist = newDist;
					
					if (newDist > 10f) {
						matrix.set(savedMatrix);
						float scale = newDist / mOldDist;
						matrix.postScale(scale, scale, mMid.x, mMid.y);
					}
					return true;
				}
				break;
			}
			
		} catch (IllegalArgumentException iae) {
			
		} catch (Exception e) {
			e.printStackTrace();
		}
			
		return false;
	}

	public interface IScaleGesturePreFroyo {
		public void onScaleBegin(MotionEvent event);
		public void onScale(MotionEvent event, float startDistance, float lastDistance);
		public void onScaleEnd(float startDistance, float lastDistance);
	}
}
