package com.automate.client.messaging.handlers;

import java.util.ArrayList;

import android.util.Log;

import com.automate.protocol.Message;
import com.automate.protocol.server.ServerProtocolParameters;
import com.automate.protocol.server.messages.ServerAuthenticationMessage;

public class AuthenticationMessageHandler implements IMessageHandler<ServerAuthenticationMessage, Void> {

	private ArrayList<AuthenticationListener> listeners = new ArrayList<AuthenticationListener>();
	
	@Override
	public Message<ServerProtocolParameters> handleMessage(int majorVersion, 
			int minorVersion, ServerAuthenticationMessage message, Void params) {
		return null;
	}

	public void addListener(AuthenticationListener listener) {
		if(listener != null) {
			listeners.add(listener);
		} else {
			Log.w(getClass().getName(), "Attempt to add a null listener.");
		}
	}

	public void removeListener(AuthenticationListener listener) {
		if(listener != null) {
			listeners.remove(listener);
		} else {
			Log.w(getClass().getName(), "Attempt to remove a null listener.");
		}
	}
	
}
