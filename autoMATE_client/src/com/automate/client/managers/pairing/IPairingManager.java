package com.automate.client.managers.pairing;

import com.automate.client.managers.IManager;
import com.automate.client.managers.bluetooth.BluetoothListener;
import com.automate.protocol.models.Node;

public interface IPairingManager extends IManager<PairingListener>, PairingListener, BluetoothListener {

	public void scan();
	
	public void pair(DeviceInfo device);
	
	public void notifyPairingSuccess(long nodeId, String password);
	
	public void notifyPairingFailure();

	void notifyNameProvided(String name);
	
}
