package com.automate.client.managers;

public interface IListener {

	public void onBind(Class<? extends IListener> listenerClass);
	
	public void onUnbind(Class<? extends IListener> listenerClass);
	
}
