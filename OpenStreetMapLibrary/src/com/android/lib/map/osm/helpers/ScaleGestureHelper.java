package com.android.lib.map.osm.helpers;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;

public class ScaleGestureHelper  {

	private ScaleGestureDetector mScaleGestureDetector;
	private IScaleGestureListener mListener;
	
	
	public ScaleGestureHelper(Context context, IScaleGestureListener listener) {
		mListener = listener;
		mScaleGestureDetector = new ScaleGestureDetector(context, new MySimpleOnScaleGestureListener());
	}
	
	public class MySimpleOnScaleGestureListener extends SimpleOnScaleGestureListener {
				
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			if (mListener != null)
				mListener.onScale(detector.getCurrentSpan());
			return true;
		}
		
		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {
			super.onScaleEnd(detector);
			if (mListener != null)
				mListener.onScaleEnd();
		}
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		mScaleGestureDetector.onTouchEvent(event);
		return mScaleGestureDetector.isInProgress();
	}
	
	public interface IScaleGestureListener {
		public void onScale(float distance);
		public void onScaleEnd();
	}
}
