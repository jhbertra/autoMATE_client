package com.automate.client.messaging;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
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
	 * The socket from which to download the message.
	 */
	private Socket socket;

	/**
	 * The binder object.
	 */
	private IBinder mBinder;
	
	private Api api;

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

	/* (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		api = new Api();
		this.mBinder = new PacketReceiveBinder();
	}

	/**
	 * Starts the download from the given socket.
	 * @param socket the socket from which to download the message.
	 */
	public void startDownload(Socket socket) {
		this.socket = socket;
		synchronized (lock) {
			lock.notify();
		}
	}
	
	@Override
	public void onHandleIntent(Intent intent) {
		int result = RESULT_UNKOWN_ERROR;
		Throwable throwable = null;
		String message = null;
		try {
			synchronized (lock) {
				if(socket == null) {
					lock.wait();
					if(socket == null) {
						Log.w(getClass().getName(), "PacketReceiveTask did not receive a socket.");
						result = RESULT_NO_SOCKET_PROVIDED;
						return;
					}
					StringBuilder sb = new StringBuilder();
					try {
						InputStreamReader reader = new InputStreamReader(socket.getInputStream());
						int next = -1;
						while((next = reader.read()) != -1) {
							sb.append((char)next);
						}
						if(sb.length() == 0) {
							result = RESULT_EMPTY_MESSAGE;
							Log.w(getClass().getName(), "Message received was empty.");
						} else {
							message = sb.toString();
							result = Activity.RESULT_OK;
							Log.v(getClass().getName(), "Downloaded messsage: " + message);
						}
					} catch (IOException e) {
						throwable = e;
						result = RESULT_IO_EXCEPTION;
					}
				}
			}
		} catch(Throwable t) {
			throwable = t;
		} finally {
			if(socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					Log.w(getClass().getName(), "Could not close socket.", e);
				}
			}
			publishResults(result, message, throwable);
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
	}
	
	public class Api {
		
		public void startDownload(Socket socket) {
			PacketReceiveService.this.startDownload(socket);
		}
		
	}
	
	public class PacketReceiveBinder extends Binder {
		
		public Api getApi() {
			return PacketReceiveService.this.api;
		}
		
	}
	
}
