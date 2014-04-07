package com.automate.client.managers.pairing;

import android.bluetooth.BluetoothDevice;

public class DeviceInfo {

	public final long modelId;
	public final String deviceName;
	public final int maxVersionMajor;
	public final int maxVersionMinor;
	public final BluetoothDevice bluetoothdevice;
	
	public DeviceInfo(long modelId, String deviceName, int maxVersionMajor, int maxVersionMinor, BluetoothDevice device) {
		this.modelId = modelId;
		this.deviceName = deviceName;
		this.maxVersionMajor = maxVersionMajor;
		this.maxVersionMinor = maxVersionMinor;
		this.bluetoothdevice = device;
	}
}
