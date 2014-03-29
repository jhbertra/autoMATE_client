package com.automate.client.managers.authentication;

import com.automate.client.AutoMateService;
import com.automate.client.AutoMateService.AutoMateServiceBinder;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class ReconnectService extends Service implements ServiceConnection {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Intent service = new Intent(this, AutoMateService.class);
		bindService(service, this, BIND_AUTO_CREATE);
		return START_NOT_STICKY;
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		((AutoMateServiceBinder)service).getService().getManager(IAuthenticationManager.class).reconnect();
		unbindService(this);
	}
	@Override
	public void onServiceDisconnected(ComponentName name) {}
	
}
