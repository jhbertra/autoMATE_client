package com.automate.client.managers.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.automate.client.managers.IListener;
import com.automate.client.managers.ManagerBase;
import com.automate.client.managers.connectivity.IConnectionManager;
import com.automate.client.managers.messaging.IMessageManager;
import com.automate.client.managers.node.INodeManager;
import com.automate.client.managers.packet.PacketSentListener;
import com.automate.protocol.Message;
import com.automate.protocol.client.ClientProtocolParameters;
import com.automate.protocol.client.messages.ClientCommandMessage;
import com.automate.protocol.models.Node;
import com.automate.protocol.server.ServerProtocolParameters;

public class CommandManager extends ManagerBase<CommandListener> implements
		ICommandManager {

	private IConnectionManager mConnectivityManager;
	private INodeManager mNodeManager;
	private IMessageManager mMessageManager;
	
	private HashMap<Long, List<Command>> mCommands = new HashMap<Long, List<Command>>();
	
	private boolean mConnected;
	private final Object lock = new Object();
	private int nextCommandId;
	private HashMap<Long, List<Command>> mCommandLists;
	
	public CommandManager(IConnectionManager connectivityManager, INodeManager nodeManager, IMessageManager messageManager) {
		super(CommandListener.class);
		this.mConnectivityManager = connectivityManager;
		this.mNodeManager = nodeManager;
		this.mMessageManager = messageManager;
	}

	@Override
	public void onMessageReceived(Message<ServerProtocolParameters> message) {}
	@Override
	public void onMessageSent(Message<ClientProtocolParameters> message) {}
	@Override
	public void onBind(Class<? extends IListener> listenerClass) {}
	@Override
	public void onUnbind(Class<? extends IListener> listenerClass) {
	}
	@Override
	public void onNodeAdded(Node node) {
		mCommands.put(node.id, new ArrayList<Command>());
	}

	@Override
	public void onNodesAdded(List<Node> nodes) {
		for(Node node : nodes) {
			onNodeAdded(node);
		}
	}

	@Override
	public void onNodeRemoved(Node node) {
		mCommands.remove(node.id);
	}

	@Override
	public void onNodeListDownload() {}
	@Override
	public void onNodeListDownloadFailed() {}

	@Override
	public void afterNodeListDownload(List<Node> nodes) {
		onNodesAdded(nodes);
	}

	@Override
	public void onConnecting() {
		this.mConnected = false;
	}

	@Override
	public void onConnected() {
		synchronized (mCommands) {
			this.mConnected = true;
		}
		Set<Long> nodeIds = mCommands.keySet();
		for(Long nodeId : nodeIds) {
			Collection<Command> commands = mCommands.remove(nodeId);
			for(Command command : commands) {
				sendCommand(command, nodeId);
			}
		}
	}

	@Override
	public void onDisconnected() {
		this.mConnected = false;
	}

	@Override
	public void onCommandIssued(long nodeId, long commandId) {
		synchronized (mListeners) {
			for(CommandListener listener : mListeners) {
				listener.onCommandIssued(nodeId, commandId);
			}
		}
	}

	@Override
	public void onCommandLost(long nodeId, long commandId) {
		synchronized (mListeners) {
			for(CommandListener listener : mListeners) {
				listener.onCommandLost(nodeId, commandId);
			}
		}
	}

	@Override
	public void onCommandSuccess(long nodeId, long commandId) {
		synchronized (mListeners) {
			for(CommandListener listener : mListeners) {
				listener.onCommandSuccess(nodeId, commandId);
			}
		}
	}

	@Override
	public void onCommandFailure(long nodeId, long commandId) {
		synchronized (mListeners) {
			for(CommandListener listener : mListeners) {
				listener.onCommandFailure(nodeId, commandId);
			}
		}
	}

	@Override
	public void sendCommand(Command command, final long nodeId) {
		if(!mCommands.containsKey(nodeId)) return;
		synchronized (mCommands) {
			if(!mConnected) {
				mCommands.get(nodeId).add(command);
				return;
			}
		}
		long commandId = 0;
		synchronized (lock) {
			commandId = nextCommandId ++;
		}
		final long finalCommandId = commandId;
		ClientCommandMessage message = new ClientCommandMessage(mMessageManager.getProtocolParameters(), 
				nodeId, command.name, commandId, command.args);
		mMessageManager.sendMessage(message, new PacketSentListener() {
			@Override
			public void onUnbind(Class<? extends IListener> listenerClass) {}
			@Override
			public void onBind(Class<? extends IListener> listenerClass) {}
			@Override
			public void onSendNoServerPort(int packetId) {
				onCommandLost(nodeId, finalCommandId);
			}
			@Override
			public void onSendNoServerAddress(int packetId) {
				onCommandLost(nodeId, finalCommandId);				
			}
			@Override
			public void onSendIoException(int packetId) {
				onCommandLost(nodeId, finalCommandId);				
			}
			@Override
			public void onSendError(int packetId) {
				onCommandLost(nodeId, finalCommandId);
			}
			@Override
			public void onPacketSent(int packetId) {
				onCommandIssued(nodeId, finalCommandId);
			}
		});
	}

	@Override
	public List<Command> getCommandList(long nodeId) {
		return mCommandLists.get(nodeId);
	}

	@Override
	public void setCommandList(List<Command> commandList, long nodeId) {
		mCommandLists.put(nodeId, commandList);
	}

	@Override
	protected void unbindSelf() {
		this.mNodeManager.unbind(this);
		this.mMessageManager.unbind(this);
		this.mConnectivityManager.unbind(this);
	}

	@Override
	protected void bindSelf() {
		this.mNodeManager.bind(this);
		this.mMessageManager.bind(this);
		this.mConnectivityManager.bind(this);
	}

	@Override
	protected void setupInitialState() {
		this.mConnected = false;
	}

	@Override
	protected void teardown() {
		this.mCommands.clear();
	}

	@Override
	protected void performInitialUpdate(CommandListener listener) {}

}
