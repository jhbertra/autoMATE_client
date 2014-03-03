package com.automate.client.messaging;

import java.util.Hashtable;

import com.automate.client.ListenerBinding;

public class PacketSentManager extends ListenerBinding<PacketSentListener> implements PacketSentListener {

	private Hashtable<Integer, PacketSentListener> deliveryListeners = new Hashtable<Integer, PacketSentListener>();
	
	public void addDeliveryListener(int packetId, PacketSentListener listener) {
		if(listener != null && packetId > 0) {
			deliveryListeners.put(packetId, listener);
		}
	}
	
	@Override
	public void onPacketSent(int packetId) {
		PacketSentListener listener = deliveryListeners.get(packetId);
		if(listener != null) {
			listener.onPacketSent(packetId);
		}
		deliveryListeners.remove(packetId);
		for(PacketSentListener listener2 : listeners) {
			listener2.onPacketSent(packetId);
		}
	}

	@Override
	public void onSendIoException(int packetId) {
		PacketSentListener listener = deliveryListeners.get(packetId);
		if(listener != null) {
			listener.onSendIoException(packetId);
		}
		deliveryListeners.remove(packetId);
		for(PacketSentListener listener2 : listeners) {
			listener2.onSendIoException(packetId);
		}
	}

	@Override
	public void onSendNoServerAddress(int packetId) {
		PacketSentListener listener = deliveryListeners.get(packetId);
		if(listener != null) {
			listener.onSendNoServerAddress(packetId);
		}
		deliveryListeners.remove(packetId);
		for(PacketSentListener listener2 : listeners) {
			listener2.onSendNoServerAddress(packetId);
		}
	}

	@Override
	public void onSendNoServerPort(int packetId) {
		PacketSentListener listener = deliveryListeners.get(packetId);
		if(listener != null) {
			listener.onSendNoServerPort(packetId);
		}
		deliveryListeners.remove(packetId);
		for(PacketSentListener listener2 : listeners) {
			listener2.onSendNoServerPort(packetId);
		}
	}

	@Override
	public void onSendError(int packetId) {
		PacketSentListener listener = deliveryListeners.get(packetId);
		if(listener != null) {
			listener.onSendError(packetId);
		}
		deliveryListeners.remove(packetId);
		for(PacketSentListener listener2 : listeners) {
			listener2.onSendError(packetId);
		}
	}
	
}
