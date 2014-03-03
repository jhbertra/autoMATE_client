package com.automate.client.views.authentication;

import com.automate.client.R;
import com.automate.client.views.AbstractAuthenticationService;
import com.automate.protocol.client.messages.ClientAuthenticationMessage;

import android.content.SharedPreferences.Editor;

public class AuthenticationService extends AbstractAuthenticationService {

	public boolean signIn(String username, String password) {
		String currentSessionKey = mMessagingServiceApi.getSessionKey();
		if(username != null && password != null && (currentSessionKey == null || currentSessionKey.isEmpty())) {
			ClientAuthenticationMessage message = new ClientAuthenticationMessage(mMessagingServiceApi.getProtocolParameters(), 
					username, password);
			mMessagingServiceApi.sendMessage(message, this);
			String prefsKey = getResources().getString(R.string.prefs_credentials);
			Editor editor = getSharedPreferences(prefsKey, MODE_PRIVATE).edit();
			editor.putString(getResources().getString(R.string.prefs_credentials_password), password);
			editor.commit();
			return true;
		}
		return false;
	}
	
}
