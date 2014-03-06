package com.automate.client.managers.security;

import com.automate.client.managers.IManager;

public interface ISecurityManager extends IManager<SecurityListener>, SecurityListener {

	public String encrypt(String message) throws ProtocolSecurityException;
	
	public String decrypt(String packet) throws ProtocolSecurityException;
	
}
