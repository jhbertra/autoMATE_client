package com.automate.client;

import com.automate.client.AutoMateService.AutoMateServiceBinder;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class AutoMateClient extends Application {

	private AutoMateService mService;
	
	@Override
	public void onCreate() {
		bindService(new Intent(this, AutoMateService.class), new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {
			}
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				AutoMateClient.this.mService = ((AutoMateServiceBinder)service).getService();
				AutoMateClient.this.startService(new Intent(AutoMateClient.this, AutoMateService.class));
			}
		}, Context.BIND_AUTO_CREATE);
		super.onCreate();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		mService.stopSelf();
	}

}
