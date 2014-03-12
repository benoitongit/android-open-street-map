package com.android.lib.map.osm.utils;

import android.os.Handler;

public abstract class CountDownTimer {
	
    private long millisInFuture;
    private long countDownInterval;
    private boolean cancelled;
    
    public abstract void onFinish();
    public abstract void onTick();
    
    public CountDownTimer(long aMillisInFuture, long aCountDownInterval) {
    	this.millisInFuture = aMillisInFuture;
    	this.countDownInterval = aCountDownInterval;
    }
    
    public void start() {
    	cancelled = false;
        final Handler handler = new Handler();
        final Runnable counter = new Runnable() {
            @Override
			public void run() {
                if (millisInFuture <= 0 || cancelled) {
                	onFinish();
                } else {
                	onTick();
    				millisInFuture -= countDownInterval;
                    handler.postDelayed(this, countDownInterval);
                }
            }
        };
        handler.postDelayed(counter, countDownInterval);
    }

    public void cancel() {
    	cancelled = true;
    }
}
