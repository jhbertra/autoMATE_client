package com.automate.client.views.node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.automate.client.R;
import com.automate.client.managers.command.ArgumentRange;
import com.automate.client.managers.command.ArgumentRange.EnumRange;
import com.automate.client.managers.command.ArgumentRange.NumericRange;
import com.automate.client.managers.command.ArgumentSpecification;
import com.automate.client.managers.command.Command;
import com.automate.client.views.AbstractViewService;
import com.automate.client.views.node.NodeService.NodeServiceBinder;
import com.automate.protocol.models.CommandArgument;
import com.automate.protocol.models.Status;
import com.automate.protocol.models.Type;
import com.automate.protocol.models.Warning;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.Messenger;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

public class NodeActivity extends Activity implements Callback, OnItemClickListener {

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
			mService.refreshStatus();
		}
	};

	private AlertDialog dialog;

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
		mListView.setOnItemClickListener(this);
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

		case NodeService.STATUS_UPDATED:
			this.mStatusAdapter.setStatuses((List<Status<?>>) msg.obj);
			this.mCommandAdapter.setStatuses((List<Status<?>>) msg.obj);
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
			new AlertDialog.Builder(this)
			.setTitle(R.string.command_lost_title)
			.setMessage(getResources().getString(R.string.command_lost_message, msg.getData().getString(NodeService.FAILED_COMMAND)))
			.setNeutralButton(R.string.button_ok, new Dialog.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			}).show();
			break;

		case NodeService.COMMAND_FAILED:
			new AlertDialog.Builder(this)
			.setTitle(R.string.command_failure_title)
			.setMessage(getResources().getString(R.string.command_failure_message, msg.getData().getString(NodeService.FAILED_COMMAND)) + 
					"\n\n" + msg.obj)
					.setNeutralButton(R.string.button_ok, new Dialog.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					}).show();
			break;

		case NodeService.COMMAND_SUCCESS:
			mService.refreshStatus();
			break;

		default:
			return false;
		}
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if(mListView.getAdapter() == this.mCommandAdapter) {
			final Command command = mCommandAdapter.getItem(position);
			new AlertDialog.Builder(this)
			.setTitle(R.string.command_confirm_title)
			.setMessage(getResources().getString(R.string.command_confirm_message, command.name))
			.setPositiveButton(R.string.button_ok, new Dialog.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					onConfirm(command);
				}
			})
			.setNegativeButton(R.string.button_cancel, new Dialog.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			}).show();
		}
	}

	private void onConfirm(Command command) {
		if(command.args == null || command.args.size() == 0) {
			mService.sendCommand(command, null);
		} else {
			List<CommandArgument<?>> commandArgs = new ArrayList<CommandArgument<?>>();
			Iterator<ArgumentSpecification> args = command.args.iterator();
			getArgValue(command, args, commandArgs);
		}
	}

	private void getArgValue(final Command command, final Iterator<ArgumentSpecification> args, final List<CommandArgument<?>> commandArgs) {
		if(!args.hasNext()) {
			mService.sendCommand(command, commandArgs);
		} else {
			ArgumentSpecification arg = args.next();
			View contentView = getContentView(arg, commandArgs);
			TextView message = (TextView)contentView.findViewById(R.id.command_set_arg_message);
			message.setText(getResources().getString(R.string.command_set_arg_message, arg.mName));
			Button cancelButton = (Button)contentView.findViewById(R.id.command_set_arg_cancel);
			cancelButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});
			this.dialog = new AlertDialog.Builder(this)
			.setTitle(R.string.command_set_arg_title)
			.setView(contentView)
			.show();
			dialog.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					getArgValue(command, args, commandArgs);
				}
			});
		}
	}

	private View getContentView(ArgumentSpecification arg, List<CommandArgument<?>> list) {
		ArgumentRange<?> range = arg.range;
		Type type = arg.mType;
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		if(range instanceof NumericRange<?>) {
			switch(type) {
			case INTEGER:
				return integerRangeContentView(((NumericRange<Integer>)range).getLowerBound(), ((NumericRange<Integer>)range).getUpperBound(), 
						arg, inflater, list);
			case REAL:
				return realRangeContentView(((NumericRange<Double>)range).getLowerBound(), ((NumericRange<Double>)range).getUpperBound(), 
						arg, inflater, list);
			}

		} else if(range instanceof EnumRange) {
			switch(type) {
			case STRING:
				return enumContentView(((EnumRange) range).enumerate(), arg, inflater, list);
			}
		} else {
			switch(type) {
			case STRING:
				return genericStringContentView(arg, inflater, list);
			case INTEGER:
				return genericIntegerContentView(arg, inflater, list);
			case REAL:
				return genericRealContentView(arg, inflater, list);
			}
		}
		switch(type) {
		case BOOLEAN:
			return booleanContentView(arg, inflater, list);
		case PERCENT:
			return percentContentView(arg, inflater, list);
		}
		return null;
	}

	private View percentContentView(final ArgumentSpecification arg, LayoutInflater inflater, final List<CommandArgument<?>> list) {
		View view = inflater.inflate(R.layout.percent_arg, null);
		final Button okButton = (Button)view.findViewById(R.id.command_set_arg_ok);
		okButton.setEnabled(false);
		final EditText field = (EditText)view.findViewById(R.id.command_set_arg_field);
		field.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s.length() > 0) {
					double value = Double.parseDouble(s.toString());
					if(value > 0 && value <= 100) {
						okButton.setEnabled(true);
					}
				}
			}
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			public void afterTextChanged(Editable s) {}
		});
		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				list.add(CommandArgument.newCommandArgument(arg.mName, arg.mType, field.getText()));
				dialog.dismiss();
			}
		});
		return view;
	}

	private View booleanContentView(final ArgumentSpecification arg, LayoutInflater inflater, final List<CommandArgument<?>> list) {
		View view = inflater.inflate(R.layout.boolean_arg, null);
		Button okButton = (Button)view.findViewById(R.id.command_set_arg_ok);
		final ToggleButton toggle = (ToggleButton)view.findViewById(R.id.command_set_arg_toggle);
		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				list.add(CommandArgument.newCommandArgument(arg.mName, arg.mType, toggle.isActivated()));
				dialog.dismiss();
			}
		});
		return view;
	}

	private View genericRealContentView(final ArgumentSpecification arg, LayoutInflater inflater, final List<CommandArgument<?>> list) {
		View view = inflater.inflate(R.layout.real_arg, null);
		final Button okButton = (Button)view.findViewById(R.id.command_set_arg_ok);
		okButton.setEnabled(false);
		final EditText field = (EditText)view.findViewById(R.id.command_set_arg_field);
		field.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s.length() > 0) {
					okButton.setEnabled(true);
				}
			}
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			public void afterTextChanged(Editable s) {}
		});
		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				list.add(CommandArgument.newCommandArgument(arg.mName, arg.mType, field.getText()));
				dialog.dismiss();
			}
		});
		return view;
	}

	private View genericIntegerContentView(final ArgumentSpecification arg, LayoutInflater inflater, final List<CommandArgument<?>> list) {
		View view = inflater.inflate(R.layout.int_arg, null);
		final Button okButton = (Button)view.findViewById(R.id.command_set_arg_ok);
		okButton.setEnabled(false);
		final EditText field = (EditText)view.findViewById(R.id.command_set_arg_field);
		field.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s.length() > 0) {
					okButton.setEnabled(true);
				}
			}
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			public void afterTextChanged(Editable s) {}
		});
		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				list.add(CommandArgument.newCommandArgument(arg.mName, arg.mType, field.getText()));
				dialog.dismiss();
			}
		});
		return view;
	}

	private View enumContentView(List<String> enumeration, final ArgumentSpecification arg, LayoutInflater inflater, final List<CommandArgument<?>> list) {
		View view = inflater.inflate(R.layout.enum_arg, null);
		Button okButton = (Button)view.findViewById(R.id.command_set_arg_ok);
		final Spinner spinner = (Spinner)view.findViewById(R.id.command_set_arg_spinner);
		spinner.setAdapter(new ArrayAdapter<String>(this, R.layout.enum_arg_spinner, R.id.enum_field, enumeration));
		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				list.add(CommandArgument.newCommandArgument(arg.mName, arg.mType, spinner.getSelectedItem()));
				dialog.dismiss();
			}
		});
		return view;
	}

	private View genericStringContentView(final ArgumentSpecification arg, LayoutInflater inflater, final List<CommandArgument<?>> list) {
		View view = inflater.inflate(R.layout.string_arg, null);
		final Button okButton = (Button)view.findViewById(R.id.command_set_arg_ok);
		okButton.setEnabled(false);
		final EditText field = (EditText)view.findViewById(R.id.command_set_arg_field);
		field.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s.length() > 0) {
					okButton.setEnabled(true);
				}
			}
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			public void afterTextChanged(Editable s) {}
		});
		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				list.add(CommandArgument.newCommandArgument(arg.mName, arg.mType, field.getText()));
				dialog.dismiss();
			}
		});
		return view;
	}

	private View realRangeContentView(final double lower, final double upper, final ArgumentSpecification arg, LayoutInflater inflater,
			final List<CommandArgument<?>> list) {
		View view = inflater.inflate(R.layout.real_range_arg, null);
		final Button okButton = (Button)view.findViewById(R.id.command_set_arg_ok);
		okButton.setEnabled(false);
		final EditText field = (EditText)view.findViewById(R.id.command_set_arg_field);
		field.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s.length() > 0) {
					double value = Double.parseDouble(s.toString());
					if(value >= lower && value <= upper) {
						okButton.setEnabled(true);
					}
				}
			}
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			public void afterTextChanged(Editable s) {}
		});
		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				list.add(CommandArgument.newCommandArgument(arg.mName, arg.mType, field.getText()));
				dialog.dismiss();
			}
		});
		TextView rangeView = (TextView)view.findViewById(R.id.command_set_arg_range);
		rangeView.setText(getResources().getString(R.string.command_set_arg_real_range, lower, upper));
		return view;
	}

	private View integerRangeContentView(final int lower, final int upper, final ArgumentSpecification arg, LayoutInflater inflater, 
			final List<CommandArgument<?>> list) {
		View view = inflater.inflate(R.layout.int_range_arg, null);
		final Button okButton = (Button)view.findViewById(R.id.command_set_arg_ok);
		okButton.setEnabled(false);
		final EditText field = (EditText)view.findViewById(R.id.command_set_arg_field);
		field.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s.length() > 0) {
					int value = Integer.parseInt(s.toString());
					if(value >= lower && value <= upper) {
						okButton.setEnabled(true);
					}
				}
			}
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			public void afterTextChanged(Editable s) {}
		});
		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				list.add(CommandArgument.newCommandArgument(arg.mName, arg.mType, field.getText()));
				dialog.dismiss();
			}
		});
		TextView rangeView = (TextView)view.findViewById(R.id.command_set_arg_range);
		rangeView.setText(getResources().getString(R.string.command_set_arg_int_range, lower, upper));
		return view;
	}

}
