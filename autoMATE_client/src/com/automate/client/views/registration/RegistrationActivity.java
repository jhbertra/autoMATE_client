package com.automate.client.views.registration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.automate.client.R;
import com.automate.client.views.authentication.AbstractAuthenticationService;
import com.automate.client.views.authentication.AuthenticationService;
import com.automate.client.views.authentication.AbstractAuthenticationService.AbstractAuthenticationServiceBinder;
import com.automate.client.views.nodelist.NodeListActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Handler.Callback;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class RegistrationActivity extends Activity implements ServiceConnection, Callback {

	public static final int REGISTRATION_COMPLETE = RESULT_FIRST_USER;
	
	private Button registerButton;
	private TextView usernameField;
	private TextView passwordField;
	private TextView confirmPasswordField;
	private TextView nameField;
	private TextView emailField;
	
	private boolean usernameValid;
	private boolean passwordValid;
	private boolean passwordsMatch;
	private boolean nameValid;
	private boolean emailValid;
	
	private Pattern usernamePattern;
	private Pattern passwordPattern1;
	private Pattern passwordPattern2;
	private Pattern namePattern;
	private Pattern emailPattern;
	
	private TextWatcher usernameWatcher;
	private TextWatcher passwordWatcher;
	private TextWatcher confirmPasswordWatcher;
	private TextWatcher nameWatcher;
	private TextWatcher emailWatcher;
	private ProgressDialog progressDialog;
	
	private RegistrationService mService;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_registration);
		
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
				setPasswordsMatch(s.equals(confirmPasswordField.getText()));
			}
		};
		confirmPasswordWatcher = new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void afterTextChanged(Editable s) {}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				setPasswordsMatch(s.equals(passwordField.getText()));
			}
		};
		nameWatcher = new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void afterTextChanged(Editable s) {}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				Matcher matcher = namePattern.matcher(s);
				setNameValid(matcher.matches());
			}
		};
		emailWatcher = new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void afterTextChanged(Editable s) {}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				Matcher matcher = emailPattern.matcher(s);
				setEmailValid(matcher.matches());
			}
		};
		
		usernameValid = false;
		passwordValid = false;
		passwordsMatch = false;
		nameValid = false;
		emailValid = false;
		
		usernamePattern = Pattern.compile("[a-zA-Z0-9_.-]{6,}");
		passwordPattern1 = Pattern.compile("[a-zA-Z]");
		passwordPattern2 = Pattern.compile("[0-9]");
		namePattern = Pattern.compile("[A-Z][a-z]* [A-Z][a-z]*");
		emailPattern = Pattern.compile("[-0-9a-zA-Z.+_]+@[-0-9a-zA-Z.+_]+\\.[a-zA-Z]{2,4}");
		
		registerButton = (Button) findViewById(R.id.reg_btn_register);
		usernameField = (TextView) findViewById(R.id.reg_username);
		usernameField.addTextChangedListener(usernameWatcher);
		passwordField = (TextView) findViewById(R.id.reg_password);
		passwordField.addTextChangedListener(passwordWatcher);
		confirmPasswordField = (TextView) findViewById(R.id.reg_confirm_password);
		confirmPasswordField.addTextChangedListener(confirmPasswordWatcher);
		nameField = (TextView) findViewById(R.id.reg_name);
		nameField.addTextChangedListener(nameWatcher);
		emailField = (TextView) findViewById(R.id.reg_email);
		emailField.addTextChangedListener(emailWatcher);
	}
	
	@Override
	protected void onStart() {
		super.onStart();		
		Intent serviceIntent = new Intent(this, RegistrationService.class);
		Messenger messenger = new Messenger(new Handler(this));
		serviceIntent.putExtra(AbstractAuthenticationService.MESSENGER, messenger);
		startService(serviceIntent);
		bindService(serviceIntent, this, 0);
	}

	@Override
	protected void onStop() {
		super.onStop();
		unbindService(this);
		this.mService.stopSelf();
	}

	private void setEmailValid(boolean emailVaild) {
		this.emailValid = emailVaild;
		updateButtonEnabled();
	}

	private void setNameValid(boolean nameValid) {
		this.nameValid = nameValid;
		updateButtonEnabled();
	}

	private void setPasswordsMatch(boolean passwordsMatch) {
		this.passwordsMatch = passwordsMatch;
		updateButtonEnabled();
	}

	private void setPasswordValid(boolean passwordValid) {
		this.passwordValid = passwordValid;
		updateButtonEnabled();
	}

	private void setUsernameValid(boolean usernameValid) {
		this.usernameValid = usernameValid;
		updateButtonEnabled();
	}

	private void updateButtonEnabled() {
		this.registerButton.setEnabled(usernameValid && passwordValid && passwordsMatch && nameValid && emailValid);
	}
	
	public void onRegisterPressed(View v) {
		if(this.mService.register(usernameField.getText().toString(), passwordField.getText().toString(), 
				nameField.getText().toString(), emailField.getText().toString())) {
			this.progressDialog = ProgressDialog.show(this, null, getResources().getString(R.string.auth_signing_in));
		}
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		this.mService = (RegistrationService) ((AbstractAuthenticationServiceBinder) service).getService();
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
			setResult(REGISTRATION_COMPLETE);
			finish();
			break;
		case AuthenticationService.AUTHENTICATION_FAILED:
			this.progressDialog.dismiss();
			String message = getResources().getString(R.string.reg_failed) + "\n" + msg.obj;
			new AlertDialog.Builder(this)
			.setTitle(R.string.reg_title_failed)
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
