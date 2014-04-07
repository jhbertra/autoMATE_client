package com.automate.client.managers.discovery;

import java.util.List;

import com.automate.client.managers.IListener;

public interface DiscoveryListener extends IListener {

	public void onDevicesDiscovered(List<DeviceInfo> device);
	
	public void onScanningForDevices();
	
}
