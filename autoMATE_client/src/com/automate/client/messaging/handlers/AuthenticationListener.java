package com.automate.client.messaging.handlers;

public interface AuthenticationListener {

	public void onAuthenticated(String sessionKey, String username);
	
	public void onAuthenticationFailed(String failureMessage);
	
}
