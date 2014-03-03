package com.automate.client.messaging;

import java.util.HashMap;
import java.util.Hashtable;

import com.automate.client.R;
import com.automate.client.authentication.AuthenticationManager;
import com.automate.client.messaging.IncomingPacketListenerService.IncomingPacketListenerServiceBinder;
import com.automate.client.messaging.handlers.AuthenticationMessageHandler;
import com.automate.client.messaging.handlers.IMessageHandler;
import com.automate.protocol.IncomingMessageParser;
import com.automate.protocol.Message;
import com.automate.protocol.Message.MessageType;
import com.automate.protocol.MessageSubParser;
import com.automate.protocol.client.ClientProtocolParameters;
import com.automate.protocol.server.ServerProtocolParameters;
import com.automate.protocol.server.subParsers.ServerAuthenticationMessageSubParser;
import com.automate.util.xml.XmlFormatException;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences.Editor;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class MessagingService extends Service {

	private final IBinder mBinder = new MessagingServiceBinder();
	private final Api api = new Api();
	private String serverAddress = "192.168.0.23";
	private int serverPort = 6300;
	private MessageReceiveReceiver receiveReceiver;
	private String bindPort;
	private IncomingPacketListenerService incomingPacketListenerService;
	private String sessionKey;
	private int majorVersion;
	private int minorVersion;
	
	private MessageSendReceiver deliverReceiver;
	
	private int nextPacketId = 0;
	private final Object packetIdLock = new Object();
	
	private AuthenticationManager mAuthenticationManager;
	private PacketSentManager mPacketSentManager;
	private PacketReceivedManager mPacketReceivedManager;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	/*
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		HashMap<MessageType, IMessageHandler<? extends Message<ServerProtocolParameters>, ?>> handlers = 
				new HashMap<Message.MessageType, IMessageHandler<? extends Message<ServerProtocolParameters>,?>>();
		handlers.put(MessageType.AUTHENTICATION, new AuthenticationMessageHandler());

		HashMap<String, MessageSubParser<? extends Message<ServerProtocolParameters>, ServerProtocolParameters>> subParsers 
			= new HashMap<String, MessageSubParser<? extends Message<ServerProtocolParameters>,ServerProtocolParameters>>();
		subParsers.put(MessageType.AUTHENTICATION.toString(), new ServerAuthenticationMessageSubParser());
		IncomingMessageParser<ServerProtocolParameters> incomingMessageParser = new IncomingMessageParser<ServerProtocolParameters>(subParsers);
		
		this.mAuthenticationManager = new AuthenticationManager(this, 
				(AuthenticationMessageHandler) handlers.get(MessageType.AUTHENTICATION), this.api);
		this.mPacketSentManager = new PacketSentManager();
		this.mPacketReceivedManager = new PacketReceivedManager(incomingMessageParser, handlers);
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(getClass().getName(), "Starting MessagingService.");
		
		receiveReceiver = new MessageReceiveReceiver(mPacketReceivedManager);
		registerReceiver(receiveReceiver, new IntentFilter(PacketReceiveService.class.getName()));
		
		deliverReceiver = new MessageSendReceiver(mPacketSentManager);
		registerReceiver(deliverReceiver, new IntentFilter(PacketDeliveryService.class.getName()));
		
		Intent listenerIntent = new Intent(this, IncomingPacketListenerService.class);
		listenerIntent.putExtra(IncomingPacketListenerService.BIND_PORT, bindPort);
		startService(listenerIntent);
		bindService(listenerIntent, new ServiceConnection() {

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(MessagingService.this.getClass().getName(), "Bound to IncomingPacketListeningService.");
				incomingPacketListenerService = ((IncomingPacketListenerServiceBinder) service).getService();
			}
			
			@Override
			public void onServiceDisconnected(ComponentName name) {
				Intent listenerIntent = new Intent(MessagingService.this, IncomingPacketListenerService.class);
				listenerIntent.putExtra(IncomingPacketListenerService.BIND_PORT, bindPort);
				startService(listenerIntent);
			}
			
		}, Context.BIND_AUTO_CREATE);
		return Service.START_NOT_STICKY;
	}
	
	/* (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		incomingPacketListenerService.stopSelf();
		incomingPacketListenerService = null;
		this.mAuthenticationManager.onDestroy();
	}

	/**
	 * Sends a message to the server.
	 * @param message the message to send.
	 */
	public void sendMessage(Message<ClientProtocolParameters> message, PacketSentListener listener) {
		int packetId;
		synchronized (packetIdLock) {
			packetId = this.nextPacketId++;
		}
		Intent intent = new Intent(this, PacketDeliveryService.class);
		intent.putExtra(PacketDeliveryService.SERVER_ADDRESS, serverAddress);
		intent.putExtra(PacketDeliveryService.SERVER_PORT, serverPort);
		intent.putExtra(PacketDeliveryService.PACKET_ID, packetId);
		StringBuilder sb = new StringBuilder();
		try {
			message.toXml(sb, 0);
			intent.putExtra(PacketDeliveryService.DATA, sb.toString());
		} catch (XmlFormatException e) {
			Log.e(getClass().getName(), "Error formatting outgoing message.", e);
		}
		if(listener != null) {
			mPacketSentManager.addDeliveryListener(packetId, listener);
		}
		startService(intent);
	}
	
	public void sendMessage(Message<ClientProtocolParameters> message) {
		sendMessage(message, null);
	}
	
	public class MessagingServiceBinder extends Binder {
		
		public MessagingService.Api getApi() {
			return MessagingService.this.api;
		}
		
	}
	
	public class Api {
		
		public <T> T getManager(Class<T> managerClass) {
			if(managerClass.equals(AuthenticationManager.class)) {
				return (T) mAuthenticationManager;
			} else if(managerClass.equals(PacketSentManager.class)) {
				return (T) mPacketSentManager;
			}
			return null;
		}

		public void sendMessage(Message<ClientProtocolParameters> message) {
			MessagingService.this.sendMessage(message);
		}
		
		public void sendMessage(Message<ClientProtocolParameters> message, PacketSentListener listener) {
			MessagingService.this.sendMessage(message, listener);
		}

		public ClientProtocolParameters getProtocolParameters() {
			return new ClientProtocolParameters(majorVersion, minorVersion, sessionKey);
		}

		public String getSessionKey() {
			return sessionKey;
		}

		public void setSessionKey(String sessionKey) {
			MessagingService.this.sessionKey = sessionKey;
		}
		
	}

}
