package com.automate.client.managers.status;

import java.util.List;

import com.automate.client.managers.IManager;
import com.automate.client.managers.connectivity.ConnectionListener;
import com.automate.client.managers.messaging.MessageListener;
import com.automate.client.managers.node.NodeListener;
import com.automate.protocol.models.Status;

public interface IStatusManager extends IManager<StatusListener>, StatusListener, ConnectionListener, NodeListener, MessageListener {

	public List<Status<?>> getStatuses(long nodeId);
	
	public void requestStatusUpdate(long nodeId);
	
}
