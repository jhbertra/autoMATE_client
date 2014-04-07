package com.automate.client.managers.bluetooth;

import android.bluetooth.BluetoothDevice;

import com.automate.client.managers.IListener;

public interface BluetoothListener extends IListener {

	public abstract void onBluetoothUnavailable();
	
	public abstract void onBluetoothDisabled();
	
	public abstract void onBluetoothDiscovery();
	
	public abstract void onBluetoothDiscoveryFinished();
	
	public abstract void onDeviceDisvocered(BluetoothDevice device);

	public abstract void onConnecting(BluetoothDevice device);
	
	public abstract void onConnectFailed(BluetoothDevice device);
	
	public abstract void onConnected(BluetoothDevice device);
	
	public abstract void onDisconnected();
	
}
