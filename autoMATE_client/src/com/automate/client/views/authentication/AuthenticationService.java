package com.automate.client.views.authentication;

import com.automate.client.messaging.MessagingService;
import com.automate.client.messaging.handlers.AuthenticationListener;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class AuthenticationService extends Service implements ServiceConnection, AuthenticationListener {

	public static final String MESSENGER = "messenger";

	public static final int AUTHENTICATION_SUCCESSFUL = 0;

	public static final int AUTHENTICATION_FAILED = 1;

	private final IBinder mBinder = new AuthenticationService.Binder();
	
	private MessagingService.Api mMessagingServiceApi;

	private Messenger mMessenger;
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public boolean signIn(String username, String password) {
		return false;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		this.mMessenger = intent.getParcelableExtra(MESSENGER);
		bindService(new Intent(this, MessagingService.class), this, Context.BIND_AUTO_CREATE);
		return Service.START_NOT_STICKY;
	}
	
	/* (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		unbindService(this);
	}



	public class Binder extends android.os.Binder {

		public AuthenticationService getService() {
			return AuthenticationService.this;
		}
		
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		this.mMessagingServiceApi = ((MessagingService.MessagingServiceBinder)service).getApi();
		this.mMessagingServiceApi.addAuthenticationListener(this);
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		this.mMessagingServiceApi = null;
		this.mMessagingServiceApi.removeAuthenticationListener(this);
	}

	@Override
	public void onAuthenticated(String sessionKey, String username, String password) {
		Message message = new Message();
		message.what = AUTHENTICATION_SUCCESSFUL;
		try {
			mMessenger.send(message);
		} catch (Exception e) {
			Log.e(getClass().getName(), "Authentication message could not be sent.", e);
		}
	}

	@Override
	public void onAuthenticationFailed(String failureMessage) {
		Message message = new Message();
		message.what = AUTHENTICATION_FAILED;
		message.obj = failureMessage;
		try {
			mMessenger.send(message);
		} catch (Exception e) {
			Log.e(getClass().getName(), "Authentication message could not be sent.", e);
		}
	}
	
}
