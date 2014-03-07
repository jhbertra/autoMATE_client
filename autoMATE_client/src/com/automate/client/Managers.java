package com.automate.client;

import com.automate.client.managers.IManager;
import com.automate.client.managers.authentication.IAuthenticationManager;
import com.automate.client.managers.command.ICommandManager;
import com.automate.client.managers.connectivity.IConnectionManager;
import com.automate.client.managers.messaging.IMessageManager;
import com.automate.client.managers.node.INodeManager;
import com.automate.client.managers.packet.IPacketManager;
import com.automate.client.managers.security.ISecurityManager;
import com.automate.client.managers.status.IStatusManager;
import com.automate.client.managers.warning.IWarningManager;

public class Managers {

	public final IAuthenticationManager authenticationManager;
	
	public final ICommandManager commandManager;
	
	public final IConnectionManager connectionManager;
	
	public final IMessageManager messageManager;
	
	public final INodeManager nodeManager;
	
	public final IPacketManager packetManager;
	
	public final ISecurityManager securityManager;
	
	public final IStatusManager statusManager;
	
	public final IWarningManager warningManager;

	public Managers(IAuthenticationManager authenticationManager,
			ICommandManager commandManager,
			IConnectionManager connectionManager,
			IMessageManager messageManager, INodeManager nodeManager,
			IPacketManager packetManager, ISecurityManager securityManager,
			IStatusManager statusManager, IWarningManager warningManager) {
		this.authenticationManager = authenticationManager;
		this.commandManager = commandManager;
		this.connectionManager = connectionManager;
		this.messageManager = messageManager;
		this.nodeManager = nodeManager;
		this.packetManager = packetManager;
		this.securityManager = securityManager;
		this.statusManager = statusManager;
		this.warningManager = warningManager;
	}
	
	public <T extends IManager<?>> T getManager(Class<T> managerClass) {
		if(managerClass.equals(IAuthenticationManager.class)) {
			return (T) authenticationManager;
		} else if(managerClass.equals(ICommandManager.class)) {
			return (T) commandManager;
		} else if(managerClass.equals(IConnectionManager.class)) {
			return (T) connectionManager;
		} else if(managerClass.equals(IMessageManager.class)) {
			return (T) messageManager;
		} else if(managerClass.equals(INodeManager.class)) {
			return (T) nodeManager;
		} else if(managerClass.equals(IPacketManager.class)) {
			return (T) packetManager;
		} else if(managerClass.equals(ISecurityManager.class)) {
			return (T) securityManager;
		} else if(managerClass.equals(IStatusManager.class)) {
			return (T) statusManager;
		} else if(managerClass.equals(IWarningManager.class)) {
			return (T) warningManager;
		} else {
			return null;
		}
	}
	
}
