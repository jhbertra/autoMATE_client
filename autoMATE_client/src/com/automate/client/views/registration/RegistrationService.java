package com.automate.client.views.registration;

import com.automate.client.R;
import com.automate.client.managers.messaging.IMessageManager;
import com.automate.client.views.authentication.AbstractAuthenticationService;
import com.automate.protocol.client.messages.ClientRegistrationMessage;

import android.content.SharedPreferences.Editor;

public class RegistrationService extends AbstractAuthenticationService {
	
	public boolean register(String username, String password, String name, String email) {
		IMessageManager manager = mAutoMateService.getManager(IMessageManager.class);
		String currentSessionKey = manager.getSessionKey();
		if(username != null && password != null && name != null && email != null && (currentSessionKey == null || currentSessionKey.isEmpty())) {
			ClientRegistrationMessage message = new ClientRegistrationMessage(manager.getProtocolParameters(), 
					username, password, email, name);
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
