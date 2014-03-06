package com.automate.client.managers.authentication;

import com.automate.client.managers.IListener;

public interface AuthenticationListener extends IListener {

	public void onAuthenticated(String sessionKey, String username);
	
	public void onAuthenticationFailed(String failureMessage);
	
	public void onAuthenticating(String username);
	
}
