package com.automate.client.messaging;

public interface PacketSentListener {

	public void onPacketSent(int packetId);

	public void onSendIoException(int packetId);

	public void onSendNoServerAddress(int packetId);

	public void onSendNoServerPort(int packetId);

	public void onSendError(int packetId);

}
