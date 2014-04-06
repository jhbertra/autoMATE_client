package com.automate.client.managers.authentication;

import android.app.AlarmManager; 
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.automate.client.R;
import com.automate.client.managers.IListener;
import com.automate.client.managers.ManagerBase;
import com.automate.client.managers.connectivity.ConnectionListener;
import com.automate.client.managers.connectivity.ConnectionManager.ConnectedState;
import com.automate.client.managers.connectivity.IConnectionManager;
import com.automate.client.managers.messaging.IMessageManager;
import com.automate.client.managers.packet.PacketSentListener;
import com.automate.protocol.Message;
import com.automate.protocol.client.ClientProtocolParameters;
import com.automate.protocol.client.messages.ClientAuthenticationMessage;
import com.automate.protocol.client.messages.ClientRegistrationMessage;
import com.automate.protocol.server.ServerProtocolParameters;

public class AuthenticationManager extends ManagerBase<AuthenticationListener> implements IAuthenticationManager, PacketSentListener {

	private Context mContext;
	private IMessageManager mMessageManager;
	private IConnectionManager mConnectionManager;

	private ConnectedState mConnectedState;

	private String mUsername;
	private String mPassword;

	private boolean attemptReconeect;

	public AuthenticationManager(Context context, IMessageManager messageManager, IConnectionManager connectionManager) {
		super(AuthenticationListener.class);
		this.mContext = context;
		this.mMessageManager = messageManager;
		this.mConnectionManager = connectionManager;
	}

	@Override
	public void onBind(Class<? extends IListener> listenerClass) {
	}

	@Override
	public void onUnbind(Class<? extends IListener> listenerClass) {
		if(listenerClass.equals(ConnectionListener.class)) {
			mConnectedState = ConnectedState.DISCONNECTED;
		}
	}

	@Override
	public boolean signIn(String username, String password) {
		String currentSessionKey = mMessageManager.getSessionKey();
		if(username != null && password != null 
				&& (currentSessionKey == null || currentSessionKey.isEmpty())) {
			ClientAuthenticationMessage message = new ClientAuthenticationMessage(mMessageManager.getProtocolParameters(), username, password);
			mMessageManager.sendMessage(message, this);
			onAuthenticating(username);
			mUsername = username;
			mPassword = password;
			this.attemptReconeect = true;
			return true;
		}
		return false;
	}

	@Override
	public void onPacketSent(int packetId) {}

	@Override
	public void onSendIoException(int packetId) {
		onAuthenticationFailed("A network error occured.");
	}

	@Override
	public void onSendNoServerAddress(int packetId) {
		onAuthenticationFailed("No destination address.");
	}

	@Override
	public void onSendNoServerPort(int packetId) {
		onAuthenticationFailed("No destination port.");
	}

	@Override
	public void onSendError(int packetId) {
		onAuthenticationFailed("Unknown error.");
	}

	@Override
	public boolean signOut() {
		this.mUsername = null;
		this.mPassword = null;
		this.mConnectionManager.disconnect();
		this.attemptReconeect = false;
		return false;
	}

	@Override
	public boolean register(String username, String password, String name, String email) {
		String currentSessionKey = mMessageManager.getSessionKey();
		if(username != null && password != null && name != null && email != null 
				&& (currentSessionKey == null || currentSessionKey.isEmpty())) {
			ClientRegistrationMessage message = new ClientRegistrationMessage(mMessageManager.getProtocolParameters(), username, 
					password, email, name);
			mMessageManager.sendMessage(message, this);
			onAuthenticating(username);
			mUsername = username;
			mPassword = password;
			this.attemptReconeect = true;
			return true;
		}
		return false;
	}

	@Override
	protected void unbindSelf() {
		mMessageManager.unbind(this);
		mConnectionManager.unbind(this);
	}

	@Override
	protected void bindSelf() {
		mMessageManager.bind(this);
		mConnectionManager.bind(this);
	}

	@Override
	protected void setupInitialState() {
		this.mConnectedState = ConnectedState.DISCONNECTED;
		SharedPreferences authPrefs = mContext.getSharedPreferences(mContext.getResources().getString(R.string.prefs_credentials), 
				Context.MODE_PRIVATE);
		String username = authPrefs.getString(mContext.getResources().getString(R.string.prefs_credentials_username), null);
		String password = authPrefs.getString(mContext.getResources().getString(R.string.prefs_credentials_password), null);
		if(username != null && password != null) {
			signIn(username, password);
		}
	}

	@Override
	protected void teardown() {
		this.mUsername = null;
		this.mPassword = null;
		signOut();
	}

	@Override
	protected void performInitialUpdate(AuthenticationListener listener) {
		switch(mConnectedState) {
		case CONNECTED:
			listener.onAuthenticated(mMessageManager.getSessionKey(), mUsername);
			break;
		case CONNECTING:
			listener.onAuthenticating(mUsername);
			break;
		case DISCONNECTED:
			break;
		}
	}

	/* (non-Javadoc)
	 * @see com.automate.client.authentication.IAuthenticationManagerr#onAuthenticated(java.lang.String, java.lang.String)
	 */
	@Override
	public void onAuthenticated(String sessionKey, String username) {
		Log.i(getClass().getName(), "Client authenticated.");
		mMessageManager.setSessionKey(sessionKey);
		String prefsKey = mContext.getResources().getString(R.string.prefs_credentials);
		Editor editor = mContext.getSharedPreferences(prefsKey, Context.MODE_PRIVATE).edit();
		editor.putString(mContext.getResources().getString(R.string.prefs_credentials_username), username);
		editor.putString(mContext.getResources().getString(R.string.prefs_credentials_password), mPassword);
		editor.commit();
		mConnectionManager.onConnected();
		for(AuthenticationListener listener : mListeners) {
			listener.onAuthenticated(sessionKey, username);
		}
	}

	/* (non-Javadoc)
	 * @see com.automate.client.authentication.IAuthenticationManagerr#onAuthenticationFailed(java.lang.String)
	 */
	@Override
	public void onAuthenticationFailed(String failureMessage) {
		mMessageManager.setSessionKey(null);
		mConnectionManager.onDisconnected();
		for(AuthenticationListener listener : mListeners) {
			listener.onAuthenticationFailed(failureMessage);
		}
		if(attemptReconeect) {
			scheduleReconnect();
		}
	}

	@Override
	public void onAuthenticating(String username) {
		mConnectionManager.onConnecting();
		for(AuthenticationListener listener : mListeners) {
			listener.onAuthenticating(username);
		}
	}

	@Override
	public void onMessageReceived(Message<ServerProtocolParameters> message) {
	}

	@Override
	public void onMessageSent(Message<ClientProtocolParameters> message) {
	}

	@Override
	public void onConnecting() {
		this.mConnectedState = ConnectedState.CONNECTING;
	}

	@Override
	public void onConnected() {
		this.mConnectedState = ConnectedState.CONNECTED;
	}

	@Override
	public void onDisconnected() {
		this.mConnectedState = ConnectedState.DISCONNECTED;
		if(attemptReconeect) {
			scheduleReconnect();
		}
	}

	private void scheduleReconnect() {
		Log.i(getClass().getName(), "Reconnect scheduled.");
		AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(mContext, ReconnectReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 15000, pendingIntent);
	}

	@Override
	public void reconnect() {
		if(mUsername != null && mPassword != null) {
			signIn(mUsername, mPassword);
		}
	}

}
