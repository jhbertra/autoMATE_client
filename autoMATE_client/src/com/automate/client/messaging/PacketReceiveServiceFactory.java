package com.automate.client.messaging;

import java.net.Socket;

import com.automate.client.messaging.PacketReceiveService.PacketReceiveBinder;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class PacketReceiveServiceFactory implements ServiceConnection {

	private Socket socket;
	private Context context;

	public PacketReceiveServiceFactory(Socket socket, Context context) {
		this.socket = socket;
		this.context = context;
	}

	public void startService() {
		Intent intent = new Intent(context, PacketReceiveService.class);
		context.startService(intent);
		context.bindService(intent, this, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		PacketReceiveService.PacketReceiveBinder binder = (PacketReceiveBinder) service;
		binder.getApi().startDownload(socket);
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {/* Do nothing */}

}
