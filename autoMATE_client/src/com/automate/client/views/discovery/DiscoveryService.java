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

	public static class Messages {
		public static final int SCANNING = 0;
		public static final int DEVICES_DISCOVERRED = 1;
		public static final int NO_DEVICES = 2;
		public static final int PAIRING = 3;
		public static final int NEW_DEVICE_PARIED = 4;
		public static final int PAIRING_FAILED = 5;
		public static final int BLUETOOTH_UNAVAILABLE = 6;
		public static final int BLUETOOTH_DISABLED = 7;
	}
	
	private IBinder mBinder = new DiscoveryServiceBinder();
	private IPairingManager mPairingManager;
	private IBluetoothManager mBluetoothManager;

	private void notifyScanning() {
		Message message = new Message();
		message.what = Messages.SCANNING;
		try {
			mMessenger.send(message);
		} catch(RemoteException e) {}
	}
	
	private void notifyDevicesDiscovered(List<DeviceInfo> devices) {
		Message message = new Message();
		message.what = Messages.DEVICES_DISCOVERRED;
		message.obj = devices;
		try {
			mMessenger.send(message);
		} catch(RemoteException e) {}
	}
	
	private void notifyNoDevices() {
		Message message = new Message();
		message.what = Messages.NO_DEVICES;
		try {
			mMessenger.send(message);
		} catch(RemoteException e) {}
	}
	
	private void notifyPairing(DeviceInfo device) {
		Message message = new Message();
		message.what = Messages.PAIRING;
		message.obj = device;
		try {
			mMessenger.send(message);
		} catch(RemoteException e) {}
	}
	
	private void notifyNewDevicePaired(DeviceInfo device) {
		Message message = new Message();
		message.what = Messages.NEW_DEVICE_PARIED;
		message.obj = device;
		try {
			mMessenger.send(message);
		} catch(RemoteException e) {}
	}	

	private void notifyPairingFailure(DeviceInfo device) {
		Message message = new Message();
		message.what = Messages.PAIRING_FAILED;
		message.obj = device;
		try {
			mMessenger.send(message);
		} catch(RemoteException e) {}
	}
	
	private void notifyBluetoothUnavailable() {
		Message message = new Message();
		message.what = Messages.BLUETOOTH_UNAVAILABLE;
		try {
			mMessenger.send(message);
		} catch(RemoteException e) {}
	}
	
	private void notifyBluetoothDisabled() {
		Message message = new Message();
		message.what = Messages.BLUETOOTH_DISABLED;
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

	public void pair(DeviceInfo device) {
		this.mPairingManager.pair(device);
	}
	
	public void scan() {
		if(mPairingManager != null) {
			this.mPairingManager.scan();
		}
	}
	
	public void setDeviceName(String name) {
		if(mPairingManager != null) {
			this.mPairingManager.notifyNameProvided(name);
		}
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
		if(mPairingManager != null) {
			mPairingManager.unbind(this);
			mPairingManager = null;
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
		mPairingManager = mAutoMateService.getManager(IPairingManager.class);
		mBluetoothManager = mAutoMateService.getManager(IBluetoothManager.class);
		
		mBluetoothManager.bind(this);
		mPairingManager.bind(this);
	}
	
	@Override
	protected void onServiceDisconnected() {
		if(mPairingManager != null) {
			mPairingManager.unbind(this);
			mPairingManager = null;
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
