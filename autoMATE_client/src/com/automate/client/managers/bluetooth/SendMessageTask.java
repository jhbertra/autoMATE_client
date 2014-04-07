package com.automate.client.managers.bluetooth;

import java.io.BufferedReader; 
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import com.automate.client.managers.bluetooth.IBluetoothManager.MessageCallback;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class SendMessageTask implements Runnable {

	private BluetoothSocket mSocket;
	private String mMessage;
	private MessageCallback mCallback;

	public SendMessageTask(BluetoothSocket socket, String message, MessageCallback callback) {
		this.mSocket = socket;
		this.mMessage = message;
		this.mCallback = callback;
	}

	@Override
	public void run() {
		try {
			sendMessage(mSocket.getOutputStream());
			receiveResponse(mSocket.getInputStream());
		} catch (IOException e) {
			Log.e(getClass().getName(), "Failed to to open streams from bluetooth device.", e);
			return;
		}
	}
	
	private void sendMessage(OutputStream outputStream) {
		PrintWriter writer = new PrintWriter(outputStream);
		writer.println(mMessage);
		writer.flush();
	}

	private void receiveResponse(InputStream inputStream) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String response = null;
		try {
			response = reader.readLine();
		} catch (IOException e) {
			Log.e(getClass().getName(), "Failed to receive response from bluetooth device.", e);
		}
		mCallback.onResponse(response);
	}

}
