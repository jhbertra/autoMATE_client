package com.automate.client.messaging.handlers;

import java.util.ArrayList;

import android.util.Log;

import com.automate.protocol.Message;
import com.automate.protocol.server.ServerProtocolParameters;
import com.automate.protocol.server.messages.ServerAuthenticationMessage;

public class AuthenticationMessageHandler implements IMessageHandler<ServerAuthenticationMessage, Void> {

	private ArrayList<AuthenticationListener> listeners = new ArrayList<AuthenticationListener>();
	
	@Override
	public Message<ServerProtocolParameters> handleMessage(int majorVersion, int minorVersion, ServerAuthenticationMessage message, Void params) {
		int responseCode = message.responseCode;
		switch(responseCode) {
		case 200:
			String sessionKey = message.sessionKey;
			if(sessionKey == null ) {
				Log.e(getClass().getName(), "Unable to handle response.  Session key not provided.");
				return null;
			}
			notifyAuthenticated(sessionKey, message.username);
			break;
		case 400:
			notifyAuthenticationFailed("Incorrect username / password");
			break;
		case 401:
			notifyAuthenticationFailed("The requested username already exists.");
			break;
		case 500:
			notifyAuthenticationFailed("InternalServerError");
			break;
		default:
			Log.e(getClass().getName(), "Unable to handle response.  Response code " + responseCode + " unknown.");
		}
		return null;
	}

	private void notifyAuthenticationFailed(String failureMessage) {
		for(AuthenticationListener listener : listeners) {
			listener.onAuthenticationFailed(failureMessage);
		}
	}

	private void notifyAuthenticated(String sessionKey, String username) {
		for(AuthenticationListener listener : listeners) {
			listener.onAuthenticated(sessionKey, username);
		}
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
