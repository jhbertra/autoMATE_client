package com.automate.client.managers.packet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

import com.automate.client.AutoMateService;
import com.automate.client.managers.connectivity.IConnectionManager;
import com.automate.client.packet.services.PacketReceiveService;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class IncomingPacketListenerThread extends Thread {

	private AutoMateService mService;

	private Socket mSocket;

	private ArrayList<Socket> socketQueue;

	private boolean mCancelled;

	public IncomingPacketListenerThread(AutoMateService service) {
		super("Incoming packet listener.");
		this.mService = service;
		socketQueue = new ArrayList<Socket>();
	}

	public void queueSocket(Socket socket) {
		if(mCancelled) throw new IllegalStateException("Cannot queue a new socket after listen thread cancelled.");
		synchronized (socketQueue) {
			socketQueue.add(socket);
			socketQueue.notify();
		}
	}

	@Override
	public void run() {
		while(!mCancelled) {
			try {
				synchronized (socketQueue) {
					if(socketQueue.isEmpty()) {
						socketQueue.wait();
					}
					mSocket = socketQueue.remove(0);
				}
				while(true) {
					String line;
					BufferedReader reader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
					StringBuilder sb = new StringBuilder();
					while(!(line = reader.readLine()).equals("\0")) {
						if(line.equals("EOF")) {
							mSocket.close();
							Log.d(getClass().getName(), "Using next socket.");
							break;
						}
						sb.append(line);
						sb.append('\n');
					}
					if(mSocket.isClosed()) break;
					Intent service = new Intent(mService, PacketReceiveService.class);
					service.putExtra(PacketReceiveService.MESSAGE, sb.toString());
					mService.startService(service);
				}
			} catch(NullPointerException e) {
				Log.d(getClass().getName(), "Using next socket.");
			} catch(Exception e) {
				//mService.getManager(IConnectionManager.class).disconnect();
				try {
					mSocket.close();
				} catch (IOException e1) {}
			}
		}
	}

	public void cancel() {
		if(mSocket != null) {
			try {
				mCancelled = true;
				mSocket.close();
			} catch (IOException e) {}
		}
	}

}
