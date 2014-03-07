package com.automate.client.views.authentication;

import com.automate.client.R;
import com.automate.client.managers.messaging.IMessageManager;
import com.automate.protocol.client.messages.ClientAuthenticationMessage;

import android.content.SharedPreferences.Editor;

public class AuthenticationService extends AbstractAuthenticationService {

	public boolean signIn(String username, String password) {
		IMessageManager manager = mAutoMateService.getManager(IMessageManager.class);
		String currentSessionKey = manager.getSessionKey();
		if(username != null && password != null && (currentSessionKey == null || currentSessionKey.isEmpty())) {
			ClientAuthenticationMessage message = new ClientAuthenticationMessage(manager.getProtocolParameters(), 
					username, password);
			manager.sendMessage(message, this);
			String prefsKey = getResources().getString(R.string.prefs_credentials);
			Editor editor = getSharedPreferences(prefsKey, MODE_PRIVATE).edit();
			editor.putString(getResources().getString(R.string.prefs_credentials_password), password);
			editor.commit();
			return true;
		}
		return false;
	}
	
}
