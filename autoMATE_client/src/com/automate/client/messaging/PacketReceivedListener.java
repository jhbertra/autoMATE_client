package com.automate.client.messaging;

import com.automate.client.IListener;

public interface PacketReceivedListener extends IListener {
	
	public void onPacketReceived(String packet);

	public void onEmptyPacketReceived();

	public void onReceiveIoException();

	public void onNoSocketProvided();

	public void onReceiveError();
}
