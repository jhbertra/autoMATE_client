package com.automate.client.views.node;

import java.util.ArrayList;
import java.util.List;

import com.automate.client.R;
import com.automate.client.views.AbstractViewService;
import com.automate.client.views.node.NodeService.NodeServiceBinder;
import com.automate.protocol.models.Status;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.Messenger;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class NodeActivity extends Activity implements Callback {
	
	public static final String NODE_ID = "Node Id";

	private NodeService mService;
	
	private ListView mListView;
	private Button mButton;
	private TextView mNameView;
	
	private StatusAdapter mStatusAdapter;
	private CommandAdapter mCommandAdapter;

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = ((NodeServiceBinder)service).getService();
			mCommandAdapter = new CommandAdapter(NodeActivity.this, R.layout.command_list_item, R.id.node_command_name, mService.getCommands());
			mNameView.setText(mService.getNode().name);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.node_view);
		
		this.mListView = (ListView) findViewById(R.id.node_status_list);
		this.mButton = (Button) findViewById(R.id.node_command_button);
		this.mNameView = (TextView) findViewById(R.id.node_name);
		
		mStatusAdapter = new StatusAdapter(NodeActivity.this, R.layout.status_list_item, R.id.node_status_name, 
				R.id.node_status_value, new ArrayList<Status<?>>());
		mListView.setAdapter(mStatusAdapter);
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		Intent intent = new Intent(this, NodeService.class);
		intent.putExtra(AbstractViewService.MESSENGER, new Messenger(new Handler(this)));
		intent.putExtra(NodeService.NODE_ID, getIntent().getLongExtra(NODE_ID, -1));
		startService(intent);
		bindService(intent, mConnection , BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		unbindService(mConnection);
	}

	public void onSwitchList(View v) {
		if(this.mListView.getAdapter() == mStatusAdapter) {
			this.mListView.setAdapter(mCommandAdapter);
			this.mButton.setText(R.string.button_status);
		} else {
			this.mListView.setAdapter(mStatusAdapter);
			this.mButton.setText(R.string.button_commands);
		}
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case NodeService.CONNECTED:
			
			break;

		case NodeService.CONNECTING:
			
			break;

		case NodeService.DISCONNECTED:
			
			break;

		case NodeService.NEW_WARNING:
			
			break;

		case NodeService.STATUS_UPDATED:
			this.mStatusAdapter.setStatuses((List<Status<?>>) msg.obj);
			break;

		case NodeService.STATUS_UPDATE_CANCELLED:
			break;

		case NodeService.STATUS_UPDATE_FAILED:
			break;

		case NodeService.STATUS_UPDATING:
			break;

		case NodeService.COMMAND_SENT:
			break;

		case NodeService.COMMAND_LOST:
			break;

		case NodeService.COMMAND_FAILED:
			break;

		case NodeService.COMMAND_SUCCESS:
			break;
			
		default:
			return false;
		}
		return true;
	}
	
}
