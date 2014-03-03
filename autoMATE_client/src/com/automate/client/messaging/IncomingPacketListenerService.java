package com.automate.client.messaging;

import java.io.IOException;
import java.net.ServerSocket;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class IncomingPacketListenerService extends Service {

	public static final String BIND_PORT = "bind port";

	private int bindPort;
	
	private ServerSocket serverSocket;

	private IBinder mBinder = new IncomingPacketListenerServiceBinder();
	
	/* (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		if(serverSocket != null && serverSocket.isBound()) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder ;
	}
	
	/* (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(final Intent intent, int flags, int startId) {
		Log.d(getClass().getName(), "Starting IncomingPacketListenerService.");
		new Thread(new Runnable() {
			@Override
			public void run() {
				bindPort = intent.getIntExtra(BIND_PORT, 6300);
				try {
					serverSocket = new ServerSocket(bindPort);
					while(true) {
						new PacketReceiveServiceFactory(serverSocket.accept(), IncomingPacketListenerService.this).startService();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
		return Service.START_NOT_STICKY;
	}

	public class IncomingPacketListenerServiceBinder extends Binder {
		
		public IncomingPacketListenerService getService() {
			return IncomingPacketListenerService.this;
		}
		
	}
	
}
