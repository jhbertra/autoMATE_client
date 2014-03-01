package com.automate.client.messaging;

import java.util.HashMap;

import com.automate.client.R;
import com.automate.client.messaging.IncomingPacketListenerService.IncomingPacketListenerServiceBinder;
import com.automate.client.messaging.handlers.AuthenticationListener;
import com.automate.client.messaging.handlers.AuthenticationMessageHandler;
import com.automate.client.messaging.handlers.IMessageHandler;
import com.automate.protocol.Message;
import com.automate.protocol.Message.MessageType;
import com.automate.protocol.client.ClientProtocolParameters;
import com.automate.protocol.server.ServerProtocolParameters;
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
	private String serverAddress;
	private int serverPort;
	private MessageReceiveReceiver receiveReceiver;
	private String bindPort;
	private IncomingPacketListenerService incomingPacketListenerService;
	private String sessionKey;
	
	private HashMap<MessageType, IMessageHandler<? extends Message<ServerProtocolParameters>, ?>> handlers =
			new HashMap<Message.MessageType, IMessageHandler<? extends Message<ServerProtocolParameters>,?>>();
	
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		receiveReceiver = new MessageReceiveReceiver(api);
		registerReceiver(receiveReceiver, new IntentFilter(PacketReceiveService.class.getName()));
		Intent listenerIntent = new Intent(this, IncomingPacketListenerService.class);
		listenerIntent.putExtra(IncomingPacketListenerService.BIND_PORT, bindPort);
		bindService(listenerIntent, this, Context.BIND_AUTO_CREATE);
		return Service.START_REDELIVER_INTENT;
	}
	
	/* (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		incomingPacketListenerService.stopSelf();
		incomingPacketListenerService = null;
	}

	/**
	 * Sends a message to the server.
	 * @param message the message to send.
	 */
	public void sendMessage(Message<ClientProtocolParameters> message) {
		Intent intent = new Intent(this, PacketDeliveryService.class);
		intent.putExtra(PacketDeliveryService.SERVER_ADDRESS, serverAddress);
		intent.putExtra(PacketDeliveryService.SERVER_PORT, serverPort);
		StringBuilder sb = new StringBuilder();
		try {
			message.toXml(sb, 0);
			intent.putExtra(PacketDeliveryService.DATA, sb.toString());
		} catch (XmlFormatException e) {
			Log.e(getClass().getName(), "Error formatting outgoing message.", e);
		}
		startService(intent);
	}
	
	/**
	 * Called when a message is received from the server.
	 * @param packet the message that was received.
	 */
	public void onPacketReceived(String packet) {
		//TODO implement
	}
	
	public class MessagingServiceBinder extends Binder {
		
		public MessagingService.Api getApi() {
			return MessagingService.this.api;
		}
		
	}
	
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		this.incomingPacketListenerService = ((IncomingPacketListenerServiceBinder) service).getService();
	}
	
	@Override
	public void onServiceDisconnected(ComponentName name) {
		Intent listenerIntent = new Intent(this, IncomingPacketListenerService.class);
		listenerIntent.putExtra(IncomingPacketListenerService.BIND_PORT, bindPort);
		startService(listenerIntent);
	}
	
	public class Api {
		
		private int majorVersion;
		private int minorVersion;

		public void sendMessage(Message<ClientProtocolParameters> message) {
			MessagingService.this.sendMessage(message);
		}
		
		public void onPacketReceived(String packet) {
			MessagingService.this.onPacketReceived(packet);
		}

		public void onEmptyPacketReceived() {
			
		}

		public void onReceiveIoException() {
			
		}

		public void onNoSocketProvided() {
			
		}

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
		
	}

	@Override
	public void onAuthenticated(String sessionKey, String username, String password) {
		this.sessionKey = sessionKey;
		String prefsKey = getResources().getString(R.string.prefs_credentials);
		Editor editor = getSharedPreferences(prefsKey, MODE_PRIVATE).edit();
		editor.putString(getResources().getString(R.string.prefs_credentials_username), username);
		editor.putString(getResources().getString(R.string.prefs_credentials_password), password);
		editor.commit();
	}

	@Override
	public void onAuthenticationFailed(String failureMessage) {
		// do nothing
	}

}
