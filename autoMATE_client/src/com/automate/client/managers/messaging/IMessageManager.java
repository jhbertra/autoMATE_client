package com.automate.client.managers.messaging;

import com.automate.client.managers.IListenerBinder;
import com.automate.client.managers.IManager;
import com.automate.client.managers.connectivity.ConnectionListener;
import com.automate.client.managers.packet.PacketListener;
import com.automate.client.managers.packet.PacketSentListener;
import com.automate.protocol.Message;
import com.automate.protocol.client.ClientProtocolParameters;

public interface IMessageManager extends IManager<MessageListener>, IListenerBinder<MessageListener>, MessageListener, PacketListener, 
	ConnectionListener {

	public void sendMessage(Message<ClientProtocolParameters> message);

	public void sendMessage(Message<ClientProtocolParameters> message, PacketSentListener listener);
	
	public ClientProtocolParameters getProtocolParameters();
	
	public String getSessionKey();
	
	public void setSessionKey(String sessionKey);
	
}
