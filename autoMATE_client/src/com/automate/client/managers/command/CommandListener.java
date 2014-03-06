package com.automate.client.managers.command;

import com.automate.client.managers.IListener;

public interface CommandListener extends IListener {

	public void onCommandIssued(long nodeId, long commandId);
	
	public void onCommandLost(long nodeId, long commandId);
	
	public void onCommandSuccess(long nodeId, long commandId);
	
	public void onCommandFailure(long nodeId, long commandId);
	
}
