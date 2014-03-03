package com.automate.client;

import com.automate.client.views.authentication.AuthenticationActivity;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent nextViewIntent = null;
		SharedPreferences preferences = getSharedPreferences(getResources().getString(R.string.prefs_credentials), MODE_PRIVATE);
		String username = preferences.getString(getResources().getString(R.string.prefs_credentials_username), null);
		String password = preferences.getString(getResources().getString(R.string.prefs_credentials_password), null);
		if(username == null || password == null) {
			nextViewIntent = new Intent(MainActivity.this, AuthenticationActivity.class);
		}
		startActivity(nextViewIntent);
		finish();
	}

}
