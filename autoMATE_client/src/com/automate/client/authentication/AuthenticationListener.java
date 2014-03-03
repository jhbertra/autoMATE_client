package com.automate.client.authentication;

import com.automate.client.IListener;

public interface AuthenticationListener extends IListener {

	public void onAuthenticated(String sessionKey, String username);
	
	public void onAuthenticationFailed(String failureMessage);
	
}
