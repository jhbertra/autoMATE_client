package com.automate.client.views.nodelist;

import java.util.List;

import com.automate.client.authentication.AuthenticationListener;
import com.automate.client.messaging.managers.IMessageManager;
import com.automate.protocol.client.messages.ClientNodeListMessage;
import com.automate.protocol.models.Node;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class NodeListService extends Service implements AuthenticationListener {

	public static final int NODE_ADDED = 0;
	public static final int NODES_ADDED = 1;
	public static final int NODE_REMOVED = 2;
	public static final int AUTHENTICATING = 3;
	public static final int DOWNLOADING_LIST = 4;
	public static final int NO_NODES = 5;
	public static final int AUTHENTICATION_FAILED = 6;
	public static final int DISCONNECTED = 7;
	public static final int WARNING_RECEIVED = 8;
	
	public static final String MESSENGER = "messenger";
	private static final String NODE_ID = "node id";
	private static final String WARNING = "warning";
	
	private IBinder mBinder = new NodeListServiceBinder();
	private Messenger mMessenger;
	private IMessageManager mMessageManager;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mMessenger = intent.getParcelableExtra(MESSENGER);
		
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	public void registerListeners() {
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void downloadNodeList() {
		ClientNodeListMessage nodeListMessage = new ClientNodeListMessage(mMessageManager.getProtocolParameters());
		mMessageManager.sendMessage(nodeListMessage);
	}
	
	@Override
	public void onAuthenticated(String sessionKey, String username) {
		downloadNodeList();
	}

	@Override
	public void onAuthenticationFailed(String failureMessage) {
		notifyAuthenticationFailed();
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

	private void notifyAuthenticating() {
		Message message = new Message();
		message.what = AUTHENTICATING;
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

	private void notifyAuthenticationFailed() {
		Message message = new Message();
		message.what = AUTHENTICATION_FAILED;
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

	private void notifyWarningReceived(long nodeId, String warning) {
		Message message = new Message();
		message.what = DOWNLOADING_LIST;
		Bundle data = new Bundle();
		data.putLong(NODE_ID, nodeId);
		data.putString(WARNING, warning);
		message.setData(data);
		try {
			mMessenger.send(message);
		} catch (RemoteException e) {
			// ignore, remote messaging not used.
		}
	}
	
	public class NodeListServiceBinder extends Binder {
		
		public NodeListService getService() {
			return NodeListService.this;
		}
		
	}
	
}
