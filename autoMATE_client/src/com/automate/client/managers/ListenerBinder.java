package com.automate.client.managers;

import java.util.ArrayList;

import android.util.Log;

public abstract class ListenerBinder <T extends IListener> implements IListenerBinder<T> {

	protected ArrayList<T> mListeners = new ArrayList<T>();
	protected boolean mBindAllowed;
	private final Class<T> mListenerClass;
	
	public ListenerBinder(Class<T> listenerClass) {
		this.mListenerClass = listenerClass;
	}
	
	public void bind(T listener) {
		if(mBindAllowed) {
			synchronized (mListeners) {
				if(listener != null) {
					mListeners.add(listener);
					performInitialUpdate(listener);
					listener.onBind(mListenerClass);
				} else {
					Log.w(getClass().getName(), "Attempt to add null listener object.");
				}
			}
		}
	}
	
	public void unbind(T listener) {
		if(mBindAllowed) {
			synchronized (mListeners) {
				if(listener != null) {
					mListeners.remove(listener);
					listener.onUnbind(mListenerClass);
				} else {
					Log.w(getClass().getName(), "Attempt to remove null listener object.");
				}
			}
		}
	}
	
	protected void unbindAll() {
		synchronized (mListeners) {
			for(T listener : mListeners) {
				unbind(listener);
			}
		}
	}
	
	protected abstract void performInitialUpdate(T listener);
	
}
