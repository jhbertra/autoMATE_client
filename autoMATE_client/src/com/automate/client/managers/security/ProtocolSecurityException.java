package com.automate.client.managers.security;

public class ProtocolSecurityException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2879903672472455730L;

	public ProtocolSecurityException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public ProtocolSecurityException(String detailMessage) {
		super(detailMessage);
	}

	public ProtocolSecurityException(Throwable throwable) {
		super(throwable);
	}

}
