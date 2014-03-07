package com.automate.client.messaging.handlers;

import com.automate.client.managers.connectivity.IConnectionManager;
import com.automate.protocol.Message;
import com.automate.protocol.server.ServerProtocolParameters;
import com.automate.protocol.server.messages.ServerPingMessage;

public class PingMessageHandler implements IMessageHandler<ServerPingMessage, Void> {

	private IConnectionManager mConnectionManager;
	
	public PingMessageHandler(IConnectionManager mConnectionManager) {
		this.mConnectionManager = mConnectionManager;
	}

	@Override
	public Message<ServerProtocolParameters> handleMessage(int majorVersion, int minorVersion, ServerPingMessage message, Void params) {
		return null;
	}

}
