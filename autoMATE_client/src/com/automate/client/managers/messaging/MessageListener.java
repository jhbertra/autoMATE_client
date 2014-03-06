package com.automate.client.managers.messaging;

import com.automate.client.managers.IListener;
import com.automate.protocol.Message;
import com.automate.protocol.client.ClientProtocolParameters;
import com.automate.protocol.server.ServerProtocolParameters;

public interface MessageListener extends IListener {

	public void onMessageReceived(Message<ServerProtocolParameters> message);
	
	public void onMessageSent(Message<ClientProtocolParameters> message);
	
}
