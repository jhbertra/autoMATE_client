package com.automate.client.managers.discovery;

import java.util.ArrayList;
import java.util.List;

import com.automate.client.managers.IListener;
import com.automate.client.managers.ManagerBase;
import com.automate.client.managers.bluetooth.BluetoothListener;
import com.automate.client.managers.bluetooth.IBluetoothManager;

public class DiscoveryManager extends ManagerBase<DiscoveryListener> implements IDiscoveryManager {

	private boolean bluetoothEnabled;
	
	private List<DeviceInfo> devices;

	private boolean scanning;
	
	private IBluetoothManager bluetoothManager;
	
	public DiscoveryManager() {
		super(DiscoveryListener.class);
	}

	@Override
	public void onDevicesDiscovered(List<DeviceInfo> device) {
		synchronized (mListeners) {
			for(DiscoveryListener listener : this.mListeners) {
				listener.onDevicesDiscovered(device);
			}
		}
	}

	@Override
	public void onScanningForDevices() {
		synchronized (mListeners) {
			for(DiscoveryListener listener : this.mListeners) {
				listener.onScanningForDevices();
			}
		}
	}

	@Override
	public void onBind(Class<? extends IListener> listenerClass) {}

	@Override
	public void onUnbind(Class<? extends IListener> listenerClass) {
		if(listenerClass.equals(BluetoothListener.class)) {
			this.bluetoothEnabled = false;
		}
	}

	@Override
	public void scan() {
		if(!bluetoothEnabled) return;
		// start scan for devices
		this.scanning = true;
	}

	@Override
	protected void unbindSelf() {
		this.bluetoothManager.unbind(this);
	}

	@Override
	protected void bindSelf() {
		this.bluetoothManager.bind(this);
	}

	@Override
	protected void setupInitialState() {
		this.bluetoothEnabled = false;
		this.scanning = false;
		this.devices = new ArrayList<DeviceInfo>();
	}

	@Override
	protected void teardown() {
		this.devices.clear();
	}

	@Override
	protected void performInitialUpdate(DiscoveryListener listener) {
		synchronized (devices) {
			if(this.bluetoothEnabled && this.devices.size() > 0) {
				listener.onDevicesDiscovered(devices);
			} else if (scanning) {
				listener.onScanningForDevices();
			}
		}
	}

}
