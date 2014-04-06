package com.automate.client.views.nodelist;

import java.util.ArrayList;
import java.util.List;

import com.automate.client.R;
import com.automate.client.views.node.NodeActivity;
import com.automate.client.views.nodelist.NodeListService.NodeListServiceBinder;
import com.automate.protocol.models.Node;
import com.automate.protocol.models.Warning;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class NodeListActivity extends Activity implements Callback, ServiceConnection, OnItemClickListener {

	private NodeListAdapter mAdapter;
	private NodeListService mService;
	
	private View mEmptyView;
	private View mDisconnectedView;
	private View mLoadingView;
	
	private ListView mListView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.node_list_view);
		this.mAdapter = new NodeListAdapter(this, R.layout.node_list_item, R.id.list_node_name, R.id.list_node_manufacturer, 
				R.id.list_node_icon, new ArrayList<Node>());
		
		this.mEmptyView = findViewById(R.id.list_empty_view);
		this.mDisconnectedView = findViewById(R.id.list_disconnected_view);
		this.mLoadingView = findViewById(R.id.list_loading_view);
		
		this.mListView = (ListView) findViewById(R.id.node_list);
		
		this.mListView.setOnItemClickListener(this);
		
		this.mListView.setAdapter(mAdapter);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Intent serviceIntent = new Intent(this, NodeListService.class);
		Messenger messenger = new Messenger(new Handler(this));
		serviceIntent.putExtra(NodeListService.MESSENGER, messenger);
		startService(serviceIntent);
		bindService(serviceIntent, this, 0);
	}

	@Override
	protected void onStop() {
		super.onStop();
		unbindService(this);
		mService.stopSelf();
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
		mDisconnectedView.setVisibility(View.GONE);
		mEmptyView.setVisibility(View.GONE);
		mLoadingView.setVisibility(View.GONE);
		mListView.setVisibility(View.VISIBLE);
		switch(msg.what) {
		case NodeListService.NODE_ADDED:
			if(mAdapter.contains((Node) msg.obj)) return true;
			mAdapter.add((Node) msg.obj);
			break;

		case NodeListService.NODES_ADDED:
			List<Node> nodes = (List<Node>) msg.obj;
			for(Node node : nodes) {
				if(mAdapter.contains(node)) return true;
			}
			mAdapter.addAll(nodes);
			break;

		case NodeListService.NODE_REMOVED:
			mAdapter.remove((Node) msg.obj);
			break;
			
		case NodeListService.NO_NODES:
			this.mAdapter.clear();
			mListView.setVisibility(View.GONE);
			mEmptyView.setVisibility(View.VISIBLE);
			break;
			
		case NodeListService.CONNECTING:
			this.mAdapter.clear();
			((TextView)this.mLoadingView.findViewById(R.id.list_node_loading_message)).setText(R.string.node_list_connecting);
			mListView.setVisibility(View.GONE);
			mLoadingView.setVisibility(View.VISIBLE);
			break;
			
		case NodeListService.DISCONNECTED:
			this.mAdapter.clear();
			mListView.setVisibility(View.GONE);
			mDisconnectedView.setVisibility(View.VISIBLE);
			break;
			
		case NodeListService.DOWNLOADING_LIST:
			this.mAdapter.clear();
			((TextView)this.mLoadingView.findViewById(R.id.list_node_loading_message)).setText(R.string.node_list_downloading);
			mListView.setVisibility(View.GONE);
			mLoadingView.setVisibility(View.VISIBLE);
			break;			
			
		case NodeListService.WARNING_RECEIVED:
			new AlertDialog.Builder(this)
			.setTitle("Warning Received")
			.setMessage(((Warning) msg.obj).message)
			.setPositiveButton(R.string.button_ok, new Dialog.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			}).show();
			break;
			
		default:
			return false;
		}
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent(this, NodeActivity.class);
		intent.putExtra(NodeActivity.NODE_ID, id);
		startActivity(intent);
	}
	
}
