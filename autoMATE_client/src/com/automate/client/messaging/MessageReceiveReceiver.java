package com.automate.client.messaging;

import com.automate.client.messaging.services.PacketReceiveService;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MessageReceiveReceiver extends BroadcastReceiver {

	private PacketReceivedListener mListener;
	
	public MessageReceiveReceiver(PacketReceivedListener listener) {
		this.mListener = listener;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		int result = intent.getIntExtra(PacketReceiveService.RESULT, Activity.RESULT_CANCELED);
		switch (result) {
		case Activity.RESULT_OK:
			String packet = intent.getStringExtra(PacketReceiveService.MESSAGE);
			if(packet != null) {
				mListener.onPacketReceived(packet);
			}
			break;
		case PacketReceiveService.RESULT_EMPTY_MESSAGE:
			mListener.onEmptyPacketReceived();
			break;
		case PacketReceiveService.RESULT_IO_EXCEPTION:
			mListener.onReceiveIoException();
			break;
		case PacketReceiveService.RESULT_NO_SOCKET_PROVIDED:
			mListener.onNoSocketProvided();
			break;
		case PacketReceiveService.RESULT_UNKOWN_ERROR:
			mListener.onReceiveError();
			break;
		default:
			break;
		}
	}

}
