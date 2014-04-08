package com.automate.client.views.discovery;

import java.util.List;

import com.automate.client.R;
import com.automate.client.managers.pairing.DeviceInfo;
import com.automate.client.views.discovery.DiscoveryService.DiscoveryServiceBinder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class DiscoveryActivity extends ListActivity implements ServiceConnection, Callback {

	private static final int REQUEST_ENABLE_BT = RESULT_FIRST_USER;

	private Dialog mDialog;

	private ArrayAdapter<DeviceInfo.Descriptor> mAdapter;
	
	private DiscoveryService mService;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.pairing, menu);
		return true;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.pairing_refresh:
			mService.scan();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAdapter = new ArrayAdapter<DeviceInfo.Descriptor>(this, R.layout.discovery_list_item, R.id.discovery_list_name);
		this.setListAdapter(mAdapter);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Intent serviceIntent = new Intent(this, DiscoveryService.class);
		Messenger messenger = new Messenger(new Handler(this));
		serviceIntent.putExtra(DiscoveryService.MESSENGER, messenger);
		startService(serviceIntent);
		bindService(serviceIntent, this, 0);
	}

	@Override
	protected void onStop() {
		super.onStop();
		unbindService(this);
		mService.stopSelf();
	}

	/* (non-Javadoc)
	 * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 */
	@Override
	protected void onListItemClick(ListView l, View v, final int position, long id) {
		mDialog = new AlertDialog.Builder(this)
		.setTitle(R.string.pair_confirm_title)
		.setMessage(getResources().getString(R.string.pair_confirm_message, mAdapter.getItem(position).toString()))
		.setPositiveButton(R.string.button_ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mService.pair(mAdapter.getItem(position).enclosingInstance());
				dialog.dismiss();
			}
		})
		.setNegativeButton(R.string.button_cancel, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).show();
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		this.mService = ((DiscoveryServiceBinder)service).getService();
		mService.scan();
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		finish();
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch(msg.what) {
		case DiscoveryService.Messages.BLUETOOTH_DISABLED:
			Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			break;

		case DiscoveryService.Messages.BLUETOOTH_UNAVAILABLE:
			mDialog = new AlertDialog.Builder(this)
			.setTitle(R.string.bluetooth_unavailable_title)
			.setMessage(R.string.bluetooth_unavailable_message)
			.setNeutralButton(R.string.button_ok, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					finish();
				}
			}).show();
			break;

		case DiscoveryService.Messages.DEVICES_DISCOVERRED:
			List<DeviceInfo> devices = (List<DeviceInfo>) msg.obj;
			if(devices != null) {
				mAdapter.clear();
				for(DeviceInfo info : devices) {
					mAdapter.add(info.descriptor);
				}
			}
			break;

		case DiscoveryService.Messages.NEW_DEVICE_PARIED:
			Toast.makeText(this, R.string.new_device_added, Toast.LENGTH_SHORT).show();
			mDialog.dismiss();
			finish();
			break;

		case DiscoveryService.Messages.NO_DEVICES:
			mAdapter.clear();
			break;

		case DiscoveryService.Messages.PAIRING:
			final EditText input = new EditText(this);
			mDialog = new AlertDialog.Builder(this)
			.setTitle(R.string.name_device_title)
			.setMessage(R.string.name_device_message)
			.setView(input)
			.setPositiveButton(R.string.button_ok, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mService.setDeviceName(input.getText().toString());
					dialog.dismiss();
					mDialog = ProgressDialog.show(DiscoveryActivity.this, getResources().getString(R.string.pairing_progress_title), 
							getResources().getString(R.string.pairing_progress_message), true, false);
				}
			})
			.setNegativeButton(R.string.button_cancel, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			}).show();
			break;

		case DiscoveryService.Messages.PAIRING_FAILED:
			mDialog.dismiss();
			mDialog = new AlertDialog.Builder(this)
			.setTitle(R.string.device_pair_fail_title)
			.setMessage(R.string.device_pair_fail_message)
			.setNeutralButton(R.string.button_ok, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					mService.scan();
				}
			}).show();
			break;

		case DiscoveryService.Messages.SCANNING:
	        setProgressBarIndeterminateVisibility(true);
	        setTitle(R.string.scanning);
			break;
			
			default:
				return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
		case REQUEST_ENABLE_BT:
			if(resultCode == RESULT_OK) {
				mService.scan();
			} else {
				Toast.makeText(this, R.string.bluetooth_not_enabled, Toast.LENGTH_SHORT).show();
				finish();
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}
