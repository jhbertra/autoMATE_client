package com.automate.client.messaging.handlers;

import java.util.List;

import com.automate.client.managers.node.NodeListener;
import com.automate.client.messaging.handlers.IMessageHandler; 
import com.automate.protocol.Message;
import com.automate.protocol.client.ClientProtocolParameters;
import com.automate.protocol.models.Node;
import com.automate.protocol.server.ServerProtocolParameters;
import com.automate.protocol.server.messages.ServerNodeListMessage;

public class NodeListMessageHandler implements IMessageHandler<ServerNodeListMessage, Void> {
	
	private NodeListener mListener;
	
	public NodeListMessageHandler(NodeListener mListener) {
		this.mListener = mListener;
	}

	@Override
	public Message<ClientProtocolParameters> handleMessage(int majorVersion, int minorVersion, ServerNodeListMessage message, Void params) {
		List<Node> nodes = message.nodes;
		mListener.afterNodeListDownload(nodes);
		return null;
	}

}
