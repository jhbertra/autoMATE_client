package com.automate.client.views.nodelist;

import java.util.ArrayList;

import com.automate.client.R;
import com.automate.client.views.nodelist.NodeListService.NodeListServiceBinder;
import com.automate.protocol.models.Node;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.view.LayoutInflater;
import android.view.View;

public class NodeListActivity extends ListActivity implements Callback, ServiceConnection {

	private NodeListAdapter mAdapter;
	private NodeListService mService;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.mAdapter = new NodeListAdapter(this, R.layout.node_list_item, R.id.list_node_name, R.id.list_node_manufacturer, 
				R.id.list_node_icon, new ArrayList<Node>());
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View emptyView = inflater.inflate(R.layout.list_loading_view, null);
		this.getListView().setEmptyView(emptyView);
		
		Intent serviceIntent = new Intent(this, NodeListService.class);
		Messenger messenger = new Messenger(new Handler(this));
		serviceIntent.putExtra(NodeListService.MESSENGER, messenger);
		startService(serviceIntent);
		bindService(serviceIntent, this, 0);
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		this.mService = ((NodeListServiceBinder)service).getService();
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		finish();
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch(msg.what) {
		case NodeListService.NODE_ADDED:
			mAdapter.add((Node) msg.obj);
		
		default:
			return false;
		}
	}
	
}
