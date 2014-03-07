package com.automate.client.views.nodelist;

import java.util.List;

import com.automate.client.managers.IListener;
import com.automate.client.managers.connectivity.ConnectionListener;
import com.automate.client.managers.messaging.IMessageManager;
import com.automate.client.managers.node.INodeManager;
import com.automate.client.managers.node.NodeListener;
import com.automate.client.managers.warning.IWarningManager;
import com.automate.client.managers.warning.WarningListener;
import com.automate.client.views.AbstractViewService;
import com.automate.protocol.client.messages.ClientNodeListMessage;
import com.automate.protocol.models.Node;
import com.automate.protocol.models.Warning;

import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

public class NodeListService extends AbstractViewService implements ConnectionListener, NodeListener, WarningListener {

	public static final int NODE_ADDED = 0;
	public static final int NODES_ADDED = 1;
	public static final int NODE_REMOVED = 2;
	public static final int CONNECTING = 3;
	public static final int DOWNLOADING_LIST = 4;
	public static final int NO_NODES = 5;
	public static final int DISCONNECTED = 6;
	public static final int WARNING_RECEIVED = 7;
	
	private static final String NODE_ID = "node id";
	private static final String WARNING = "warning";
	
	private IBinder mBinder = new NodeListServiceBinder();
	private IMessageManager mMessageManager;
	private INodeManager mNodeManager;
	private IWarningManager mWarningManager;

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void downloadNodeList() {
		ClientNodeListMessage nodeListMessage = new ClientNodeListMessage(mMessageManager.getProtocolParameters());
		mMessageManager.sendMessage(nodeListMessage);
	}
	
	private void notifyNodeAdded(Node node) {
		Message message = new Message();
		message.what = NODE_ADDED;
		message.obj = node;
		try {
			mMessenger.send(message);
		} catch (RemoteException e) {
			// ignore, remote messaging not used.
		}
	}
	
	private void notifyNodesAdded(List<Node> nodes) {
		Message message = new Message();
		message.what = NODES_ADDED;
		message.obj = nodes;
		try {
			mMessenger.send(message);
		} catch (RemoteException e) {
			// ignore, remote messaging not used.
		}
	}
	
	private void notifyNodeRemoved(Node node) {
		Message message = new Message();
		message.what = NODE_REMOVED;
		message.obj = node;
		try {
			mMessenger.send(message);
		} catch (RemoteException e) {
			// ignore, remote messaging not used.
		}
	}

	private void notifyConnecting() {
		Message message = new Message();
		message.what = CONNECTING;
		try {
			mMessenger.send(message);
		} catch (RemoteException e) {
			// ignore, remote messaging not used.
		}
	}

	private void notifyDownloadingList() {
		Message message = new Message();
		message.what = DOWNLOADING_LIST;
		try {
			mMessenger.send(message);
		} catch (RemoteException e) {
			// ignore, remote messaging not used.
		}
	}

	private void notifyNoNodes() {
		Message message = new Message();
		message.what = NO_NODES;
		try {
			mMessenger.send(message);
		} catch (RemoteException e) {
			// ignore, remote messaging not used.
		}
	}

	private void notifyDisconnected() {
		Message message = new Message();
		message.what = DISCONNECTED;
		try {
			mMessenger.send(message);
		} catch (RemoteException e) {
			// ignore, remote messaging not used.
		}
	}

	private void notifyWarningReceived(long nodeId, Warning warning) {
		Message message = new Message();
		message.what = DOWNLOADING_LIST;
		message.obj = warning;
		Bundle data = new Bundle();
		data.putLong(NODE_ID, nodeId);
		message.setData(data);
		try {
			mMessenger.send(message);
		} catch (RemoteException e) {
			// ignore, remote messaging not used.
		}
	}

	@Override
	public void onBind(Class<? extends IListener> listenerClass) {}

	@Override
	public void onUnbind(Class<? extends IListener> listenerClass) {
		if(listenerClass.equals(ConnectionListener.class)) {
			notifyDisconnected();
		}
	}

	@Override
	public void onConnecting() {
		notifyConnecting();
	}

	@Override
	public void onConnected() {
		downloadNodeList();
	}

	@Override
	public void onDisconnected() {
		notifyDisconnected();
	}
	
	public class NodeListServiceBinder extends Binder {
		
		public NodeListService getService() {
			return NodeListService.this;
		}
		
	}

	@Override
	public void onWarningReceived(long nodeId, Warning warning) {
		notifyWarningReceived(nodeId, warning);
	}

	@Override
	public void onNodeAdded(Node node) {
		notifyNodeAdded(node);
	}

	@Override
	public void onNodesAdded(List<Node> nodes) {
		notifyNodesAdded(nodes);
	}

	@Override
	public void onNodeRemoved(Node node) {
		notifyNodeRemoved(node);
	}

	@Override
	public void onNodeListDownload() {
		notifyDownloadingList();
	}

	@Override
	public void onNodeListDownloadFailed() {
		notifyDisconnected();
	}

	@Override
	public void afterNodeListDownload(List<Node> nodes) {
		onNodesAdded(nodes);
	}

	@Override
	protected void onServiceConnected() {
		mNodeManager = mAutoMateService.getManager(INodeManager.class);
		mMessageManager = mAutoMateService.getManager(IMessageManager.class);
		mWarningManager = mAutoMateService.getManager(IWarningManager.class);
		
		mNodeManager.bind(this);
		mWarningManager.bind(this);
	}

	@Override
	protected void onServiceDisconnected() {
		mNodeManager.unbind(this);
		mWarningManager.unbind(this);
		
		mNodeManager = null;
		mMessageManager = null;
		mWarningManager = null;
	}
	
}
