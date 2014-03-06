package com.automate.client.managers.warning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.automate.client.managers.IListener;
import com.automate.client.managers.ManagerBase;
import com.automate.client.managers.connectivity.IConnectionManager;
import com.automate.client.managers.messaging.IMessageManager;
import com.automate.client.managers.node.INodeManager;
import com.automate.client.managers.packet.PacketSentListener;
import com.automate.protocol.Message;
import com.automate.protocol.client.ClientProtocolParameters;
import com.automate.protocol.client.messages.ClientWarningMessage;
import com.automate.protocol.models.Node;
import com.automate.protocol.models.Warning;
import com.automate.protocol.server.ServerProtocolParameters;

public class WarningManager extends ManagerBase<WarningListener> implements IWarningManager {

	private IConnectionManager mConnectivityManager;
	
	private INodeManager mNodeManager;
	
	private IMessageManager mMessageManager;

	private HashMap<Long, List<Warning>> mWarnings = new HashMap<Long, List<Warning>>();
	
	public WarningManager(IConnectionManager mConnectivityManager, INodeManager mNodeManager, IMessageManager mMessageManager) {
		super(WarningListener.class);
		this.mConnectivityManager = mConnectivityManager;
		this.mNodeManager = mNodeManager;
		this.mMessageManager = mMessageManager;
	}

	@Override
	public void onBind(Class<? extends IListener> listenerClass) {}
	@Override
	public void onUnbind(Class<? extends IListener> listenerClass) {}

	@Override
	public void onWarningReceived(long nodeId, Warning warning) {
		mWarnings.get(nodeId).add(warning);
		synchronized (mListeners) {
			for(WarningListener listener : mListeners) {
				listener.onWarningReceived(nodeId, warning);
			}
		}
	}

	@Override
	protected void unbindSelf() {
		this.mConnectivityManager.unbind(this);
		this.mNodeManager.unbind(this);
		this.mMessageManager.unbind(this);
	}

	@Override
	protected void bindSelf() {
		this.mConnectivityManager.bind(this);
		this.mNodeManager.bind(this);
		this.mMessageManager.bind(this);
	}

	@Override
	protected void setupInitialState() {}
	@Override
	protected void teardown() {}

	@Override
	protected void performInitialUpdate(WarningListener listener) {
		Set<Long> nodeIds = mWarnings.keySet();
		for(Long nodeId : nodeIds) {
			List<Warning> warningList = mWarnings.get(nodeId);
			for(Warning warning : warningList) {
				listener.onWarningReceived(nodeId, warning);
			}
		}
	}

	@Override
	public void onConnecting() {}
	@Override
	public void onConnected() {}
	@Override
	public void onDisconnected() {}

	@Override
	public void onNodeAdded(Node node) {
		mWarnings.put(node.id, new ArrayList<Warning>());
	}

	@Override
	public void onNodesAdded(List<Node> nodes) {
		for(Node node : nodes) {
			onNodeAdded(node);
		}
	}

	@Override
	public void onNodeRemoved(Node node) {
		mWarnings.remove(node.id);
	}

	@Override
	public void onNodeListDownload() {}
	@Override
	public void onNodeListDownloadFailed() {}

	@Override
	public void afterNodeListDownload(List<Node> nodes) {
		onNodesAdded(nodes);
	}

	@Override
	public void onMessageReceived(Message<ServerProtocolParameters> message) {}
	@Override
	public void onMessageSent(Message<ClientProtocolParameters> message) {}

	@Override
	public List<Warning> getActiveWarnings(long nodeId) {
		return mWarnings.get(nodeId);
	}

	@Override
	public void dismissWarning(long nodeId, long warningId) {
		if(!mWarnings.containsKey(nodeId)) return;
		Warning selectedWarning = null;
		for(Warning warning : mWarnings.get(nodeId)) {
			if(warning.warningId == warningId) {
				selectedWarning = warning;
			}
		}
		if(selectedWarning == null) return;
		final Warning finalSelectedWarning = selectedWarning;
		ClientWarningMessage message = new ClientWarningMessage(mMessageManager.getProtocolParameters(), warningId);
		mMessageManager.sendMessage(message, new PacketSentListener() {
			public void onUnbind(Class<? extends IListener> listenerClass) {}
			public void onBind(Class<? extends IListener> listenerClass) {}
			public void onSendNoServerPort(int packetId) {}
			public void onSendNoServerAddress(int packetId) {}
			public void onSendIoException(int packetId) {}
			public void onSendError(int packetId) {}
			@Override
			public void onPacketSent(int packetId) {
				mWarnings.remove(finalSelectedWarning);
			}
		});
	}

	@Override
	public void dismissWarnings(long nodeId) {
		for(Warning warning : mWarnings.get(nodeId)) {
			dismissWarning(nodeId, warning.warningId);
		}
	}
}
