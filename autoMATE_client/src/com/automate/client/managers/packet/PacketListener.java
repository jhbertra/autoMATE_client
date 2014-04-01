package com.automate.client.managers.packet;

public interface PacketListener extends PacketReceivedListener, PacketSentListener {

	public void onPacketReceived(String packet);

	public void onEmptyPacketReceived();

	public void onReceiveIoException();

	public void onNoSocketProvided();

	public void onReceiveError();
	
	
	
}
