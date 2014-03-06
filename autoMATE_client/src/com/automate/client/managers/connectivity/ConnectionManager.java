package com.automate.client.managers.connectivity;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.automate.client.managers.IListener;
import com.automate.client.managers.ManagerBase;

public class ConnectionManager extends ManagerBase<ConnectionListener> implements IConnectionManager {

	public enum ConnectedState {
		DISCONNECTED,
		CONNECTING,
		CONNECTED
	}
	
	private ConnectedState mConnectedState;
	
	private Context mContext;
	
	public ConnectionManager(Context context) {
		super(ConnectionListener.class);
		this.mContext = context;
	}

	@Override
	public void onBind(Class<? extends IListener> listenerClass) {
		// no bindings
	}

	@Override
	public void onUnbind(Class<? extends IListener> listenerClass) {
		// no bindings
	}

	@Override
	protected void unbindSelf() {
		// no bindings
	}

	@Override
	protected void bindSelf() {
		// no bindings
	}

	@Override
	protected void setupInitialState() {
		this.mConnectedState = ConnectedState.DISCONNECTED;
	}

	@Override
	protected void teardown() {
		this.mConnectedState = ConnectedState.DISCONNECTED;
	}

	@Override
	protected void performInitialUpdate(ConnectionListener listener) {
		switch(mConnectedState) {
		case DISCONNECTED:
			listener.onDisconnected();
		case CONNECTING:
			listener.onConnecting();
		case CONNECTED:
			listener.onConnected();
		}
	}
	
	@Override
	public void scheduleDisconnect(long millis) {
		AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(mContext, DisconnectReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		alarmManager.set(AlarmManager.RTC_WAKEUP, millis, pendingIntent);
	}

	@Override
	public void disconnect() {
		this.mConnectedState = ConnectedState.DISCONNECTED;
		onDisconnected();
	}

	@Override
	public void onConnecting() {
		this.mConnectedState = ConnectedState.CONNECTING;
		synchronized (mListeners) {
			for(ConnectionListener listener : mListeners) {
				listener.onConnecting();
			}
		}
	}

	@Override
	public void onConnected() {
		this.mConnectedState = ConnectedState.CONNECTED;
		synchronized (mListeners) {
			for(ConnectionListener listener : mListeners) {
				listener.onConnected();
			}
		}
	}

	@Override
	public void onDisconnected() {
		this.mConnectedState = ConnectedState.DISCONNECTED;
		synchronized (mListeners) {
			for(ConnectionListener listener : mListeners) {
				listener.onDisconnected();
			}
		}
	}
}
