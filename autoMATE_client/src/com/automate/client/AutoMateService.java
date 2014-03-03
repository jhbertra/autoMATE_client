package com.automate.client;

import com.automate.client.messaging.MessagingService;
import com.automate.client.messaging.MessagingService.MessagingServiceBinder;
import com.automate.protocol.client.messages.ClientAuthenticationMessage;

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
	
	private MessagingService.Api mMessagingServiceApi;
	private boolean started;
	
	/* (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(getClass().getName(), "Creating AutoMateService.");
		mBinder = new AutoMateServiceBinder();
	}
	
	/* (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(getClass().getName(), "Starting AutoMateService.");
		if(started) return START_STICKY;
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
		started = true;
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
	
	public class AutoMateServiceBinder extends Binder {
		
		public AutoMateService getService() {
			return AutoMateService.this;
		}
		
	}

}
