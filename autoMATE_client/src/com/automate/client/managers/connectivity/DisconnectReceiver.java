package com.automate.client.managers.connectivity;

import com.automate.client.AutoMateService;
import com.automate.client.AutoMateService.AutoMateServiceBinder;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class DisconnectReceiver extends BroadcastReceiver implements ServiceConnection {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent serviceIntent = new Intent(context, AutoMateService.class);
		context.bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		((AutoMateServiceBinder)service).getService().getManager(IConnectionManager.class).disconnect();
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		
	}

}
