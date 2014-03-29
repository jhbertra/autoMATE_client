package com.automate.client.managers.status;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.automate.client.managers.IListener;
import com.automate.client.managers.ManagerBase;
import com.automate.client.managers.connectivity.ConnectionListener;
import com.automate.client.managers.connectivity.IConnectionManager;
import com.automate.client.managers.messaging.IMessageManager;
import com.automate.client.managers.node.INodeManager;
import com.automate.client.managers.node.NodeListener;
import com.automate.client.managers.packet.PacketSentListener;
import com.automate.protocol.Message;
import com.automate.protocol.client.ClientProtocolParameters;
import com.automate.protocol.client.messages.ClientStatusUpdateMessage;
import com.automate.protocol.models.Node;
import com.automate.protocol.models.Status;
import com.automate.protocol.server.ServerProtocolParameters;

public class StatusManager extends ManagerBase<StatusListener> implements IStatusManager {
	
	private HashMap<Long, List<Status<?>>> mStatuses = new HashMap<Long, List<Status<?>>>();
	
	private IMessageManager mMessageManager;
	
	private IConnectionManager mConnectivityManager;
	
	private INodeManager mNodeManager;
	
	public StatusManager(IMessageManager messageManager, IConnectionManager connectionManager, INodeManager nodeManager) {
		super(StatusListener.class);
		this.mMessageManager = messageManager;
		this.mConnectivityManager = connectionManager;
		this.mNodeManager = nodeManager;
	}

	@Override
	protected void performInitialUpdate(StatusListener listener) {
		for(Long nodeId: mStatuses.keySet()) {
			listener.onStatusUpdated(nodeId, mStatuses.get(nodeId));
		}
	}

	@Override
	protected void unbindSelf() {
		mMessageManager.unbind(this);
		mConnectivityManager.unbind(this);
		mNodeManager.unbind(this);
	}

	@Override
	protected void bindSelf() {
		mMessageManager.bind(this);
		mConnectivityManager.bind(this);
		mNodeManager.bind(this);
	}

	@Override
	protected void setupInitialState() {
	}

	@Override
	protected void teardown() {
		this.mStatuses.clear();
	}

	@Override
	public void onStatusUpdating(long nodeId) {
		synchronized (mListeners) {
			for(StatusListener listener : mListeners) {
				listener.onStatusUpdating(nodeId);
			}
		}
	}

	@Override
	public void onStatusUpdated(long nodeId, List<Status<?>> statuses) {
		mStatuses.put(nodeId, statuses);
		synchronized (mListeners) {
			for(StatusListener listener : mListeners) {
				listener.onStatusUpdated(nodeId, statuses);
			}
		}
	}
	
	@Override
	public void onStatusUpdateCancelled(long nodeId) {
		synchronized (mListeners) {
			for(StatusListener listener : mListeners) {
				listener.onStatusUpdateCancelled(nodeId);
			}
		}
	}

	@Override
	public void onStatusUpdateFailed(long nodeId) {
		synchronized (mListeners) {
			for(StatusListener listener : mListeners) {
				listener.onStatusUpdateFailed(nodeId);
			}
		}
	}

	@Override
	public List<Status<?>> getStatuses(long nodeId) {
		return mStatuses.get(nodeId);
	}

	@Override
	public void requestStatusUpdate(final long nodeId) {
		ClientStatusUpdateMessage message = new ClientStatusUpdateMessage(mMessageManager.getProtocolParameters(), nodeId);
		mMessageManager.sendMessage(message, new PacketSentListener() {
			@Override
			public void onUnbind(Class<? extends IListener> listenerClass) {}
			@Override
			public void onBind(Class<? extends IListener> listenerClass) {}
			@Override
			public void onSendNoServerPort(int packetId) {
				onStatusUpdateFailed(nodeId);
			}
			@Override
			public void onSendNoServerAddress(int packetId) {
				onStatusUpdateFailed(nodeId);
			}
			@Override
			public void onSendIoException(int packetId) {
				onStatusUpdateFailed(nodeId);
			}
			@Override
			public void onSendError(int packetId) {
				onStatusUpdateFailed(nodeId);
			}
			@Override
			public void onPacketSent(int packetId) {
				onStatusUpdating(nodeId);
			}
		});
	}

	@Override
	public void onBind(Class<? extends IListener> listenerClass) {
		
	}

	@Override
	public void onUnbind(Class<? extends IListener> listenerClass) {
		if(listenerClass.equals(NodeListener.class) || listenerClass.equals(ConnectionListener.class)) {
			mStatuses.clear();
		}
	}

	@Override
	public void onConnecting() {}

	@Override
	public void onConnected() {}

	@Override
	public void onDisconnected() {
		this.mStatuses.clear();
	}

	@Override
	public void onNodeAdded(Node node) {
		requestStatusUpdate(node.id);
	}

	@Override
	public void onNodesAdded(List<Node> nodes) {
		for(Node node : nodes) {
			requestStatusUpdate(node.id);
		}
	}

	@Override
	public void onNodeRemoved(Node node) {
		mStatuses.remove(node.id);
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

}
