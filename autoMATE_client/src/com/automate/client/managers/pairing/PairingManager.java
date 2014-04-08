package com.automate.client.managers.pairing;

import java.util.ArrayList; 
import java.util.List;

import android.bluetooth.BluetoothDevice;

import com.automate.client.managers.IListener;
import com.automate.client.managers.ManagerBase;
import com.automate.client.managers.bluetooth.BluetoothListener;
import com.automate.client.managers.bluetooth.IBluetoothManager;
import com.automate.client.managers.bluetooth.IBluetoothManager.MessageCallback;
import com.automate.client.managers.messaging.IMessageManager;
import com.automate.protocol.client.messages.ClientNodeRegistrationMessage;

public class PairingManager extends ManagerBase<PairingListener> implements IPairingManager {

	private boolean bluetoothEnabled;

	private List<BluetoothDevice> btDevices;

	private List<DeviceInfo> devices;

	private boolean scanning;

	private IBluetoothManager bluetoothManager;

	private IMessageManager messageManager;

	private DeviceInfo mPairingDevice;

	private String ssid = "airuc-secure";

	private String username = "jhbertra";

	private String passphrase = "CAMeosixty4";

	public PairingManager(IBluetoothManager bluetoothManager, IMessageManager messageManager) {
		super(PairingListener.class);
		this.messageManager = messageManager;
		this.bluetoothManager = bluetoothManager;
	}

	@Override
	public void onBluetoothUnavailable() {}

	@Override
	public void onBluetoothDisabled() {}

	@Override
	public void onBluetoothDiscovery() {
		synchronized (devices) {
			this.scanning = true;
			devices.clear();
			onScanningForDevices();
		}
	}

	@Override
	public void onBluetoothDiscoveryFinished() {
		getNextDeviceInfo();
	}

	@Override
	public void onDeviceDisvocered(BluetoothDevice device) {
		synchronized (devices) {
			this.btDevices.add(device);
		}
	}

	@Override
	public void onConnecting(BluetoothDevice device) {

	}

	@Override
	public void onConnectFailed(BluetoothDevice device) {
		if(scanning) {
			getNextDeviceInfo();
		} else {

		}
	}

	@Override
	public void onConnected(final BluetoothDevice device) {
		if(scanning) {
			bluetoothManager.sendMessage("sendDeviceInfo", new MessageCallback() {
				@Override
				public void onResponse(String message) {
					if(message != null) {
						String [] parts = message.substring(11, message.length() - 1).split(",");
						devices.add(new DeviceInfo(Long.parseLong(parts[0]), parts[1], Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), 
								device));
					}
					bluetoothManager.disconnect();
				}
			});
		} else if(mPairingDevice != null) {
			bluetoothManager.sendMessage("initPairing", new MessageCallback() {
				@Override
				public void onResponse(String message) {
					if("ackInitPairing".equals(message)) {
						onPairing(mPairingDevice);
					}
				}
			});
		}
	}

	@Override
	public void onDisconnected() {
		synchronized (devices) {
			if(scanning) {
				getNextDeviceInfo();
			} else {
				mPairingDevice = null;
			}
		}
	}

	@Override
	public void onDevicesDiscovered(List<DeviceInfo> device) {
		synchronized (mListeners) {
			for(PairingListener listener : this.mListeners) {
				listener.onDevicesDiscovered(device);
			}
		}
	}

	@Override
	public void onScanningForDevices() {
		synchronized (mListeners) {
			for(PairingListener listener : this.mListeners) {
				listener.onScanningForDevices();
			}
		}
	}

	@Override
	public void onPairing(DeviceInfo device) {
		synchronized (mListeners) {
			for(PairingListener listener : this.mListeners) {
				listener.onPairing(device);
			}
		}
	}

	@Override
	public void onNewDevicePaired(DeviceInfo device) {
		synchronized (mListeners) {
			for(PairingListener listener : this.mListeners) {
				listener.onNewDevicePaired(device);
			}
		}
	}

	@Override
	public void onPairingFailure(DeviceInfo device) {
		synchronized (mListeners) {
			for(PairingListener listener : this.mListeners) {
				listener.onPairingFailure(device);
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
		bluetoothManager.startDiscovery();
	}

	@Override
	public void pair(DeviceInfo device) {
		if(!scanning && device != null) {
			mPairingDevice = device;
			bluetoothManager.connect(device.bluetoothdevice);
		}
	}

	@Override
	public void notifyNameProvided(String name) {
		ClientNodeRegistrationMessage regMessage = new ClientNodeRegistrationMessage(messageManager.getProtocolParameters(), 
				mPairingDevice.modelId, name, mPairingDevice.maxVersionMajor, mPairingDevice.maxVersionMinor);
		messageManager.sendMessage(regMessage);
	}

	@Override
	public void notifyPairingSuccess(final long nodeId, final String password) {
		bluetoothManager.sendMessage("wifiCreds(" + ssid + "," + username + "," + passphrase + ")", new MessageCallback() {
			@Override
			public void onResponse(String message) {
				if("ackWifiCreds".equals(message)) {
					bluetoothManager.sendMessage("pairingSuccess(" + nodeId + "," + password + ")", new MessageCallback() {
						@Override
						public void onResponse(String message) {
							if("ackPairingSuccess".equals(message)) {
								onNewDevicePaired(mPairingDevice);
								bluetoothManager.disconnect();
							}
						}
					});
				}
			}
		});
	}

	@Override
	public void notifyPairingFailure() {
		onPairingFailure(mPairingDevice);
		bluetoothManager.disconnect();
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
		this.btDevices = new ArrayList<BluetoothDevice>();
	}

	@Override
	protected void teardown() {
		this.devices.clear();
	}

	@Override
	protected void performInitialUpdate(PairingListener listener) {
		synchronized (devices) {
			if(this.bluetoothEnabled && this.devices.size() > 0) {
				listener.onDevicesDiscovered(devices);
			} else if (scanning) {
				listener.onScanningForDevices();
			}
		}
	}


	private void getNextDeviceInfo() {
		BluetoothDevice device = null;
		while(btDevices.size() > 0) {
			device = btDevices.remove(0);
			String deviceName = device.getName();
			if(deviceName != null && deviceName.equals("autoMATE-bluetooth-service")) {
				getDeviceInfo(device);
				return;
			}
		}
		scanning = false;
		onDevicesDiscovered(devices);
	}

	private void getDeviceInfo(BluetoothDevice device) {
		bluetoothManager.connect(device);
	}
}
