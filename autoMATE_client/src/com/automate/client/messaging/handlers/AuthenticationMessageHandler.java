package com.automate.client.messaging.handlers;

import android.util.Log;

import com.automate.client.managers.authentication.AuthenticationListener;
import com.automate.protocol.Message;
import com.automate.protocol.server.ServerProtocolParameters;
import com.automate.protocol.server.messages.ServerAuthenticationMessage;

public class AuthenticationMessageHandler implements IMessageHandler<ServerAuthenticationMessage, Void> {

	private AuthenticationListener listener;
	
	public AuthenticationMessageHandler(AuthenticationListener listener) {
		this.listener = listener;
	}

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
		if(listener != null) {
			listener.onAuthenticationFailed(failureMessage);
		}
	}

	private void notifyAuthenticated(String sessionKey, String username) {
		if(listener != null) {
			listener.onAuthenticated(sessionKey, username);
		}
	}

	public void setListener(AuthenticationListener listener) {
		this.listener = listener;
	}
	
}
