package com.automate.client.messaging.handlers;

import com.automate.client.managers.status.StatusListener;
import com.automate.protocol.Message;
import com.automate.protocol.server.ServerProtocolParameters;
import com.automate.protocol.server.messages.ServerClientStatusUpdateMessage;

public class StatusUpdateMessageHandler implements IMessageHandler<ServerClientStatusUpdateMessage, Void> {

	private StatusListener mListener;
	
	public StatusUpdateMessageHandler(StatusListener mListener) {
		this.mListener = mListener;
	}

	@Override
	public Message<ServerProtocolParameters> handleMessage(int majorVersion, int minorVersion, ServerClientStatusUpdateMessage message, Void params) {
		return null;
	}

}
