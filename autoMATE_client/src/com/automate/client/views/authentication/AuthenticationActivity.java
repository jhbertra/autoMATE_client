package com.automate.client.views.authentication;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.automate.client.R;
import com.automate.client.views.nodelist.NodeListActivity;
import com.automate.client.views.AbstractAuthenticationService;
import com.automate.client.views.AbstractAuthenticationService.AbstractAuthenticationServiceBinder;
import com.automate.client.views.registration.RegistrationActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AuthenticationActivity extends Activity implements ServiceConnection, Callback {
	
	private TextWatcher usernameWatcher;
	private TextWatcher passwordWatcher;
	
	private boolean usernameValid;
	private boolean passwordValid;
	
	private Pattern usernamePattern;
	private Pattern passwordPattern1;
	private Pattern passwordPattern2;
	
	private Button signInButton;
	private TextView usernameView;
	private TextView passwordView;
	private ProgressDialog progressDialog;
	
	
	private AuthenticationService mService;
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auth);
		
		usernameWatcher = new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void afterTextChanged(Editable s) {}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				Matcher matcher = usernamePattern.matcher(s);
				setUsernameValid(matcher.matches());
			}
		};	
		passwordWatcher = new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void afterTextChanged(Editable s) {}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				Matcher matcher1 = passwordPattern1.matcher(s);
				Matcher matcher2 = passwordPattern2.matcher(s);
				setPasswordValid(s.length() >= 6 && matcher1.find() && matcher2.find());
			}
		};
		
		usernameValid = false;
		passwordValid = false;
		
		usernamePattern = Pattern.compile("[a-zA-Z0-9_.-]{6,}");
		passwordPattern1 = Pattern.compile("[a-zA-Z]");
		passwordPattern2 = Pattern.compile("[0-9]");
		
		signInButton = (Button) findViewById(R.id.auth_sign_in);
		signInButton.setEnabled(false);
		usernameView = (TextView) findViewById(R.id.auth_username);
		passwordView = (TextView) findViewById(R.id.auth_password);
		
		usernameView.addTextChangedListener(usernameWatcher);
		passwordView.addTextChangedListener(passwordWatcher);
		
		Intent serviceIntent = new Intent(this, AuthenticationService.class);
		Messenger messenger = new Messenger(new Handler(this));
		serviceIntent.putExtra(AbstractAuthenticationService.MESSENGER, messenger);
		startService(serviceIntent);
		bindService(serviceIntent, this, 0);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		usernamePattern = null;
		passwordPattern1 = null;
		passwordPattern2 = null;
		passwordWatcher = null;
		usernameWatcher = null;
	}

	protected void setPasswordValid(boolean passwordValid) {
		this.passwordValid = passwordValid;
		signInButton.setEnabled(passwordValid && usernameValid);
	}

	protected void setUsernameValid(boolean usernameValid) {
		this.usernameValid = usernameValid;
		signInButton.setEnabled(passwordValid && usernameValid);
	}
	
	public void onSignInPressed(View v) {
		if(this.mService.signIn(usernameView.getText().toString(), passwordView.getText().toString())) {
			this.progressDialog = ProgressDialog.show(this, null, getResources().getString(R.string.auth_signing_in));
		}
	}
	
	public void onCreateNewAccountPressed(View v) {
		startActivityForResult(new Intent(this, RegistrationActivity.class), RegistrationActivity.REGISTRATION_COMPLETE);
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RegistrationActivity.REGISTRATION_COMPLETE) {
			finish();
		}
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		this.mService = (AuthenticationService) ((AbstractAuthenticationServiceBinder) service).getService();
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		finish();
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch(msg.what) {
		case AuthenticationService.AUTHENTICATION_SUCCESSFUL:
			this.progressDialog.dismiss();
			Intent nextViewIntent = new Intent(this, NodeListActivity.class);
			startActivity(nextViewIntent);
			finish();
			break;
		case AuthenticationService.AUTHENTICATION_FAILED:
			this.progressDialog.dismiss();
			String message = getResources().getString(R.string.auth_failed) + "\n" + msg.obj;
			new AlertDialog.Builder(this)
			.setTitle(R.string.auth_title_failed)
			.setMessage(message)
			.setNeutralButton(R.string.button_ok, new OnClickListener() {
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
	
}
