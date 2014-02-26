package com.automate.client.messaging;

import java.net.Socket;

import android.os.AsyncTask;

public class PacketReceiveTask extends AsyncTask<Socket, Void, String> {
	
	@Override
	protected String doInBackground(Socket... params) {
		return null;
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(String result) {
		
	}

}
