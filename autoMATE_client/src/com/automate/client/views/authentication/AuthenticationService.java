package com.automate.client.views.authentication;

public class AuthenticationService extends AbstractAuthenticationService {

	public boolean signIn(String username, String password) {
		return this.mAuthenticationManager.signIn(username, password);
	}
	
}
