package com.automate.client.views;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences.Editor;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.automate.client.R;
import com.automate.client.messaging.MessagingService;
import com.automate.client.messaging.PacketSentListener;
import com.automate.client.messaging.handlers.AuthenticationListener;

public abstract class AbstractAuthenticationService extends Service implements AuthenticationListener, PacketSentListener {

	private final IBinder mBinder = new AbstractAuthenticationServiceBinder();
	public static final String MESSENGER = "messenger";
	public static final int AUTHENTICATION_SUCCESSFUL = Activity.RESULT_FIRST_USER;
	public static final int AUTHENTICATION_FAILED = Activity.RESULT_FIRST_USER + 1;
	protected MessagingService.Api mMessagingServiceApi;
	private Messenger mMessenger;

	private ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			AbstractAuthenticationService.this.mMessagingServiceApi = null;
			AbstractAuthenticationService.this.mMessagingServiceApi.removeAuthenticationListener(AbstractAuthenticationService.this);
		}
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(this.getClass().getName(), "Bound to MessagingService.");
			AbstractAuthenticationService.this.mMessagingServiceApi = ((MessagingService.MessagingServiceBinder)service).getApi();
			AbstractAuthenticationService.this.mMessagingServiceApi.addAuthenticationListener(AbstractAuthenticationService.this);
		}
	};
	
	/* (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		unbindService(connection);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder ;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(getClass().getName(), "Starting RegistrationService.");
		this.mMessenger = intent.getParcelableExtra(MESSENGER);
		bindService(new Intent(this, MessagingService.class), connection, Context.BIND_AUTO_CREATE);
		return Service.START_NOT_STICKY;
	}

	@Override
	public void onPacketSent(int packetId) {
		
	}

	@Override
	public void onSendIoException(int packetId) {
		Log.d(getClass().getName(), "Packet sent IOException " + packetId);
		Message message = new Message();
		message.what = AUTHENTICATION_FAILED;
		message.obj = "Error connecting to server.";
		try {
			mMessenger.send(message);
		} catch (Exception e) {
			Log.e(getClass().getName(), "Authentication message could not be sent.", e);
		}
		handleError();
	}

	@Override
	public void onSendNoServerAddress(int packetId) {
		Log.d(getClass().getName(), "Packet no server address " + packetId);
		handleError();
	}

	@Override
	public void onSendNoServerPort(int packetId) {
		Log.d(getClass().getName(), "Packet no server port " + packetId);
		handleError();
	}

	@Override
	public void onSendError(int packetId) {
		Log.d(getClass().getName(), "Packet send error " + packetId);
		Message message = new Message();
		message.what = AUTHENTICATION_FAILED;
		message.obj = "Unknown error.";
		try {
			mMessenger.send(message);
		} catch (Exception e) {
			Log.e(getClass().getName(), "Authentication message could not be sent.", e);
		}
	}

	@Override
	public void onAuthenticated(String sessionKey, String username) {
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
		handleError();
	}

	public void handleError() {
		String prefsKey = getResources().getString(R.string.prefs_credentials);
		Editor editor = getSharedPreferences(prefsKey, MODE_PRIVATE).edit();
		editor.remove(getResources().getString(R.string.prefs_credentials_password));
		editor.commit();
	}
	
	public class AbstractAuthenticationServiceBinder extends Binder {
		public AbstractAuthenticationService getService() {
			return AbstractAuthenticationService.this;
		}
	}

}