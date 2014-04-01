package com.automate.client.messaging.handlers;

import com.automate.client.managers.connectivity.IConnectionManager;
import com.automate.client.managers.messaging.IMessageManager;
import com.automate.protocol.Message;
import com.automate.protocol.client.ClientProtocolParameters;
import com.automate.protocol.client.messages.ClientPingMessage;
import com.automate.protocol.server.ServerProtocolParameters;
import com.automate.protocol.server.messages.ServerPingMessage;

public class PingMessageHandler implements IMessageHandler<ServerPingMessage, Void> {

	private IConnectionManager mConnectionManager;
	private IMessageManager mMessageManager;
	
	public PingMessageHandler(IConnectionManager connectionManager, IMessageManager messageManager) {
		this.mConnectionManager = connectionManager;
		this.mMessageManager = messageManager;
	}

	@Override
	public Message<ClientProtocolParameters> handleMessage(int majorVersion, int minorVersion, ServerPingMessage message, Void params) {
		mConnectionManager.scheduleDisconnect(90000L);
		return new ClientPingMessage(mMessageManager.getProtocolParameters());
	}
}
