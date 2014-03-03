package com.automate.client;

import java.util.ArrayList;

import android.util.Log;

public abstract class ListenerBinding <T extends IListener> {

	protected ArrayList<T> listeners = new ArrayList<T>();
	
	public void addListener(T listener) {
		if(listener != null) {
			listeners.add(listener);
		} else {
			Log.w(getClass().getName(), "Attempt to add null listener object.");
		}
	}
	
	public void removeListener(T listener) {
		if(listener != null) {
			listeners.remove(listener);
		} else {
			Log.w(getClass().getName(), "Attempt to remove null listener object.");
		}
	}
	
}
