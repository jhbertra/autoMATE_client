package com.automate.client.messaging.managers;

import com.automate.client.IManager;
import com.automate.client.messaging.PacketSentListener;
import com.automate.protocol.Message;
import com.automate.protocol.client.ClientProtocolParameters;

public interface IMessageManager extends IManager {

	public void sendMessage(Message<ClientProtocolParameters> message, PacketSentListener listener);
	
	public void sendMessage(Message<ClientProtocolParameters> message);
	
	public void setSessionKey(String sessionKey);
	
	public String getSessionKey();
	
	public ClientProtocolParameters getProtocolParameters();
	
}
