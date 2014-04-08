package com.automate.client.messaging.handlers;

import com.automate.client.managers.pairing.IPairingManager;
import com.automate.protocol.Message;
import com.automate.protocol.client.ClientProtocolParameters;
import com.automate.protocol.server.messages.ServerNodeRegistrationMessage;

public class NodeRegistrationMessageHandler implements IMessageHandler<ServerNodeRegistrationMessage, Void> {

	private IPairingManager manager;
	
	public NodeRegistrationMessageHandler(IPairingManager pairingManager) {
		this.manager = pairingManager;
	}

	@Override
	public Message<ClientProtocolParameters> handleMessage(int majorVersion, int minorVersion, ServerNodeRegistrationMessage message, Void params) {
		if(message.nodeId == -1) {
			this.manager.notifyPairingFailure();
		} else {
			this.manager.notifyPairingSuccess(message.nodeId, message.password);
		}
		return null;
	}
	
}
