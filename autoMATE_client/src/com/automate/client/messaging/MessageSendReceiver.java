package com.automate.client.messaging;

import com.automate.client.messaging.MessagingService.Api;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MessageSendReceiver extends BroadcastReceiver {

	private MessagingService.Api messagingServiceApi;
	
	public MessageSendReceiver(Api messagingServiceApi) {
		this.messagingServiceApi = messagingServiceApi;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		int result = intent.getIntExtra(PacketReceiveService.RESULT, Activity.RESULT_CANCELED);
		int packetId = intent.getIntExtra(PacketDeliveryService.PACKET_ID, -1);
		switch (result) {
		case Activity.RESULT_OK:
			messagingServiceApi.onPacketSent(packetId);
			break;
		case PacketDeliveryService.RESULT_IO_EXCEPTION:
			messagingServiceApi.onSendIoException(packetId);
			break;
		case PacketDeliveryService.RESULT_NO_SERVER_ADDRESS:
			messagingServiceApi.onSendNoServerAddress(packetId);
			break;
		case PacketDeliveryService.RESULT_NO_SERVER_PORT:
			messagingServiceApi.onSendNoServerPort(packetId);
			break;
		case PacketDeliveryService.RESULT_UNKOWN_ERROR:
			messagingServiceApi.onSendError(packetId);
			break;
		default:
			break;
		}
	}

}
