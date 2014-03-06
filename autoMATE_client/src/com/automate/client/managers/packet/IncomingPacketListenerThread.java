package com.automate.client.managers.packet;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;

import com.automate.client.messaging.services.PacketReceiveService;
import com.automate.client.messaging.services.PacketReceiveService.PacketReceiveBinder;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class IncomingPacketListenerThread extends Thread {

	private int mBindPort;
	
	private ExecutorService threadpool;

	private Context mContext;

	private ServerSocket serverSocket;
	
	public IncomingPacketListenerThread() {
		super("Incoming packet listener.");
	}
	
	public IncomingPacketListenerThread(Context context, int bindPort) {
		this.mBindPort = bindPort;
		this.mContext = context;
	}

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(mBindPort);
			while(true) {
				final Socket socket = serverSocket.accept();
				Intent service = new Intent(mContext, PacketReceiveService.class);
				mContext.startService(service);
				mContext.bindService(service, new ServiceConnection() {
					@Override
					public void onServiceDisconnected(ComponentName name) {}
					@Override
					public void onServiceConnected(ComponentName name, IBinder service) {
						((PacketReceiveBinder)service).getApi().startDownload(socket);
					}
				}, Context.BIND_AUTO_CREATE);
			}
		} catch(SocketException e) {
			
		} catch (IOException e) {
			Log.e(getClass().getName(), "Fatal exception in Incoming packet listener thread.", e);
			try {
				serverSocket.close();
			} catch (IOException e2) {}
		}
	}

	public void cancel() {
		if(serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {}
		}
	}
	
}
