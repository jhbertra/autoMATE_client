package com.automate.client.managers.connectivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DisconnectReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent service = new Intent(context, DisconnectService.class);
		context.startService(service);
	}
	
}
