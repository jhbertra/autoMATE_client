package com.automate.client.messaging;

import java.util.HashMap;
import java.util.Hashtable;

import com.automate.client.R;
import com.automate.client.messaging.IncomingPacketListenerService.IncomingPacketListenerServiceBinder;
import com.automate.client.messaging.handlers.AuthenticationListener;
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

public class MessagingService extends Service implements ServiceConnection, AuthenticationListener {

	private final IBinder mBinder = new MessagingServiceBinder();
	private final Api api = new Api();
	private String serverAddress = "192.168.0.23";
	private int serverPort = 6300;
	private MessageReceiveReceiver receiveReceiver;
	private String bindPort;
	private IncomingPacketListenerService incomingPacketListenerService;
	private String sessionKey;
	
	private Hashtable<Integer, PacketSentListener> deliveryListeners = new Hashtable<Integer, PacketSentListener>();
	
	private HashMap<MessageType, IMessageHandler<? extends Message<ServerProtocolParameters>, ?>> handlers =
			new HashMap<Message.MessageType, IMessageHandler<? extends Message<ServerProtocolParameters>,?>>();
	private MessageSendReceiver deliverReceiver;
	
	private int nextPacketId = 0;
	private final Object packetIdLock = new Object();
	
	private IncomingMessageParser<ServerProtocolParameters> incomingMessageParser;
	
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
		handlers.put(MessageType.AUTHENTICATION, new AuthenticationMessageHandler());
		
		HashMap<String, MessageSubParser<? extends Message<ServerProtocolParameters>, ServerProtocolParameters>> subParsers 
			= new HashMap<String, MessageSubParser<? extends Message<ServerProtocolParameters>,ServerProtocolParameters>>();
		subParsers.put(MessageType.AUTHENTICATION.toString(), new ServerAuthenticationMessageSubParser());
		incomingMessageParser = new IncomingMessageParser<ServerProtocolParameters>(subParsers);
		
		this.api.addAuthenticationListener(this);
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(getClass().getName(), "Starting MessagingService.");
		
		receiveReceiver = new MessageReceiveReceiver(api);
		registerReceiver(receiveReceiver, new IntentFilter(PacketReceiveService.class.getName()));
		
		deliverReceiver = new MessageSendReceiver(api);
		registerReceiver(deliverReceiver, new IntentFilter(PacketDeliveryService.class.getName()));
		
		Intent listenerIntent = new Intent(this, IncomingPacketListenerService.class);
		listenerIntent.putExtra(IncomingPacketListenerService.BIND_PORT, bindPort);
		startService(listenerIntent);
		bindService(listenerIntent, this, Context.BIND_AUTO_CREATE);
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
		this.api.removeAuthenticationListener(this);
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
			deliveryListeners.put(packetId, listener);
		}
		startService(intent);
	}
	
	public void sendMessage(Message<ClientProtocolParameters> message) {
		sendMessage(message, null);
	}
	
	/**
	 * Called when a message is received from the server.
	 * @param packet the message that was received.
	 */
	public void onPacketReceived(String packet) {
		try {
			Message<ServerProtocolParameters> message = incomingMessageParser.parse(packet);
			IMessageHandler handler = handlers.get(message.getMessageType());
			Object args = null;
			switch (message.getMessageType()) {
			default:
				break;
			}
			handler.handleMessage(1, 0, message, args);
		} catch (Throwable t) {
			Log.e(getClass().getName(), "Error handling received packet.", t);
		}
	}
	
	public class MessagingServiceBinder extends Binder {
		
		public MessagingService.Api getApi() {
			return MessagingService.this.api;
		}
		
	}
	
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		Log.d(MessagingService.this.getClass().getName(), "Bound to IncomingPacketListeningService.");
		this.incomingPacketListenerService = ((IncomingPacketListenerServiceBinder) service).getService();
	}
	
	@Override
	public void onServiceDisconnected(ComponentName name) {
		Intent listenerIntent = new Intent(this, IncomingPacketListenerService.class);
		listenerIntent.putExtra(IncomingPacketListenerService.BIND_PORT, bindPort);
		startService(listenerIntent);
	}
	
	public class Api implements PacketSentListener, PacketReceivedListener {
		
		private int majorVersion;
		private int minorVersion;

		public void sendMessage(Message<ClientProtocolParameters> message) {
			MessagingService.this.sendMessage(message);
		}
		
		public void sendMessage(Message<ClientProtocolParameters> message, PacketSentListener listener) {
			MessagingService.this.sendMessage(message, listener);
		}

		@Override
		public void onPacketReceived(String packet) {
			MessagingService.this.onPacketReceived(packet);
		}

		@Override
		public void onEmptyPacketReceived() {
			
		}

		@Override
		public void onReceiveIoException() {
			
		}

		@Override
		public void onNoSocketProvided() {
			
		}

		@Override
		public void onReceiveError() {
			
		}

		public ClientProtocolParameters getProtocolParameters() {
			return new ClientProtocolParameters(majorVersion, minorVersion, sessionKey);
		}

		public void addAuthenticationListener(AuthenticationListener listener) {
			((AuthenticationMessageHandler)handlers.get(MessageType.AUTHENTICATION)).addListener(listener);
		}
		
		public void removeAuthenticationListener(AuthenticationListener listener) {
			((AuthenticationMessageHandler)handlers.get(MessageType.AUTHENTICATION)).removeListener(listener);
		}

		public String getSessionKey() {
			return sessionKey;
		}

		@Override
		public void onPacketSent(int packetId) {
			PacketSentListener listener = deliveryListeners.get(packetId);
			if(listener != null) {
				listener.onPacketSent(packetId);
			}
			deliveryListeners.remove(packetId);
		}

		@Override
		public void onSendIoException(int packetId) {
			PacketSentListener listener = deliveryListeners.get(packetId);
			if(listener != null) {
				listener.onSendIoException(packetId);
			}
			deliveryListeners.remove(packetId);
		}

		@Override
		public void onSendNoServerAddress(int packetId) {
			PacketSentListener listener = deliveryListeners.get(packetId);
			if(listener != null) {
				listener.onSendNoServerAddress(packetId);
			}
			deliveryListeners.remove(packetId);
		}

		@Override
		public void onSendNoServerPort(int packetId) {
			PacketSentListener listener = deliveryListeners.get(packetId);
			if(listener != null) {
				listener.onSendNoServerPort(packetId);
			}
			deliveryListeners.remove(packetId);
		}

		@Override
		public void onSendError(int packetId) {
			PacketSentListener listener = deliveryListeners.get(packetId);
			if(listener != null) {
				listener.onSendError(packetId);
			}
			deliveryListeners.remove(packetId);
		}
		
	}

	@Override
	public void onAuthenticated(String sessionKey, String username) {
		Log.d(getClass().getName(), "onAuthenticated(" + sessionKey + ", " + username + ")");
		this.sessionKey = sessionKey;
		String prefsKey = getResources().getString(R.string.prefs_credentials);
		Editor editor = getSharedPreferences(prefsKey, MODE_PRIVATE).edit();
		editor.putString(getResources().getString(R.string.prefs_credentials_username), username);
		editor.commit();
	}

	@Override
	public void onAuthenticationFailed(String failureMessage) {
	}

}
