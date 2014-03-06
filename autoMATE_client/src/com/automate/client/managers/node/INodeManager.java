package com.automate.client.managers.node;

import com.automate.client.managers.IManager;
import com.automate.client.managers.connectivity.ConnectionListener;
import com.automate.client.managers.messaging.MessageListener;
import com.automate.protocol.models.Node;

/**
 * Interface for the node manager singleton.
 * @author jamie.bertram
 *
 */
public interface INodeManager extends IManager<NodeListener>, NodeListener, MessageListener, ConnectionListener {

	/**
	 * Queries the node by the specified nodeId.
	 * @param nodeId the id of the node requested.
	 * @return the {@link Node} that the id identifies.  null if the nodeId does not identify a node.
	 */
	public Node getNodeById(long nodeId);
	
	public void startNodeListDownload();
	
}
