package com.automate.client.messaging.managers;

import com.automate.client.IManager;
import com.automate.client.messaging.PacketSentListener;

public interface IPacketSentManager extends PacketSentListener, IManager {

	public abstract void addDeliveryListener(int packetId,
			PacketSentListener listener);

}