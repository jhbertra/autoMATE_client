package com.automate.client.managers.packet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import com.automate.client.AutoMateService;
import com.automate.client.managers.connectivity.IConnectionManager;
import com.automate.client.packet.services.PacketReceiveService;

import android.content.Intent;

public class IncomingPacketListenerThread extends Thread {

	private AutoMateService mService;

	private Socket mSocket;

	private final Object socketLock = new Object();

	private boolean mCancelled;

	public IncomingPacketListenerThread(AutoMateService service) {
		super("Incoming packet listener.");
		this.mService = service;
	}

	public void newSocketAvailable(Socket socket) {
		if(mCancelled) throw new IllegalStateException("Cannot queue a new socket after listen thread cancelled.");
		synchronized (socketLock) {
			if(mSocket == null) {
				this.mSocket = socket;
				socketLock.notify();
			}
		}
	}

	@Override
	public void run() {
		while(!mCancelled) {
			try {
				synchronized (socketLock) {
					if(mSocket == null) {
						try {
							socketLock.wait();
						} catch (InterruptedException e) {}
					}
				}
				while(true) {
					String line;
					BufferedReader reader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
					StringBuilder sb = new StringBuilder();
					while(!(line = reader.readLine()).equals("\0")) {
						sb.append(line);
						sb.append('\n');
					}
					Intent service = new Intent(mService, PacketReceiveService.class);
					service.putExtra(PacketReceiveService.MESSAGE, sb.toString());
					mService.startService(service);
				}
			} catch(Exception e) {
				mService.getManager(IConnectionManager.class).disconnect();
				if(mSocket != null && !mSocket.isClosed()) {
					try {
						mSocket = null;
						mSocket.close();
					} catch (IOException e1) {}
				}
			}
		}
	}

	public void cancel() {
		if(mSocket != null) {
			try {
				mCancelled = true;
				mSocket = null;
				mSocket.close();
			} catch (IOException e) {}
		}
	}
	
	public void onDisconnected() {
		if(mSocket != null && !mSocket.isClosed()) {
			try {
				Socket socketTemp = mSocket;
				mSocket = null;
				socketTemp.close();
			} catch (IOException e) {}
		}
	}
}
