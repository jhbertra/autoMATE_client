package com.automate.client.views.discovery;

import java.util.List; 

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import com.automate.client.managers.IListener;
import com.automate.client.managers.bluetooth.BluetoothListener;
import com.automate.client.managers.bluetooth.IBluetoothManager;
import com.automate.client.managers.pairing.DeviceInfo;
import com.automate.client.managers.pairing.PairingListener;
import com.automate.client.managers.pairing.IPairingManager;
import com.automate.client.views.AbstractViewService;

public class DiscoveryService extends AbstractViewService implements PairingListener, BluetoothListener {

	public static int SCANNING = 0;
	public static int DEVICES_DISCOVERRED = 1;
	public static int NO_DEVICES = 2;
	public static int PAIRING = 3;
	public static int NEW_DEVICE_PARIED = 4;
	public static int PAIRING_FAILED = 5;
	public static int BLUETOOTH_UNAVAILABLE = 6;
	public static int BLUETOOTH_DISABLED = 7;
	
	private IBinder mBinder = new DiscoveryServiceBinder();
	private IPairingManager mDiscoveryManager;
	private IBluetoothManager mBluetoothManager;

	private void notifyScanning() {
		Message message = new Message();
		message.what = SCANNING;
		try {
			mMessenger.send(message);
		} catch(RemoteException e) {}
	}
	
	private void notifyDevicesDiscovered(List<DeviceInfo> devices) {
		Message message = new Message();
		message.what = DEVICES_DISCOVERRED;
		message.obj = devices;
		try {
			mMessenger.send(message);
		} catch(RemoteException e) {}
	}
	
	private void notifyNoDevices() {
		Message message = new Message();
		message.what = NO_DEVICES;
		try {
			mMessenger.send(message);
		} catch(RemoteException e) {}
	}
	
	private void notifyPairing(DeviceInfo device) {
		Message message = new Message();
		message.what = PAIRING;
		message.obj = device;
		try {
			mMessenger.send(message);
		} catch(RemoteException e) {}
	}
	
	private void notifyNewDevicePaired(DeviceInfo device) {
		Message message = new Message();
		message.what = NEW_DEVICE_PARIED;
		message.obj = device;
		try {
			mMessenger.send(message);
		} catch(RemoteException e) {}
	}	

	private void notifyPairingFailure(DeviceInfo device) {
		Message message = new Message();
		message.what = PAIRING_FAILED;
		message.obj = device;
		try {
			mMessenger.send(message);
		} catch(RemoteException e) {}
	}
	
	private void notifyBluetoothUnavailable() {
		Message message = new Message();
		message.what = BLUETOOTH_UNAVAILABLE;
		try {
			mMessenger.send(message);
		} catch(RemoteException e) {}
	}
	
	private void notifyBluetoothDisabled() {
		Message message = new Message();
		message.what = BLUETOOTH_DISABLED;
		try {
			mMessenger.send(message);
		} catch(RemoteException e) {}
	}
	
	@Override
	public void onBluetoothUnavailable() {
		notifyBluetoothUnavailable();
	}

	@Override
	public void onBluetoothDisabled() {
		notifyBluetoothDisabled();
	}
	public void onBluetoothDiscovery() {}
	public void onBluetoothDiscoveryFinished() {}
	public void onDeviceDisvocered(BluetoothDevice device) {}
	public void onConnecting(BluetoothDevice device) {}
	public void onConnectFailed(BluetoothDevice device) {}
	public void onConnected(BluetoothDevice device) {}
	public void onDisconnected() {}
	
	@Override
	public void onDevicesDiscovered(List<DeviceInfo> device) {
		if(device.size() == 0) {
			notifyNoDevices();
		} else {
			notifyDevicesDiscovered(device);
		}
	}

	@Override
	public void onScanningForDevices() {
		notifyScanning();
	}
	
	@Override
	public void onPairing(DeviceInfo device) {
		notifyPairing(device);
	}

	@Override
	public void onNewDevicePaired(DeviceInfo device) {
		notifyNewDevicePaired(device);
	}

	@Override
	public void onPairingFailure(DeviceInfo device) {
		notifyPairingFailure(device);
	}

	public void scan() {
		this.mDiscoveryManager.scan();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	/* (non-Javadoc)
	 * @see com.automate.client.views.AbstractViewService#onDestroy()
	 */
	@Override
	public void onDestroy() {
		if(mDiscoveryManager != null) {
			mDiscoveryManager.unbind(this);
			mDiscoveryManager = null;
		}
		super.onDestroy();
	}

	class DiscoveryServiceBinder extends Binder {
		
		public DiscoveryService getService() {
			return DiscoveryService.this;
		}
		
	}
	
	@Override
	protected void onServiceConnected() {
		mDiscoveryManager = mAutoMateService.getManager(IPairingManager.class);
		mBluetoothManager = mAutoMateService.getManager(IBluetoothManager.class);
		
		mBluetoothManager.bind(this);
		mDiscoveryManager.bind(this);
	}
	
	@Override
	protected void onServiceDisconnected() {
		if(mDiscoveryManager != null) {
			mDiscoveryManager.unbind(this);
			mDiscoveryManager = null;
		}
		
		if(mBluetoothManager!= null) {
			mBluetoothManager.unbind(this);
			mBluetoothManager = null;
		}
	}

	@Override
	public void onBind(Class<? extends IListener> listenerClass) {}
	@Override
	public void onUnbind(Class<? extends IListener> listenerClass) {}

}
