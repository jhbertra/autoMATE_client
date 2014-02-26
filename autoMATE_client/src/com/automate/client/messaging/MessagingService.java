package com.automate.client.messaging;

import com.automate.protocol.Message;
import com.automate.protocol.client.ClientProtocolParameters;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class MessagingService extends Service {

	private final IBinder mBinder = new MessagingServiceBinder();
	private final Api api = new Api();
	
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		return Service.START_REDELIVER_INTENT;
	}

	public void sendMessage(Message<ClientProtocolParameters> message) {
		
	}
	
	public class MessagingServiceBinder extends Binder {
		
		MessagingService.Api getApi() {
			return MessagingService.this.api;
		}
		
	}
	
	public class Api {
		
		public void sendMessage(Message<ClientProtocolParameters> message) {
			MessagingService.this.sendMessage(message);
		}
		
	}

}
