package com.automate.client.managers.connectivity;

import com.automate.client.AutoMateService;
import com.automate.client.AutoMateService.AutoMateServiceBinder;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class DisconnectService extends Service implements ServiceConnection {

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
		bindService(service, this, flags);
		return START_NOT_STICKY;
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		((AutoMateServiceBinder)service).getService().getManager(IConnectionManager.class).disconnect();
	}
	@Override
	public void onServiceDisconnected(ComponentName name) {}

}
