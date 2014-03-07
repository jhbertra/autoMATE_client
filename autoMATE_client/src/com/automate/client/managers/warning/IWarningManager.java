package com.automate.client.managers.warning;

import java.util.List;

import com.automate.client.managers.IListenerBinder;
import com.automate.client.managers.IManager;
import com.automate.client.managers.connectivity.ConnectionListener;
import com.automate.client.managers.messaging.MessageListener;
import com.automate.client.managers.node.NodeListener;
import com.automate.protocol.models.Warning;

public interface IWarningManager extends IManager<WarningListener>, WarningListener, ConnectionListener, NodeListener, MessageListener {

	public List<Warning> getActiveWarnings(long nodeId);
	
	public void dismissWarning(long nodeId, long warningId);
	
	public void dismissWarnings(long nodeId);
	
}
