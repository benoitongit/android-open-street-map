/**
 * 
 */
package com.android.lib.map.osm;

import java.util.Stack;

public class RequestsQueue {
	
	private int mTileStackSizeLimit;
	private Stack<Tile> queue = new Stack<Tile>();
	private Object lock = new Object();
	int id;

	RequestsQueue(int id, int tileStackSizeLimit)
	{
		this.id = id;
		mTileStackSizeLimit = tileStackSizeLimit;
	}
	
	public void queue(Tile tile) 
	{
		synchronized (lock) 
		{
			if(tile != null && !contains(tile)) 
			{
				if (mTileStackSizeLimit > 0 && queue.size() > mTileStackSizeLimit)
					queue.remove(queue.lastElement());
				queue.push(tile);
			}
		}
	}

	public boolean contains(Tile tile)
	{
		synchronized (lock) 
		{
			for (Tile qTile : queue) {
				if (qTile.key != null && qTile.key.equals(tile.key))
					return true;
			}
			return false;
		}
	}
	
	public Tile dequeue() 
	{
		synchronized (lock) 
		{
			return queue.pop();
		}
	}

	public boolean hasRequest() 
	{
		synchronized (lock) 
		{
			return queue.size() != 0;
		}
	}

	public void clear() 
	{
		synchronized (lock) 
		{
			queue.clear();
		}
	}
	
	public int size() 
	{
		synchronized (lock) 
		{
			return queue.size();
		}
	}
}