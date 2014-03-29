package com.automate.client.messaging.handlers;

import com.automate.client.managers.warning.WarningListener;
import com.automate.protocol.Message;
import com.automate.protocol.client.ClientProtocolParameters;
import com.automate.protocol.server.ServerProtocolParameters;
import com.automate.protocol.server.messages.ServerClientWarningMessage;

public class WarningMessageHandler implements IMessageHandler<ServerClientWarningMessage, Void> {

	private WarningListener mListener;
	
	public WarningMessageHandler(WarningListener mListener) {
		this.mListener = mListener;
	}

	@Override
	public Message<ClientProtocolParameters> handleMessage(int majorVersion, int minorVersion, ServerClientWarningMessage message, Void params) {
		return null;
	}

}
