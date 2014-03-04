package com.automate.client;

import java.util.HashMap;

import com.automate.client.authentication.AuthenticationManager;
import com.automate.client.messaging.PacketReceivedListener;
import com.automate.client.messaging.handlers.AuthenticationMessageHandler;
import com.automate.client.messaging.handlers.IMessageHandler;
import com.automate.client.messaging.managers.IMessageManager;
import com.automate.client.messaging.managers.IPacketSentManager;
import com.automate.client.messaging.managers.MessageManager;
import com.automate.client.messaging.managers.PacketReceivedManager;
import com.automate.client.messaging.managers.PacketSentManager;
import com.automate.protocol.IncomingMessageParser;
import com.automate.protocol.Message;
import com.automate.protocol.Message.MessageType;
import com.automate.protocol.MessageSubParser;
import com.automate.protocol.client.messages.ClientAuthenticationMessage;
import com.automate.protocol.server.ServerProtocolParameters;
import com.automate.protocol.server.subParsers.ServerAuthenticationMessageSubParser;
import com.automate.protocol.server.subParsers.ServerClientStatusUpdateMessageSubParser;
import com.automate.protocol.server.subParsers.ServerCommandMessageSubParser;
import com.automate.protocol.server.subParsers.ServerNodeListMessageSubParser;
import com.automate.protocol.server.subParsers.ServerPingMessageSubParser;
import com.automate.protocol.server.subParsers.ServerWarningMessageSubParser;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class AutoMateService extends Service {

	private AutoMateServiceBinder mBinder;
	
	private boolean started;

	private HashMap<Class<? extends IManager>, IManager> managers;

	private String serverAddress;

	private String serverPort;

	private int bindPort;

	private int majorVersion;

	private int minorVersion;
	
	/* (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(getClass().getName(), "Creating AutoMateService.");
		mBinder = new AutoMateServiceBinder();
		initManagers();
	}
	
	private void initManagers() {
		managers = new HashMap<Class<? extends IManager>, IManager>();
		
		IncomingMessageParser<ServerProtocolParameters> incomingMessageParser = getIncomingMessageParser();
		
		HashMap<MessageType, IMessageHandler<? extends Message<ServerProtocolParameters>, ?>> handlers = getMessageHandlers();
		
		PacketReceivedManager packetReceivedManager = new PacketReceivedManager(incomingMessageParser, handlers);
		IPacketSentManager packetSentManager = new PacketSentManager();
		IMessageManager messageManager = new MessageManager(this, packetReceivedManager, packetSentManager, serverAddress, 
				serverPort, bindPort, majorVersion, minorVersion);
		
		managers.put(PacketReceivedManager.class, packetReceivedManager);
		managers.put(IPacketSentManager.class, packetSentManager);
		managers.put(AuthenticationManager.class, 
				new AuthenticationManager(this, (AuthenticationMessageHandler) handlers.get(MessageType.AUTHENTICATION), messageManager));
	}

	HashMap<MessageType, IMessageHandler<? extends Message<ServerProtocolParameters>, ?>> getMessageHandlers() {
		HashMap<MessageType, IMessageHandler<? extends Message<ServerProtocolParameters>, ?>> handlers =
				new HashMap<Message.MessageType, IMessageHandler<? extends Message<ServerProtocolParameters>,?>>();
		return handlers;
	}

	IncomingMessageParser<ServerProtocolParameters> getIncomingMessageParser() {
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
		// start managers
		/*
		startService(new Intent(this, MessagingService.class));
		bindService(new Intent(this, MessagingService.class), new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {
			}
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(AutoMateService.this.getClass().getName(), "Bound to MessagingService.");
				mMessagingServiceApi = ((MessagingServiceBinder) service).getApi();
				SharedPreferences preferences = getSharedPreferences(getResources().getString(R.string.prefs_credentials), MODE_PRIVATE);
				String username = preferences.getString(getResources().getString(R.string.prefs_credentials_username), null);
				String password = preferences.getString(getResources().getString(R.string.prefs_credentials_password), null);
				String currentSessionKey = mMessagingServiceApi.getSessionKey();
				if(username != null && password != null && currentSessionKey == null) {
					ClientAuthenticationMessage message = new ClientAuthenticationMessage(mMessagingServiceApi.getProtocolParameters(), 
							username, password);
					mMessagingServiceApi.sendMessage(message);
				}
			}
		}, BIND_AUTO_CREATE);
		 */
		started = true;
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
	
	public <T extends IManager> T getManager(Class<T> managerClass) {
		return (T) managers.get(managerClass);
	}
	
	public class AutoMateServiceBinder extends Binder {
		
		public AutoMateService getService() {
			return AutoMateService.this;
		}
		
	}

}
