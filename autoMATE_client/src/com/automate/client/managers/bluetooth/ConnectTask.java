package com.automate.client.managers.bluetooth;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;
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
			UUID uuid = UUID.fromString("04c6093b-0000-1000-8000-00805f9b34fb");
			BluetoothSocket socket = mDevice.createInsecureRfcommSocketToServiceRecord(uuid);
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
