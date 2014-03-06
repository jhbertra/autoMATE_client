package com.automate.client.managers.security;

import com.automate.client.managers.IListener;
import com.automate.client.managers.ManagerBase;

public class SecurityManager extends ManagerBase<SecurityListener> implements ISecurityManager {

	public SecurityManager() {
		super(SecurityListener.class);
	}

	@Override
	public void onBind(Class<? extends IListener> listenerClass) {}

	@Override
	public void onUnbind(Class<? extends IListener> listenerClass) {}

	@Override
	protected void unbindSelf() {
		// no dependencies
	}

	@Override
	protected void bindSelf() {
		// no dependencies
	}

	@Override
	protected void setupInitialState() {
		// no state
	}
	
	@Override
	protected void teardown() {
		// no state
	}

	@Override
	protected void performInitialUpdate(SecurityListener listener) {
		// no listener operations
	}

	@Override
	public String encrypt(String message) throws ProtocolSecurityException {
		return message; // TODO implement in a later version
	}

	@Override
	public String decrypt(String packet) throws ProtocolSecurityException {
		return packet; // TODO implement in a later version
	}

}
