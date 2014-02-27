package com.automate.client.messaging;

import com.automate.client.messaging.MessagingService.Api;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MessageReceiveReceiver extends BroadcastReceiver {

	private MessagingService.Api messagingServiceApi;
	
	public MessageReceiveReceiver(Api messagingServiceApi) {
		this.messagingServiceApi = messagingServiceApi;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		int result = intent.getIntExtra(PacketReceiveService.RESULT, Activity.RESULT_CANCELED);
		switch (result) {
		case Activity.RESULT_OK:
			String packet = intent.getStringExtra(PacketReceiveService.MESSAGE);
			if(packet != null) {
				messagingServiceApi.onPacketReceived(packet);
			}
			break;
		case PacketReceiveService.RESULT_EMPTY_MESSAGE:
			messagingServiceApi.onEmptyPacketReceived();
			break;
		case PacketReceiveService.RESULT_IO_EXCEPTION:
			messagingServiceApi.onReceiveIoException();
			break;
		case PacketReceiveService.RESULT_NO_SOCKET_PROVIDED:
			messagingServiceApi.onNoSocketProvided();
			break;
		case PacketReceiveService.RESULT_UNKOWN_ERROR:
			messagingServiceApi.onReceiveError();
			break;
		default:
			break;
		}
	}

}
