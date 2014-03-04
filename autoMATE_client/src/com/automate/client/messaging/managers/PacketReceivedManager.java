package com.automate.client.messaging.managers;

import java.util.HashMap;

import android.util.Log;

import com.automate.client.IManager;
import com.automate.client.ListenerBinding;
import com.automate.client.messaging.PacketReceivedListener;
import com.automate.client.messaging.handlers.IMessageHandler;
import com.automate.protocol.IncomingMessageParser;
import com.automate.protocol.Message;
import com.automate.protocol.Message.MessageType;
import com.automate.protocol.server.ServerProtocolParameters;

public class PacketReceivedManager extends ListenerBinding<PacketReceivedListener> implements PacketReceivedListener, IManager {

	private IncomingMessageParser<ServerProtocolParameters> mIncomingMessageParser;
	private HashMap<MessageType, IMessageHandler<? extends Message<ServerProtocolParameters>, ?>> mHandlers;
	
	public PacketReceivedManager(
			IncomingMessageParser<ServerProtocolParameters> mIncomingMessageParser,
			HashMap<MessageType, IMessageHandler<? extends Message<ServerProtocolParameters>, ?>> handlers) {
		this.mIncomingMessageParser = mIncomingMessageParser;
		this.mHandlers = handlers;
	}

	@Override
	public void onPacketReceived(String packet) {
		try {
			Message<ServerProtocolParameters> message = mIncomingMessageParser.parse(packet);
			IMessageHandler handler = mHandlers.get(message.getMessageType());
			Object args = null;
			switch (message.getMessageType()) {
			default:
				break;
			}
			handler.handleMessage(1, 0, message, args);
		} catch (Throwable t) {
			Log.e(getClass().getName(), "Error handling received packet.", t);
		}
		for(PacketReceivedListener listener : listeners) {
			listener.onPacketReceived(packet);
		}
	}

	@Override
	public void onEmptyPacketReceived() {
		for(PacketReceivedListener listener : listeners) {
			listener.onEmptyPacketReceived();
		}
	}

	@Override
	public void onReceiveIoException() {

		for(PacketReceivedListener listener : listeners) {
			listener.onReceiveIoException();
		}
	}

	@Override
	public void onNoSocketProvided() {
		for(PacketReceivedListener listener : listeners) {
			listener.onNoSocketProvided();
		}
	}

	@Override
	public void onReceiveError() {
		for(PacketReceivedListener listener : listeners) {
			listener.onReceiveError();
		}
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

}
