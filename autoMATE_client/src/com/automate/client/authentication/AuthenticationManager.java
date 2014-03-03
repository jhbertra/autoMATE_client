package com.automate.client.authentication;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.automate.client.ListenerBinding;
import com.automate.client.R;
import com.automate.client.messaging.MessagingService;
import com.automate.client.messaging.MessagingService.Api;
import com.automate.client.messaging.handlers.AuthenticationMessageHandler;

public class AuthenticationManager extends ListenerBinding<AuthenticationListener > implements AuthenticationListener {

	private AuthenticationMessageHandler mHandler;
	private Context mContext;
	private Api mMessagingServiceApi;

	public AuthenticationManager(Context context, AuthenticationMessageHandler handler, MessagingService.Api messagingServiceApi) {
		this.mHandler = handler;
		this.mContext = context;
		this.mMessagingServiceApi = messagingServiceApi;
		this.mHandler.setListener(this);
	}
	
	@Override
	public void onAuthenticated(String sessionKey, String username) {
		Log.i(getClass().getName(), "Client authenticated.");
		mMessagingServiceApi.setSessionKey(sessionKey);
		String prefsKey = mContext.getResources().getString(R.string.prefs_credentials);
		Editor editor = mContext.getSharedPreferences(prefsKey, Context.MODE_PRIVATE).edit();
		editor.putString(mContext.getResources().getString(R.string.prefs_credentials_username), username);
		editor.commit();
		for(AuthenticationListener listener : listeners) {
			listener.onAuthenticated(sessionKey, username);
		}
	}

	@Override
	public void onAuthenticationFailed(String failureMessage) {
		mMessagingServiceApi.setSessionKey(null);
		for(AuthenticationListener listener : listeners) {
			listener.onAuthenticationFailed(failureMessage);
		}
	}

	public void onDestroy() {
		mHandler.setListener(null);
		listeners.clear();
	}

}
