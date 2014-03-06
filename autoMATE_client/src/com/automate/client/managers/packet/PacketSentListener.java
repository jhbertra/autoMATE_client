package com.automate.client.managers.packet;

import com.automate.client.managers.IListener;

public interface PacketSentListener extends IListener {

	public void onPacketSent(int packetId);

	public void onSendIoException(int packetId);

	public void onSendNoServerAddress(int packetId);

	public void onSendNoServerPort(int packetId);

	public void onSendError(int packetId);

}
