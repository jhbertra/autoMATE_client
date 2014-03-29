package com.automate.client.managers.command;

import java.util.List; 

import com.automate.client.managers.IManager;
import com.automate.client.managers.connectivity.ConnectionListener;
import com.automate.client.managers.messaging.MessageListener;
import com.automate.client.managers.node.NodeListener;

public interface ICommandManager extends IManager<CommandListener>, MessageListener, NodeListener, ConnectionListener, CommandListener {
	
	public long sendCommand(Command command, long nodeId);
	
	public List<Command> getCommandList(long nodeId);

	public void setCommandList(List<Command> commandList, long nodeId);
	
}
