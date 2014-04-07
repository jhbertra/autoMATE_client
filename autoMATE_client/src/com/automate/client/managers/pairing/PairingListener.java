package com.automate.client.managers.pairing;

import java.util.List;

import com.automate.client.managers.IListener;

public interface PairingListener extends IListener {

	public void onDevicesDiscovered(List<DeviceInfo> device);
	
	public void onScanningForDevices();
	
	public void onPairing(DeviceInfo device);
	
	public void onNewDevicePaired(DeviceInfo device);
	
	public void onPairingFailure(DeviceInfo device);
	
}
