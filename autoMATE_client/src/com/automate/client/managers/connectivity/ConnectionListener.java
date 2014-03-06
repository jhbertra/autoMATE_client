package com.automate.client.managers.connectivity;

import com.automate.client.managers.IListener;


public interface ConnectionListener extends IListener {

	public void onConnecting();
	
	public void onConnected();
	
	public void onDisconnected();
	
}
