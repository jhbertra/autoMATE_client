package com.automate.client.managers.connectivity;

import com.automate.client.managers.IManager;

public interface IConnectionManager extends IManager<ConnectionListener>, ConnectionListener {
	
	public void scheduleDisconnect(long milis);

	public void disconnect();
	
}
