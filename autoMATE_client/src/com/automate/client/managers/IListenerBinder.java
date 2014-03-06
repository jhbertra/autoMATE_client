package com.automate.client.managers;

public interface IListenerBinder <T extends IListener> {

	public void bind(T listener);
	public void unbind(T listener);	
	
}
