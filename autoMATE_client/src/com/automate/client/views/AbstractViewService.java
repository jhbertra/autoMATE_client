package com.automate.client.views;

import com.automate.client.AutoMateService;
import com.automate.client.AutoMateService.AutoMateServiceBinder;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Messenger;

public abstract class AbstractViewService extends Service {

	public static final String MESSENGER = "messenger";

	protected Messenger mMessenger;
	protected AutoMateService mAutoMateService;
	
	private ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			AbstractViewService.this.onServiceDisconnected();
			AbstractViewService.this.mAutoMateService = null;
		}
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			AbstractViewService.this.mAutoMateService = ((AutoMateServiceBinder)service).getService();
			AbstractViewService.this.onServiceConnected();
		}
	};
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mMessenger = intent.getParcelableExtra(MESSENGER);
		if(mMessenger == null) {
			throw new NullPointerException("messenger was null.");
		}
		bindService(new Intent(this, AutoMateService.class), connection, BIND_AUTO_CREATE);
		return START_NOT_STICKY;
	}
	
	@Override
	public void onDestroy() {
		unbindService(connection);
		onServiceDisconnected();
		mAutoMateService = null;
		super.onDestroy();
	}

	protected abstract void onServiceConnected();
	
	protected abstract void onServiceDisconnected();

}
