package com.automate.client.managers.packet;

import com.automate.client.packet.services.PacketReceiveService;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PacketReceiveReceiver extends BroadcastReceiver {

	private IPacketManager mPacketManager;
	
	public PacketReceiveReceiver(IPacketManager packetManager) {
		this.mPacketManager = packetManager;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		int result = intent.getIntExtra(PacketReceiveService.RESULT, Activity.RESULT_CANCELED);
		switch (result) {
		case Activity.RESULT_OK:
			String packet = intent.getStringExtra(PacketReceiveService.MESSAGE);
			if(packet != null) {
				mPacketManager.receivePacket(packet);
			}
			break;
		case PacketReceiveService.RESULT_EMPTY_MESSAGE:
			mPacketManager.onEmptyPacketReceived();
			break;
		case PacketReceiveService.RESULT_IO_EXCEPTION:
			mPacketManager.onReceiveIoException();
			break;
		case PacketReceiveService.RESULT_NO_SOCKET_PROVIDED:
			mPacketManager.onNoSocketProvided();
			break;
		case PacketReceiveService.RESULT_UNKOWN_ERROR:
			mPacketManager.onReceiveError();
			break;
		default:
			break;
		}
	}

}
