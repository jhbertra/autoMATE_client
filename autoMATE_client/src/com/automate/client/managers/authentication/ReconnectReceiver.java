package com.automate.client.managers.authentication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReconnectReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent service = new Intent(context, ReconnectService.class);
		context.startService(service);
	}

}
