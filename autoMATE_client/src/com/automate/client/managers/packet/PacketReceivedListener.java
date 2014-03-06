package com.automate.client.managers.packet;

import com.automate.client.managers.IListener;

public interface PacketReceivedListener extends IListener {
	
	public void onPacketReceived(String packet);

	public void onEmptyPacketReceived();

	public void onReceiveIoException();

	public void onNoSocketProvided();

	public void onReceiveError();
}
