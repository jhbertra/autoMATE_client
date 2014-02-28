package com.automate.client.messaging;

import com.automate.client.messaging.IncomingPacketListenerService.IncomingPacketListenerServiceBinder;
import com.automate.protocol.Message;
import com.automate.protocol.client.ClientProtocolParameters;
import com.automate.util.xml.XmlFormatException;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class MessagingService extends Service implements ServiceConnection {

	private final IBinder mBinder = new MessagingServiceBinder();
	private final Api api = new Api();
	private String serverAddress;
	private int serverPort;
	private MessageReceiveReceiver receiveReceiver;
	private String bindPort;
	private IncomingPacketListenerService incomingPacketListenerService;
	
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
		
		MessagingService.Api getApi() {
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
		
	}

}
