package com.automate.client.messaging;

import com.automate.client.IListener;

public interface PacketSentListener extends IListener {

	public void onPacketSent(int packetId);

	public void onSendIoException(int packetId);

	public void onSendNoServerAddress(int packetId);

	public void onSendNoServerPort(int packetId);

	public void onSendError(int packetId);

}
