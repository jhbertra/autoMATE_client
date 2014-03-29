package com.automate.client.messaging.handlers;

import com.automate.client.managers.command.CommandListener;
import com.automate.protocol.Message;
import com.automate.protocol.client.ClientProtocolParameters;
import com.automate.protocol.server.ServerProtocolParameters;
import com.automate.protocol.server.messages.ServerClientCommandMessage;

public class CommandMessageHandler implements IMessageHandler<ServerClientCommandMessage, Void> {

	private CommandListener mListener;
	
	public CommandMessageHandler(CommandListener mListener) {
		this.mListener = mListener;
	}

	@Override
	public Message<ClientProtocolParameters> handleMessage(int majorVersion, int minorVersion, ServerClientCommandMessage message, Void params) {
		return null;
	}

}
