package com.automate.client.packet.services;

import java.io.IOException;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class PacketReceiveService extends IntentService {

	/**
	 * Key for retrieving the result from the resulting intent.
	 */
	public final static String RESULT = "result";

	/**
	 * Key for retrieving any caught exceptions (if result is RESULT_IO_EXCEPTION or RESULT_UNKNOWN_ERROR).
	 */
	public final static String EXCEPTION = "exception";

	/**
	 * Key for retrieving the downloaded message from the resulting intent.
	 */
	public static final String MESSAGE = null;

	/**
	 * Result code indicating that there was no socket provided.
	 */
	public static final int RESULT_NO_SOCKET_PROVIDED = Activity.RESULT_FIRST_USER;

	/**
	 * Result code indicating that sending the packet threw an {@link IOException}.
	 */
	public static final int RESULT_IO_EXCEPTION = Activity.RESULT_FIRST_USER + 1;

	/**
	 * Result code indicating that execution unexpectedly ended.
	 */
	public static final int RESULT_UNKOWN_ERROR = Activity.RESULT_FIRST_USER + 2;

	/**
	 * Result code indicating that the message that was downloaded was blank.
	 */
	public static final int RESULT_EMPTY_MESSAGE = Activity.RESULT_FIRST_USER + 3;

	/**
	 * For synchronization.
	 */
	private final Object lock = new Object();

	/**
	 * The binder object.
	 */
	private IBinder mBinder;

	public PacketReceiveService() {
		super("PacketReceiveService");
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onHandleIntent(Intent intent) {
		synchronized (lock) {
			int result = RESULT_UNKOWN_ERROR;
			String message = null;
			Throwable throwable = null;
			try {
				Log.d(getClass().getName(), "Starting PacketReceiveService.");
				message = intent.getStringExtra(MESSAGE);
				if(message == null) {
					result = RESULT_EMPTY_MESSAGE;
					Log.w(getClass().getName(), "Message received was empty.");
				} else {
					result = Activity.RESULT_OK;
					Log.v(getClass().getName(), "Downloaded messsage: " + message);
				}
			} catch (Throwable t) {
				throwable = t;
			} finally {
				publishResults(result, message, throwable);
			}
		}
	}

	private void publishResults(int result, String message, Throwable throwable) {
		Intent intent = new Intent(getClass().getName());
		intent.putExtra(RESULT, result);
		if(throwable != null) {
			intent.putExtra(EXCEPTION, throwable);
		}
		if(message != null) {
			intent.putExtra(MESSAGE, message);
		}
		sendBroadcast(intent);
		stopSelf();
	}

}
