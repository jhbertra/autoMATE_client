package com.automate.client.views.node;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import com.automate.client.managers.IListener;
import com.automate.client.managers.command.Command;
import com.automate.client.managers.command.CommandListener;
import com.automate.client.managers.command.ICommandManager;
import com.automate.client.managers.connectivity.ConnectionListener;
import com.automate.client.managers.connectivity.IConnectionManager;
import com.automate.client.managers.connectivity.ConnectionManager.ConnectedState;
import com.automate.client.managers.node.INodeManager;
import com.automate.client.managers.node.NodeListener;
import com.automate.client.managers.status.IStatusManager;
import com.automate.client.managers.status.StatusListener;
import com.automate.client.managers.warning.IWarningManager;
import com.automate.client.managers.warning.WarningListener;
import com.automate.client.views.AbstractViewService;
import com.automate.protocol.models.CommandArgument;
import com.automate.protocol.models.Node;
import com.automate.protocol.models.Status;
import com.automate.protocol.models.Warning;

public class NodeService extends AbstractViewService implements CommandListener, StatusListener, WarningListener, ConnectionListener, NodeListener {

	public static final int DISCONNECTED = Activity.RESULT_FIRST_USER;
	public static final int CONNECTING = Activity.RESULT_FIRST_USER + 1;
	public static final int CONNECTED = Activity.RESULT_FIRST_USER + 2;
	public static final int STATUS_UPDATING = Activity.RESULT_FIRST_USER + 3;
	public static final int STATUS_UPDATED = Activity.RESULT_FIRST_USER + 4;
	public static final int NEW_WARNING = Activity.RESULT_FIRST_USER + 5;
	public static final int STATUS_UPDATE_CANCELLED = Activity.RESULT_FIRST_USER + 6;
	public static final int STATUS_UPDATE_FAILED = Activity.RESULT_FIRST_USER + 7;
	public static final int COMMAND_SENT = Activity.RESULT_FIRST_USER + 8;
	public static final int COMMAND_LOST = Activity.RESULT_FIRST_USER + 9;
	public static final int COMMAND_SUCCESS = Activity.RESULT_FIRST_USER + 10;
	public static final int COMMAND_FAILED = Activity.RESULT_FIRST_USER + 11;
	
	public static final String NODE_ID = "Node Id";
	public static final String FAILED_COMMAND = "Failed Command";
	
	private IBinder mBinder = new NodeServiceBinder();
	private ICommandManager mCommandManager;
	private IStatusManager mStatusManager;
	private IWarningManager mWarningManager;
	private IConnectionManager mConnectionManager;
	private INodeManager mNodeManager;

	private ConnectedState mConnectedState = ConnectedState.DISCONNECTED;
	
	private long nodeId;
	
	public void refreshStatus() {
		mStatusManager.requestStatusUpdate(nodeId);
	}
	
	public long sendCommand(Command command, List<CommandArgument<?>> args) {
		return mCommandManager.sendCommand(command, nodeId, args);
	}
	
	public List<Command> getCommands() {
		if(mCommandManager == null) return null;
		List<Command> commands = mCommandManager.getCommandList(nodeId);
		if(commands == null) {
			return new ArrayList<Command>();
		} else {			
			return commands;
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int retVal = super.onStartCommand(intent, flags, startId);
		Message message = new Message();
		message.what = DISCONNECTED;
		try {
			this.mMessenger.send(message);
		} catch (RemoteException e) {}
		this.nodeId = intent.getLongExtra(NODE_ID, -1);
		return retVal;
	}
	
	@Override
	protected void onServiceConnected() {
		mCommandManager = mAutoMateService.getManager(ICommandManager.class);
		mStatusManager = mAutoMateService.getManager(IStatusManager.class);
		mWarningManager = mAutoMateService.getManager(IWarningManager.class);
		mConnectionManager = mAutoMateService.getManager(IConnectionManager.class);
		mNodeManager = mAutoMateService.getManager(INodeManager.class);
		
		mCommandManager.bind(this);
		mStatusManager.bind(this);
		mWarningManager.bind(this);
		mConnectionManager.bind(this);
		mNodeManager.bind(this);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mCommandManager != null) {
			mCommandManager.unbind(this);
			mStatusManager.unbind(this);
			mWarningManager.unbind(this);
			mConnectionManager.unbind(this);
			mNodeManager.unbind(this);
		}
	}

	@Override
	protected void onServiceDisconnected() {
		if(mCommandManager != null) {
			mCommandManager.unbind(this);
			mStatusManager.unbind(this);
			mWarningManager.unbind(this);
			mConnectionManager.unbind(this);
			mNodeManager.unbind(this);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onBind(Class<? extends IListener> listenerClass) {
		if(listenerClass.equals(StatusListener.class)) {
			refreshStatus();
		}
	}

	@Override
	public void onUnbind(Class<? extends IListener> listenerClass) {
		
	}

	@Override
	public void onConnecting() {
		this.mConnectedState = ConnectedState.CONNECTING;
		Message message = new Message();
		message.what = CONNECTING;
		try {
			this.mMessenger.send(message);
		} catch (RemoteException e) {}
	}

	@Override
	public void onConnected() {
		this.mConnectedState = ConnectedState.CONNECTED;
		Message message = new Message();
		message.what = CONNECTED;
		try {
			this.mMessenger.send(message);
		} catch (RemoteException e) {}
	}

	@Override
	public void onDisconnected() {
		this.mConnectedState = ConnectedState.DISCONNECTED;
		Message message = new Message();
		message.what = DISCONNECTED;
		try {
			this.mMessenger.send(message);
		} catch (RemoteException e) {}
	}

	@Override
	public void onWarningReceived(long nodeId, Warning warning) {
		Message message = new Message();
		message.what = NEW_WARNING;
		message.obj = warning;
		try {
			this.mMessenger.send(message);
		} catch (RemoteException e) {}
	}

	@Override
	public void onStatusUpdating(long nodeId) {
		if(nodeId != this.nodeId || mConnectedState == ConnectedState.DISCONNECTED) return;
		Message message = new Message();
		message.what = STATUS_UPDATING;
		try {
			this.mMessenger.send(message);
		} catch (RemoteException e) {}
	}

	@Override
	public void onStatusUpdated(long nodeId, List<Status<?>> statuses) {
		if(nodeId != this.nodeId || mConnectedState == ConnectedState.DISCONNECTED) return;
		Message message = new Message();
		message.what = STATUS_UPDATED;
		message.obj = statuses;
		try {
			this.mMessenger.send(message);
		} catch (RemoteException e) {}
	}

	@Override
	public void onStatusUpdateCancelled(long nodeId) {
		if(nodeId != this.nodeId || mConnectedState == ConnectedState.DISCONNECTED) return;
		Message message = new Message();
		message.what = STATUS_UPDATE_CANCELLED;
		try {
			this.mMessenger.send(message);
		} catch (RemoteException e) {}
	}

	@Override
	public void onStatusUpdateFailed(long nodeId) {
		if(nodeId != this.nodeId || mConnectedState == ConnectedState.DISCONNECTED) return;
		Message message = new Message();
		message.what = STATUS_UPDATE_FAILED;
		try {
			this.mMessenger.send(message);
		} catch (RemoteException e) {}
	}

	@Override
	public void onCommandIssued(long nodeId, long commandId) {
		if(nodeId != this.nodeId) return;
		Message message = new Message();
		message.what = COMMAND_SENT;
		message.obj = commandId;
		try {
			this.mMessenger.send(message);
		} catch (RemoteException e) {}
	}

	@Override
	public void onCommandLost(long nodeId, long commandId) {
		if(nodeId != this.nodeId) return;
		Message message = new Message();
		message.what = COMMAND_LOST;
		message.obj = commandId;
		Bundle data = new Bundle();
		data.putString(FAILED_COMMAND, mCommandManager.getCommandNameForCommandId(commandId));
		message.setData(data);
		try {
			this.mMessenger.send(message);
		} catch (RemoteException e) {}
	}

	@Override
	public void onCommandSuccess(long nodeId, long commandId) {
		if(nodeId != this.nodeId) return;
		Message message = new Message();
		message.what = COMMAND_SUCCESS;
		message.obj = commandId;
		try {
			this.mMessenger.send(message);
		} catch (RemoteException e) {}
	}

	@Override
	public void onCommandFailure(long nodeId, long commandId, String failureMessage) {
		if(nodeId != this.nodeId) return;
		Message message = new Message();
		message.what = COMMAND_FAILED;
		message.obj = failureMessage;
		Bundle data = new Bundle();
		data.putString(FAILED_COMMAND, mCommandManager.getCommandNameForCommandId(commandId));
		message.setData(data);
		try {
			this.mMessenger.send(message);
		} catch (RemoteException e) {}
	}
	
	public Node getNode() {
		return mNodeManager.getNodeById(nodeId);
	}
	
	public long getNodeId() {
		return nodeId;
	}

	public void setNodeId(long nodeId) {
		this.nodeId = nodeId;
	}

	public class NodeServiceBinder extends Binder {
		
		public NodeService getService() {
			return NodeService.this;
		}
		
	}

	@Override
	public void onNodeAdded(Node node) {
		
	}

	@Override
	public void onNodesAdded(List<Node> nodes) {
		
	}

	@Override
	public void onNodeRemoved(Node node) {
		
	}

	@Override
	public void onNodeListDownload() {
		
	}

	@Override
	public void onNodeListDownloadFailed() {
		
	}

	@Override
	public void afterNodeListDownload(List<Node> nodes) {
		
	}
	
}
