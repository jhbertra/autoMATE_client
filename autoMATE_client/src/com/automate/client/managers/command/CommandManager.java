package com.automate.client.managers.command;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import android.content.Context;
import android.util.Log;

import com.automate.client.R;
import com.automate.client.managers.IListener;
import com.automate.client.managers.ManagerBase;
import com.automate.client.managers.connectivity.IConnectionManager;
import com.automate.client.managers.messaging.IMessageManager;
import com.automate.client.managers.node.INodeManager;
import com.automate.client.managers.packet.PacketSentListener;
import com.automate.protocol.Message;
import com.automate.protocol.client.ClientProtocolParameters;
import com.automate.protocol.client.messages.ClientCommandMessage;
import com.automate.protocol.models.CommandArgument;
import com.automate.protocol.models.Node;
import com.automate.protocol.server.ServerProtocolParameters;

public class CommandManager extends ManagerBase<CommandListener> implements ICommandManager {

	private IConnectionManager mConnectivityManager;
	private INodeManager mNodeManager;
	private IMessageManager mMessageManager;
	
	private boolean mConnected;
	private final Object lock = new Object();
	private int nextCommandId;
	private HashMap<Long, List<Command>> mCommandLists = new HashMap<Long, List<Command>>();
	private HashMap<Long, ClientCommandMessage> mCommandHistory = new HashMap<Long, ClientCommandMessage>();
	
	private CommandListParser mCommandListParser = new CommandListParser();
	private Context mContext;
	
	public CommandManager(Context context, IConnectionManager connectivityManager, INodeManager nodeManager, IMessageManager messageManager) {
		super(CommandListener.class);
		this.mContext = context;
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
		if(mCommandLists.containsKey(node.id)) return;
		Document commandListDocument =  getCommandListDocument(node.commandListUrl);
		if(commandListDocument == null) {
			Log.e(getClass().getName(), "Could not load command list file from " + node.commandListUrl);
			return;
		}
		List<Command> commandList = mCommandListParser.parse(commandListDocument);
		if(commandList == null) {
			Log.e(getClass().getName(), "Could not parse command list from " + node.commandListUrl);
			return;
		}
		setCommandList(commandList, node.id);
	}

	private Document getCommandListDocument(String commandListUrl) {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
				+ "<CommandList>"
				+ "	<Command name=\"Power On\">"
				+ "		<StatusValueCondition status=\"Power On\" type=\"boolean\" value=\"false\" default=\"true\"/>"
				+ "	</Command>"
				+ "	<Command name=\"Power Off\">"
				+ "		<StatusValueCondition status=\"Power On\" type=\"boolean\" value=\"true\" default=\"false\"/>"
				+ "	</Command>"
				+ "	<Command name=\"Set Speed\">"
				+ "		<Argument name=\"speed\" type=\"string\">"
				+ "			<EnumRange>"
				+ "				<Value>Low</Value>"
				+ "				<Value>Medium</Value>"
				+ "				<Value>High</Value>"
				+ "			</EnumRange>"
				+ "		</Argument>"
				+ "		<StatusValueCondition status=\"Power On\" type=\"boolean\" value=\"true\" default=\"false\"/>"
				+ "	</Command>"
				+ "</CommandList>";
		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes()));
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void onNodesAdded(List<Node> nodes) {
		for(Node node : nodes) {
			onNodeAdded(node);
		}
	}

	@Override
	public void onNodeRemoved(Node node) {
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
	public void onCommandFailure(long nodeId, long commandId, String failureMessage) {
		synchronized (mListeners) {
			for(CommandListener listener : mListeners) {
				listener.onCommandFailure(nodeId, commandId, failureMessage);
			}
		}
	}

	@Override
	public long sendCommand(Command command, final long nodeId, List<CommandArgument<?>> args) {
		long commandId = 0;
		synchronized (lock) {
			commandId = nextCommandId ++;
		}
		final long finalCommandId = commandId;
		ClientCommandMessage message = new ClientCommandMessage(mMessageManager.getProtocolParameters(), 
				nodeId, command.name, commandId, args);
		mCommandHistory.put(commandId, message);
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
		return commandId;
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
	}

	@Override
	protected void performInitialUpdate(CommandListener listener) {}


	@Override
	public long getNodeIdForCommandId(long commandId) {
		ClientCommandMessage message = mCommandHistory.get(commandId);
		if(message != null) {			
			return message.nodeId;
		} else {
			return -1;
		}
	}

	@Override
	public String getCommandNameForCommandId(long commandId) {
		ClientCommandMessage message = mCommandHistory.get(commandId);
		if(message != null) {			
			return message.name;
		} else {
			return null;
		}
	}

}
