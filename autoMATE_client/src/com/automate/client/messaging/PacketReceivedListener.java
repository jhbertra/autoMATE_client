package com.automate.client.messaging;

public interface PacketReceivedListener {
	
	public void onPacketReceived(String packet);

	public void onEmptyPacketReceived();

	public void onReceiveIoException();

	public void onNoSocketProvided();

	public void onReceiveError();
}
