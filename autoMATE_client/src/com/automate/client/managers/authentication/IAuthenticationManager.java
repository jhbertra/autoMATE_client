package com.automate.client.managers.authentication;

import com.automate.client.managers.IManager;
import com.automate.client.managers.connectivity.ConnectionListener;
import com.automate.client.managers.messaging.MessageListener;

public interface IAuthenticationManager extends IManager<AuthenticationListener>, AuthenticationListener, MessageListener, ConnectionListener {

	public boolean signIn(String username, String password);
	
	public boolean signOut();
	
	public boolean register(String username, String password, String name, String email);
	
}