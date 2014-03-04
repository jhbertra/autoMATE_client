package com.automate.client.authentication;

import com.automate.client.IManager;

public interface IAuthenticationManager extends IManager, AuthenticationListener {

	public abstract void onAuthenticated(String sessionKey, String username);

	public abstract void onAuthenticationFailed(String failureMessage);

}