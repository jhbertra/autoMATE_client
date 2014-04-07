package com.automate.client.views.discovery;

import java.util.List;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import com.automate.client.managers.IListener;
import com.automate.client.managers.discovery.DeviceInfo;
import com.automate.client.managers.discovery.DiscoveryListener;
import com.automate.client.managers.discovery.IDiscoveryManager;
import com.automate.client.managers.messaging.IMessageManager;
import com.automate.client.views.AbstractViewService;
import com.automate.protocol.client.messages.ClientNodeRegistrationMessage;

public class DiscoveryService extends AbstractViewService implements DiscoveryListener {

	public static int SCANNING = 0;
	public static int DEVICES_DISCOVERRED = 1;
	public static int NO_DEVICES = 2;
	
	private IBinder mBinder = new DiscoveryServiceBinder();
	private IMessageManager mMessageManager;
	private IDiscoveryManager mDiscoveryManager;
	
	@Override
	protected void onServiceConnected() {
		mMessageManager = mAutoMateService.getManager(IMessageManager.class);
		mDiscoveryManager = mAutoMateService.getManager(IDiscoveryManager.class);
		
		mDiscoveryManager.bind(this);
	}

	public void sendNodeRegistration(DeviceInfo device, String name) {
		ClientNodeRegistrationMessage message = new ClientNodeRegistrationMessage(mMessageManager.getProtocolParameters(), device.modelId, 
				name, device.maxVersionMajor, device.maxVersionMinor);
		this.mMessageManager.sendMessage(message);
	}
	
	public void scan() {
		this.mDiscoveryManager.scan();
	}
	
	@Override
	protected void onServiceDisconnected() {
		
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
	public void onBind(Class<? extends IListener> listenerClass) {}

	@Override
	public void onUnbind(Class<? extends IListener> listenerClass) {
		
	}

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
	
}
