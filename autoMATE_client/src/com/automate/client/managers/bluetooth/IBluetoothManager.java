package com.automate.client.managers.bluetooth;

import android.bluetooth.BluetoothDevice;

import com.automate.client.managers.IManager;

public interface IBluetoothManager extends IManager<BluetoothListener>, BluetoothListener {
	
	public void startDiscovery();
	
	public void connect(BluetoothDevice device);
	
	public void disconnect();
	
	public void sendMessage(String message, MessageCallback callback);
	
	public interface MessageCallback {
		
		public void onResponse(String message);
		
	}
	
}
