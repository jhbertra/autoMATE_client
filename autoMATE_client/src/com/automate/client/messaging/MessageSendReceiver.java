package com.automate.client.messaging;

import com.automate.client.messaging.services.PacketDeliveryService;
import com.automate.client.messaging.services.PacketReceiveService;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MessageSendReceiver extends BroadcastReceiver {

	private PacketSentListener mListener;
	
	public MessageSendReceiver(PacketSentListener listener) {
		this.mListener = listener;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		int result = intent.getIntExtra(PacketReceiveService.RESULT, Activity.RESULT_CANCELED);
		int packetId = intent.getIntExtra(PacketDeliveryService.PACKET_ID, -1);
		switch (result) {
		case Activity.RESULT_OK:
			mListener.onPacketSent(packetId);
			break;
		case PacketDeliveryService.RESULT_IO_EXCEPTION:
			mListener.onSendIoException(packetId);
			break;
		case PacketDeliveryService.RESULT_NO_SERVER_ADDRESS:
			mListener.onSendNoServerAddress(packetId);
			break;
		case PacketDeliveryService.RESULT_NO_SERVER_PORT:
			mListener.onSendNoServerPort(packetId);
			break;
		case PacketDeliveryService.RESULT_UNKOWN_ERROR:
			mListener.onSendError(packetId);
			break;
		default:
			break;
		}
	}

}
