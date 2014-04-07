package com.automate.client.managers.bluetooth;

import java.io.IOException;
import java.util.ArrayList; 
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.automate.client.managers.IListener;
import com.automate.client.managers.ManagerBase;
import com.automate.client.managers.bluetooth.ConnectTask.ConnectionCallback;
import com.automate.client.managers.bluetooth.IBluetoothManager.MessageCallback;

public class BluetoothManager extends ManagerBase<BluetoothListener> implements IBluetoothManager {
	
	private BluetoothAdapter mBluetoothAdapter;
	
	private List<BluetoothDevice> devices;
	
	private DiscoveryReceiver mReceiver;
	
	private ExecutorService comminucationThread;
	
	public enum State {
		NO_BLUETOOTH,
		DISABLED,
		DISCOVERY_RUNNING,
		IDLE,
		CONNECTING,
		CONNECTED
	}
	
	private State mState;

	private Context mContext;
	
	private BluetoothSocket mConnectedSocket;

	private BluetoothDevice mConnectingDevice;
	
	public BluetoothManager(Context context) {
		super(BluetoothListener.class);
		devices = new ArrayList<BluetoothDevice>();
		mReceiver = new DiscoveryReceiver();
		mContext = context;
		comminucationThread = Executors.newSingleThreadExecutor();
	}
	
	@Override
	public void onBluetoothUnavailable() {
		synchronized (mListeners) {
			for(BluetoothListener listener : mListeners) {
				listener.onBluetoothUnavailable();
			}
		}
	}

	@Override
	public void onBluetoothDisabled() {
		synchronized (mListeners) {
			for(BluetoothListener listener : mListeners) {
				listener.onBluetoothDisabled();
			}
		}
	}

	@Override
	public void onBluetoothDiscovery() {
		synchronized (mListeners) {
			for(BluetoothListener listener : mListeners) {
				listener.onBluetoothDiscovery();
			}
		}
	}

	@Override
	public void onConnecting(BluetoothDevice device) {
		synchronized (mListeners) {
			for(BluetoothListener listener : mListeners) {
				listener.onConnecting(device);
			}
		}
	}

	@Override
	public void onConnectFailed(BluetoothDevice device) {
		synchronized (mListeners) {
			for(BluetoothListener listener : mListeners) {
				listener.onConnectFailed(device);
			}
		}
	}

	@Override
	public void onConnected(BluetoothDevice device) {
		synchronized (mListeners) {
			for(BluetoothListener listener : mListeners) {
				listener.onConnected(device);
			}
		}
	}

	@Override
	public void onDisconnected() {
		synchronized (mListeners) {
			for(BluetoothListener listener : mListeners) {
				listener.onDisconnected();
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.automate.client.managers.bluetooth.BluetoothListener#onBluetoothDiscoveryFinished()
	 */
	@Override
	public void onBluetoothDiscoveryFinished() {
		synchronized (mListeners) {
			for(BluetoothListener listener : mListeners) {
				listener.onBluetoothDiscoveryFinished();
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.automate.client.managers.bluetooth.BluetoothListener#onDevicesDisvocered(java.util.List)
	 */
	@Override
	public void onDeviceDisvocered(BluetoothDevice device) {
		synchronized (mListeners) {
			for(BluetoothListener listener : mListeners) {
				listener.onDeviceDisvocered(device);
			}
		}
	}

	@Override
	protected void unbindSelf() {}
	@Override
	protected void bindSelf() {}

	@Override
	protected void setupInitialState() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBluetoothAdapter == null) {
			mState = State.NO_BLUETOOTH;
			return;
		}
		if(mBluetoothAdapter.isEnabled()) {
			mState = State.DISABLED;
			return;
		}
		if(mBluetoothAdapter.isDiscovering()) {
			mState = State.DISCOVERY_RUNNING;
		} else {
			mState = State.IDLE;
		}
		IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		mContext.registerReceiver(mReceiver, filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		mContext.registerReceiver(mReceiver, filter);
		filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		mContext.registerReceiver(mReceiver, filter);
	}

	@Override
	protected void teardown() {
		mContext.unregisterReceiver(mReceiver);
	}

	@Override
	protected void performInitialUpdate(BluetoothListener listener) {
		updateState();
		switch(mState) {
		case DISABLED:
			onBluetoothDisabled();
			break;
			
		case DISCOVERY_RUNNING:
			onBluetoothDiscovery();
		case IDLE:
			synchronized (devices) {
				for(BluetoothDevice device : devices) {
					listener.onDeviceDisvocered(device);
				}
			}
			break;
			
		case NO_BLUETOOTH:
			onBluetoothUnavailable();
			break;
			
		case CONNECTED:
			onConnected(mConnectedSocket.getRemoteDevice());
			break;
			
		case CONNECTING:
			onConnecting(mConnectingDevice);
			break;
			
		default:
			break;
		}
	}

	@Override
	public void onBind(Class<? extends IListener> listenerClass) {}

	@Override
	public void onUnbind(Class<? extends IListener> listenerClass) {}

	@Override
	public void startDiscovery() {
		if(!updateState()) return;
		if(mBluetoothAdapter.isDiscovering()) {
			mBluetoothAdapter.cancelDiscovery();
		}
		mBluetoothAdapter.startDiscovery();
		onBluetoothDiscovery();
	}
	
	@Override
	public void connect(final BluetoothDevice device) {
		if(mState == State.CONNECTED || mState == State.CONNECTING) throw new IllegalStateException("Cannot connect while already connected or connecting.");
		mBluetoothAdapter.cancelDiscovery();
		comminucationThread.submit(new ConnectTask(device, new ConnectionCallback() {
			@Override
			public void onConnected(BluetoothSocket socket) {
				synchronized (device) {
					mConnectedSocket = socket;
					mState = State.CONNECTED;
					mConnectingDevice = null;
					BluetoothManager.this.onConnected(device);
				}
			}
			@Override
			public void onConnectFailed() {
				synchronized (device) {
					mState = State.IDLE;
					mConnectingDevice = null;
					BluetoothManager.this.onConnectFailed(device);
				}
			}
			
		}));
		synchronized (device) {
			mState = State.CONNECTING;
			mConnectingDevice = device;
			onConnecting(device);
		}
	}

	@Override
	public void disconnect() {
		if(mState != State.CONNECTED) throw new IllegalStateException("Can only disconnect when connected to a device.");
		comminucationThread.submit(new Runnable() {
			@Override
			public void run() {
				try {
					mConnectedSocket.close();
				} catch (IOException e) {
					Log.e(getClass().getName(), "Error disconnecting from bluetooth device.", e);
				}
				mConnectedSocket = null;
				mState = State.IDLE;
				onDisconnected();
			}
		});
	}

	@Override
	public void sendMessage(String message, MessageCallback callback) {
		if(mState != State.CONNECTED) throw new IllegalStateException("Can only send message when connected to a device.");
		comminucationThread.submit(new SendMessageTask(mConnectedSocket, message, callback));
	}

	private boolean updateState() {
		if(mState == State.CONNECTED || mState == State.CONNECTING) return true;
		if(mBluetoothAdapter == null) {
			mState = State.NO_BLUETOOTH;
			return false;
		}
		boolean enebled = mBluetoothAdapter.isEnabled();
		if(!enebled) {
			onBluetoothDisabled();
			mState = State.DISABLED;
		} else {
			if(mBluetoothAdapter.isDiscovering()) {
				mState = State.DISCOVERY_RUNNING;
			} else 
				mState = State.IDLE;
		}
		return enebled;
	}
	
	private class DiscoveryReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
				synchronized (devices) {
					devices.clear();
				}
				onBluetoothDiscovery();
			} else if(action.equals(BluetoothDevice.ACTION_FOUND.toString())) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				synchronized (devices) {
					devices.add(device);
				}
				onDeviceDisvocered(device);
			} else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.toString())) {
				onBluetoothDiscoveryFinished();
			}
		}
		
	}

}
