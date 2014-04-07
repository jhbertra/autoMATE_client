package com.automate.client.managers.bluetooth;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class ConnectTask implements Runnable {

	private BluetoothDevice mDevice;
	private ConnectionCallback mCallback;
	
	public ConnectTask(BluetoothDevice device, ConnectionCallback connectedCallback) {
		this.mDevice = device;
		this.mCallback = connectedCallback;
	}

	@Override
	public void run() {
		try {
			BluetoothSocket socket = mDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
			socket.connect();
			mCallback.onConnected(socket);
		} catch (IOException e) {
			Log.e(getClass().getName(), "Error connecting to bluetooth device.", e);
			mCallback.onConnectFailed();
		}
	}
	
	public interface ConnectionCallback {
		
		public void onConnected(BluetoothSocket socket);

		void onConnectFailed();
		
	}

}
