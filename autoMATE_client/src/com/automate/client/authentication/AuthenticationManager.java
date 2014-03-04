package com.automate.client.authentication;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.automate.client.ListenerBinding;
import com.automate.client.R;
import com.automate.client.messaging.handlers.AuthenticationMessageHandler;
import com.automate.client.messaging.managers.IMessageManager;

public class AuthenticationManager extends ListenerBinding<AuthenticationListener > implements AuthenticationListener, IAuthenticationManager {

	private AuthenticationMessageHandler mHandler;
	private Context mContext;
	private IMessageManager mMessageManager;

	public AuthenticationManager(Context context, AuthenticationMessageHandler handler, IMessageManager messageManager) {
		this.mHandler = handler;
		this.mContext = context;
		this.mMessageManager = messageManager;
		this.mHandler.setListener(this);
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
		editor.commit();
		for(AuthenticationListener listener : listeners) {
			listener.onAuthenticated(sessionKey, username);
		}
	}

	/* (non-Javadoc)
	 * @see com.automate.client.authentication.IAuthenticationManagerr#onAuthenticationFailed(java.lang.String)
	 */
	@Override
	public void onAuthenticationFailed(String failureMessage) {
		mMessageManager.setSessionKey(null);
		for(AuthenticationListener listener : listeners) {
			listener.onAuthenticationFailed(failureMessage);
		}
	}

	@Override
	public void start() {
		
	}

	@Override
	public void stop() {
		mHandler.setListener(null);
		listeners.clear();
	}

}
