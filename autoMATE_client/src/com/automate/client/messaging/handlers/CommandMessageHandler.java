package com.automate.client.messaging.handlers;

import com.automate.client.managers.command.CommandListener;
import com.automate.client.managers.command.ICommandManager;
import com.automate.protocol.Message;
import com.automate.protocol.client.ClientProtocolParameters;
import com.automate.protocol.server.messages.ServerClientCommandMessage;

public class CommandMessageHandler implements IMessageHandler<ServerClientCommandMessage, Void> {

	private ICommandManager mCommandManager;
	private final String invalidIdMessage;
	private final String serverErrorMessage;
	private final String nodeNotOwnedMessage;
	private final String nodeOfflineMessage;
	private final String duplicateIdMessage;
	private final String invalidArgsMessage;
	private final String invalidCommandMessage;
	
	public CommandMessageHandler(ICommandManager mCommandManager, String invalidIdMessage,
			String serverErrorMessage, String nodeNotOwnedMessage,
			String nodeOfflineMessage, String duplicateIdMessage,
			String invalidArgsMessage, String invalidCommandMessage) {
		this.mCommandManager = mCommandManager;
		this.invalidIdMessage = invalidIdMessage;
		this.serverErrorMessage = serverErrorMessage;
		this.nodeNotOwnedMessage = nodeNotOwnedMessage;
		this.nodeOfflineMessage = nodeOfflineMessage;
		this.duplicateIdMessage = duplicateIdMessage;
		this.invalidArgsMessage = invalidArgsMessage;
		this.invalidCommandMessage = invalidCommandMessage;
	}

	@Override
	public Message<ClientProtocolParameters> handleMessage(int majorVersion, int minorVersion, ServerClientCommandMessage message, Void params) {
		long nodeId = mCommandManager.getNodeIdForCommandId(message.commandId);
		switch(message.responseCode) {
		case 200:
			mCommandManager.onCommandSuccess(nodeId, message.commandId);
			break;
		case 400:
			mCommandManager.onCommandFailure(nodeId, message.commandId, invalidIdMessage);
			break;
		case 401:
			mCommandManager.onCommandFailure(nodeId, message.commandId, invalidCommandMessage);
			break;
		case 402:
			mCommandManager.onCommandFailure(nodeId, message.commandId, invalidArgsMessage);
			break;
		case 403:
			mCommandManager.onCommandFailure(nodeId, message.commandId, duplicateIdMessage);
			break;
		case 404:
			mCommandManager.onCommandFailure(nodeId, message.commandId, nodeOfflineMessage);
			break;
		case 405:
			mCommandManager.onCommandFailure(nodeId, message.commandId, nodeNotOwnedMessage);
			break;
		case 500:
			mCommandManager.onCommandFailure(nodeId, message.commandId, serverErrorMessage);
			break;
		}
		return null;
	}

}
