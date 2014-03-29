package com.automate.client.views.registration;

import com.automate.client.views.authentication.AbstractAuthenticationService;

public class RegistrationService extends AbstractAuthenticationService {
	
	public boolean register(String username, String password, String name, String email) {
		return mAuthenticationManager.register(username, password, name, email);
	}

}
