package com.automate.client.managers.packet;

import com.automate.client.packet.services.PacketDeliveryService;
import com.automate.client.packet.services.PacketReceiveService;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PacketSendReceiver extends BroadcastReceiver {

	private IPacketManager mPacketManager;
	
	public PacketSendReceiver(IPacketManager packetManager) {
		this.mPacketManager = packetManager;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		int result = intent.getIntExtra(PacketReceiveService.RESULT, Activity.RESULT_CANCELED);
		int packetId = intent.getIntExtra(PacketDeliveryService.PACKET_ID, -1);
		switch (result) {
		case Activity.RESULT_OK:
			mPacketManager.onPacketSent(packetId);
			break;
		case PacketDeliveryService.RESULT_IO_EXCEPTION:
			mPacketManager.onSendIoException(packetId);
			break;
		case PacketDeliveryService.RESULT_NO_SERVER_ADDRESS:
			mPacketManager.onSendNoServerAddress(packetId);
			break;
		case PacketDeliveryService.RESULT_NO_SERVER_PORT:
			mPacketManager.onSendNoServerPort(packetId);
			break;
		case PacketDeliveryService.RESULT_UNKOWN_ERROR:
			mPacketManager.onSendError(packetId);
			break;
		default:
			break;
		}
	}

}
