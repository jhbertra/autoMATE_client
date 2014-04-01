package com.automate.client.managers.command;

import java.util.List; 

import com.automate.client.managers.IManager;
import com.automate.client.managers.connectivity.ConnectionListener;
import com.automate.client.managers.messaging.MessageListener;
import com.automate.client.managers.node.NodeListener;
import com.automate.protocol.models.CommandArgument;

public interface ICommandManager extends IManager<CommandListener>, MessageListener, NodeListener, ConnectionListener, CommandListener {
	
	public List<Command> getCommandList(long nodeId);

	public void setCommandList(List<Command> commandList, long nodeId);

	long sendCommand(Command command, long nodeId, List<CommandArgument<?>> args);

	public long getNodeIdForCommandId(long commandId);

	public String getCommandNameForCommandId(long commandId);
}
