package com.automate.client;

import java.util.HashMap;

import com.automate.client.managers.IListener;
import com.automate.client.managers.IManager;
import com.automate.client.managers.authentication.AuthenticationManager;
import com.automate.client.managers.authentication.IAuthenticationManager;
import com.automate.client.managers.command.CommandManager;
import com.automate.client.managers.command.ICommandManager;
import com.automate.client.managers.connectivity.ConnectionManager;
import com.automate.client.managers.connectivity.IConnectionManager;
import com.automate.client.managers.messaging.IMessageManager;
import com.automate.client.managers.messaging.MessageListener;
import com.automate.client.managers.messaging.MessageManager;
import com.automate.client.managers.node.INodeManager;
import com.automate.client.managers.node.NodeManager;
import com.automate.client.managers.packet.IPacketManager;
import com.automate.client.managers.packet.IncomingPacketListenerThread;
import com.automate.client.managers.packet.PacketManager;
import com.automate.client.managers.security.ISecurityManager;
import com.automate.client.managers.security.SecurityManager;
import com.automate.client.managers.status.IStatusManager;
import com.automate.client.managers.status.StatusManager;
import com.automate.client.managers.warning.IWarningManager;
import com.automate.client.managers.warning.WarningManager;
import com.automate.client.messaging.handlers.*;
import com.automate.protocol.IncomingMessageParser;
import com.automate.protocol.Message;
import com.automate.protocol.Message.MessageType;
import com.automate.protocol.MessageSubParser;
import com.automate.protocol.client.ClientProtocolParameters;
import com.automate.protocol.server.ServerProtocolParameters;
import com.automate.protocol.server.subParsers.*;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class AutoMateService extends Service implements MessageListener {

	private AutoMateServiceBinder mBinder;
	
	private boolean started;

	private ConnectionParameters parameters;

	private Managers managers;
	
	private int majorVersion;

	private int minorVersion;

	private HashMap<MessageType, IMessageHandler> handlers;
	
	/* (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(getClass().getName(), "Creating AutoMateService.");
		mBinder = new AutoMateServiceBinder();
		initSubsystems();
	}
	
	private void initSubsystems() {
		IncomingMessageParser<ServerProtocolParameters> parser = getIncomingMessageParser();
		IncomingPacketListenerThread listenerThread = new IncomingPacketListenerThread(this);
		
		ISecurityManager securityManager = new SecurityManager();
		IConnectionManager connectionManager = new ConnectionManager(this);
		IPacketManager packetManager = new PacketManager(this, listenerThread, "10.11.106.74", "6300", 6300, securityManager);
		IMessageManager messageManager = new MessageManager(packetManager, connectionManager, parser, majorVersion, minorVersion);
		IAuthenticationManager authenticationManager = new AuthenticationManager(this, messageManager, connectionManager);
		INodeManager nodeManager = new NodeManager(messageManager, connectionManager);
		IStatusManager statusManager = new StatusManager(messageManager, connectionManager, nodeManager);
		IWarningManager warningManager = new WarningManager(connectionManager, nodeManager, messageManager);
		ICommandManager commandManager = new CommandManager(connectionManager, nodeManager, messageManager);
		
		this.managers = new Managers(authenticationManager, 
				commandManager, 
				connectionManager, 
				messageManager, 
				nodeManager, 
				packetManager, 
				securityManager, 
				statusManager, 
				warningManager);
		
		createMessageHandlers();
	}

	private void createMessageHandlers() {
		handlers = new HashMap<Message.MessageType, IMessageHandler>();
		handlers.put(MessageType.AUTHENTICATION, new AuthenticationMessageHandler(managers.authenticationManager));
		handlers.put(MessageType.NODE_LIST, new NodeListMessageHandler(managers.nodeManager));
		handlers.put(MessageType.PING, new PingMessageHandler(managers.connectionManager, managers.messageManager));
		handlers.put(MessageType.COMMAND_NODE, new CommandMessageHandler(managers.commandManager));
		handlers.put(MessageType.STATUS_UPDATE_NODE, new StatusUpdateMessageHandler(managers.statusManager));
		handlers.put(MessageType.WARNING_NODE, new WarningMessageHandler(managers.warningManager));
	}

	private IncomingMessageParser<ServerProtocolParameters> getIncomingMessageParser() {
		HashMap<String, MessageSubParser<? extends Message<ServerProtocolParameters>, ServerProtocolParameters>> subParsers = 
				new HashMap<String, MessageSubParser<? extends Message<ServerProtocolParameters>,ServerProtocolParameters>>();
		subParsers.put(MessageType.AUTHENTICATION.toString(), new ServerAuthenticationMessageSubParser());
		subParsers.put(MessageType.NODE_LIST.toString(), new ServerNodeListMessageSubParser());
		subParsers.put(MessageType.COMMAND_NODE.toString(), new ServerCommandMessageSubParser());
		subParsers.put(MessageType.PING.toString(), new ServerPingMessageSubParser());
		subParsers.put(MessageType.STATUS_UPDATE_NODE.toString(), new ServerClientStatusUpdateMessageSubParser());
		subParsers.put(MessageType.WARNING_NODE.toString(), new ServerWarningMessageSubParser());
		IncomingMessageParser<ServerProtocolParameters> incomingMessageParser = new IncomingMessageParser<ServerProtocolParameters>(subParsers);
		return incomingMessageParser;
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(getClass().getName(), "Starting AutoMateService.");
		if(started) return START_STICKY;
		
		managers.securityManager.start();
		managers.connectionManager.start();
		managers.packetManager.start();
		managers.messageManager.start();
		managers.authenticationManager.start();
		managers.nodeManager.start();
		managers.statusManager.start();
		managers.warningManager.start();
		managers.commandManager.start();

		managers.messageManager.bind(this);
		started = true;
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
	
	public <T extends IManager<?>> T getManager(Class<T> managerClass) {
		return managers.getManager(managerClass);
	}
	
	public class AutoMateServiceBinder extends Binder {
		
		public AutoMateService getService() {
			return AutoMateService.this;
		}
		
	}

	@Override
	public void onBind(Class<? extends IListener> listenerClass) {}
	@Override
	public void onUnbind(Class<? extends IListener> listenerClass) {}

	@Override
	public void onMessageReceived(Message<ServerProtocolParameters> message) {
		 Message<ClientProtocolParameters> responseMessage = handlers.get(message.getMessageType()).handleMessage(majorVersion, minorVersion, message, getParams(message));
		 if(message != null) {
			 this.managers.messageManager.sendMessage(responseMessage);
		 }
	}

	private Object getParams(Message<ServerProtocolParameters> message) {
		return null;
	}

	@Override
	public void onMessageSent(Message<ClientProtocolParameters> message) {}

}
