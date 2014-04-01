package com.automate.client.managers.packet;

import com.automate.client.managers.IManager;
import com.automate.client.managers.security.SecurityListener;

public interface IPacketManager extends PacketListener, IManager<PacketListener>, SecurityListener {
	
	public void sendPacket(String packet);
	
	public void sendPacket(String packet, PacketSentListener listener);

	public void receivePacket(String packet);
	
	public void onDisconnected();
	
}
