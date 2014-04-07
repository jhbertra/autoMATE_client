package com.automate.client.managers.discovery;

import com.automate.client.managers.IManager;
import com.automate.client.managers.bluetooth.BluetoothListener;

public interface IDiscoveryManager extends IManager<DiscoveryListener>, DiscoveryListener, BluetoothListener {

	public void scan();
	
}
