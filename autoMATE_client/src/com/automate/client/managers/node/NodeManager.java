package com.automate.client.managers.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.automate.client.managers.IListener;
import com.automate.client.managers.ManagerBase;
import com.automate.client.managers.connectivity.IConnectionManager;
import com.automate.client.managers.messaging.IMessageManager;
import com.automate.client.managers.packet.PacketSentListener;
import com.automate.protocol.Message;
import com.automate.protocol.client.ClientProtocolParameters;
import com.automate.protocol.client.messages.ClientNodeListMessage;
import com.automate.protocol.models.Node;
import com.automate.protocol.server.ServerProtocolParameters;

public class NodeManager extends ManagerBase<NodeListener> implements INodeManager, PacketSentListener {

	public enum DownloadState {
		DOWNLOAD_NOT_STARTED,
		DOWNLOADING,
		DOWNLOAD_COMPLETE,
		DOWNLOAD_FAILD
	}

	private DownloadState mDownloadState = DownloadState.DOWNLOAD_NOT_STARTED;

	private HashMap<Long, Node> mNodes = new HashMap<Long, Node>();

	private IMessageManager mMessageManager;
	private IConnectionManager mConnectionManager;
	
	public NodeManager(IMessageManager messageManager, IConnectionManager connectionManager) {
		super(NodeListener.class);
		this.mMessageManager = messageManager;
		this.mConnectionManager = connectionManager;
	}

	@Override
	protected void unbindSelf() {
		mMessageManager.unbind(this);
		mConnectionManager.unbind(this);
	}

	@Override
	protected void bindSelf() {
		mMessageManager.bind(this);
		mConnectionManager.bind(this);
	}

	@Override
	protected void setupInitialState() {
		this.mDownloadState = DownloadState.DOWNLOAD_NOT_STARTED;
	}

	@Override
	protected void teardown() {
		this.mNodes.clear();
	}

	@Override
	protected void performInitialUpdate(NodeListener listener) {
		if(mDownloadState == DownloadState.DOWNLOADING) {
			listener.onNodeListDownload();
		} else if(mDownloadState == DownloadState.DOWNLOAD_COMPLETE) {
			listener.afterNodeListDownload(new ArrayList<Node>(mNodes.values()));
		}	
	}

	@Override
	public Node getNodeById(long nodeId) {
		return mNodes.get(nodeId);
	}

	@Override
	public void startNodeListDownload() {
		ClientNodeListMessage message = new ClientNodeListMessage(mMessageManager.getProtocolParameters());
		mMessageManager.sendMessage(message, this);
	}

	@Override
	public void onNodeAdded(Node node) {
		mNodes.put(node.id, node);
		synchronized (mListeners) {
			for(NodeListener listener : mListeners) {
				listener.onNodeAdded(node);
			}
		}
	}

	@Override
	public void onNodesAdded(List<Node> nodes) {
		for(Node node : nodes) {
			mNodes.put(node.id, node);
		}
		synchronized (mListeners) {
			for(NodeListener listener : mListeners) {
				listener.onNodesAdded(nodes);
			}
		}
	}

	@Override
	public void onNodeRemoved(Node node) {
		mNodes.remove(node.id);
		synchronized (mListeners) {
			for(NodeListener listener : mListeners) {
				listener.onNodeRemoved(node);
			}
		}
	}

	@Override
	public void onNodeListDownload() {
		this.mDownloadState = DownloadState.DOWNLOADING;
		synchronized (mListeners) {
			for(NodeListener listener : mListeners) {
				listener.onNodeListDownload();
			}
		}
	}

	@Override
	public void onNodeListDownloadFailed() {
		this.mDownloadState = DownloadState.DOWNLOAD_FAILD;
		synchronized (mListeners) {
			for(NodeListener listener : mListeners) {
				listener.onNodeListDownloadFailed();
			}
		}
	}

	@Override
	public void afterNodeListDownload(List<Node> nodes) {
		this.mDownloadState = DownloadState.DOWNLOAD_COMPLETE;
		for(Node node : nodes) {
			mNodes.put(node.id, node);
		}
		synchronized (mListeners) {
			for(NodeListener listener : mListeners) {
				listener.afterNodeListDownload(nodes);
			}
		}
	}

	@Override
	public void onBind(Class<? extends IListener> listenerClass) {
	}

	@Override
	public void onUnbind(Class<? extends IListener> listenerClass) {
		if(listenerClass.equals(NodeListener.class)) {
			this.mDownloadState = DownloadState.DOWNLOAD_NOT_STARTED;
		}
	}

	@Override
	public void onMessageReceived(Message<ServerProtocolParameters> message) {
	}

	@Override
	public void onMessageSent(Message<ClientProtocolParameters> message) {
	}

	@Override
	public void onConnecting() {}

	@Override
	public void onConnected() {
		startNodeListDownload();
	}

	@Override
	public void onDisconnected() {
		this.mDownloadState = DownloadState.DOWNLOAD_NOT_STARTED;
	}

	@Override
	public void onPacketSent(int packetId) {
		this.onNodeListDownload();
	}

	@Override
	public void onSendIoException(int packetId) {
		this.onNodeListDownloadFailed();
	}

	@Override
	public void onSendNoServerAddress(int packetId) {
		this.onNodeListDownloadFailed();
	}

	@Override
	public void onSendNoServerPort(int packetId) {
		this.onNodeListDownloadFailed();
	}

	@Override
	public void onSendError(int packetId) {
		this.onNodeListDownloadFailed();
	}

}
