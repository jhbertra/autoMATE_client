package com.automate.client.packet.services;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import com.automate.client.managers.packet.IncomingPacketListenerThread;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * Sends messages to the server as TCP packets.
 * @author jamie.bertram
 *
 */
public class PacketDeliveryService extends IntentService {

	/**
	 * Key used for storing the server address in the creating Intent.
	 */
	public static final String SERVER_ADDRESS = "server address";

	/**
	 * Key used for storing the server port in the creating Intent.
	 */
	public static final String SERVER_PORT = "server port";

	/**
	 * Key used for storing the xml data in the creating Intent.
	 */
	public static final String DATA = "data";

	/**
	 * Key for retrieving the result from the resulting intent.
	 */
	public static final String RESULT = "result";

	/**
	 * Key for retrieving any caught exceptions (if result is RESULT_IO_EXCEPTION or RESULT_UNKNOWN_ERROR).
	 */
	public static final String EXCEPTION = "exception";

	/**
	 * Result code indicating that the server address was not provided.
	 */
	public static final int RESULT_NO_SERVER_ADDRESS = Activity.RESULT_FIRST_USER;

	/**
	 * Result code indicating that the server port was not provided.
	 */
	public static final int RESULT_NO_SERVER_PORT = Activity.RESULT_FIRST_USER + 1;

	/**
	 * Result code indicating that sending the packet threw an {@link IOException}.
	 */
	public static final int RESULT_IO_EXCEPTION = Activity.RESULT_FIRST_USER + 2;

	/**
	 * Result code indicating that execution unexpectedly ended.
	 */
	public static final int RESULT_UNKOWN_ERROR = Activity.RESULT_FIRST_USER + 3;

	public static final String PACKET_ID = "Packet Id";

	private IBinder mBinder;

	private IncomingPacketListenerThread mListenerThread;

	public PacketDeliveryService() {
		super("PacketDeliveryService");
	}

	/* (non-Javadoc)
	 * @see android.app.IntentService#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	/* (non-Javadoc)
	 * @see android.app.IntentService#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		mBinder = new PacketDeliveryServiceBinder();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(getClass().getName(), "Starting PacketDeliveryService.");
		int result = RESULT_UNKOWN_ERROR;
		Throwable throwable = null;
		int packetId = intent.getIntExtra(PACKET_ID, -1);
		try {
			synchronized (this) {
				if(mListenerThread == null) {
					wait();
				}
			}
			String serverAddress = intent.getStringExtra(SERVER_ADDRESS);
			int serverPort = intent.getIntExtra(SERVER_PORT, 6300);
			String xmlData = intent.getStringExtra(DATA);

			if(serverAddress == null) {
				Log.w(getClass().getName(), "No server address provided. Terminating service.");
				result = RESULT_NO_SERVER_ADDRESS;
				return;
			} else if(xmlData == null) {
				Log.w(getClass().getName(), "No message data provided. Terminating service.");
				result = RESULT_NO_SERVER_PORT;
				return;
			} else if(packetId < 0) {
				Log.w(getClass().getName(), "No packet id provided. Terminating service.");
				return;
			}

			Socket outputSocket = null;
			PrintWriter writer = null;
			try {
				outputSocket = new Socket(serverAddress, serverPort);
				writer = new PrintWriter(outputSocket.getOutputStream());
				writer.println(xmlData);
				writer.println('\0');
				writer.flush();
				Log.v(getClass().getName(), "Sent message: " + xmlData);
				result = Activity.RESULT_OK;
				mListenerThread.queueSocket(outputSocket);
			} catch (IOException e) {
				Log.e(getClass().getName(), "Error sending packet.", e);
				result = RESULT_IO_EXCEPTION;
				throwable = e;
				if(writer != null) {
					writer.close();
				} else if(outputSocket != null) {
					try {
						outputSocket.close();
					} catch (IOException e2) {
						Log.w(getClass().getName(), "Unable to close socket.", e);
					}
				}
			}
		} catch (Throwable t) {
			result = RESULT_UNKOWN_ERROR;
			throwable = t;
		} finally {
			publishResults(result, throwable, packetId);
		}
	}

	private void publishResults(int result, Throwable throwable, int packetId) {
		Intent intent = new Intent(getClass().getName());
		intent.putExtra(RESULT, result);
		if(throwable != null) {
			intent.putExtra(EXCEPTION, throwable);
		}
		intent.putExtra(PACKET_ID, packetId);
		sendBroadcast(intent);
	}

	public class PacketDeliveryServiceBinder extends Binder {
		
		public void setListenThread(IncomingPacketListenerThread thread) {
			synchronized (PacketDeliveryService.this) {
				mListenerThread = thread;
				PacketDeliveryService.this.notify();
			}
		}
		
	}
	
}
