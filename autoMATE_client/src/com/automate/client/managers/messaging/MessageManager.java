package com.automate.client.managers.messaging;

import android.util.Log; 

import com.automate.client.managers.IListener;
import com.automate.client.managers.ManagerBase;
import com.automate.client.managers.connectivity.IConnectionManager;
import com.automate.client.managers.packet.IPacketManager;
import com.automate.client.managers.packet.PacketSentListener;
import com.automate.protocol.IncomingMessageParser;
import com.automate.protocol.Message;
import com.automate.protocol.client.ClientProtocolParameters;
import com.automate.protocol.server.ServerProtocolParameters;
import com.automate.util.xml.XmlFormatException;

public class MessageManager extends ManagerBase<MessageListener> implements IMessageManager {

	private IPacketManager mPacketManager;

	private IConnectionManager mConnectionManager;

	private IncomingMessageParser<ServerProtocolParameters> mParser;

	private String mSessionKey;

	private int mMajorVersion;

	private int mMinorVersion;

	public MessageManager(IPacketManager packetManager, IConnectionManager connectionManager, 
			IncomingMessageParser<ServerProtocolParameters> parser, int majorVersion, int minorVersion) {
		super(MessageListener.class);
		mPacketManager = packetManager;
		mConnectionManager = connectionManager;
		mParser = parser;
		mMajorVersion = majorVersion;
		mMinorVersion = minorVersion;
	}

	@Override
	public void onMessageReceived(Message<ServerProtocolParameters> message) {
		synchronized (mListeners) {
			for(MessageListener listener : mListeners) {
				listener.onMessageReceived(message);
			}
		}
	}

	@Override
	public void onMessageSent(Message<ClientProtocolParameters> message) {
		synchronized (mListeners) {
			for(MessageListener listener : mListeners) {
				listener.onMessageSent(message);
			}
		}
	}

	@Override
	public void onBind(Class<? extends IListener> listenerClass) {}

	@Override
	public void onUnbind(Class<? extends IListener> listenerClass) {
	}

	@Override
	public void sendMessage(Message<ClientProtocolParameters> message) {
		sendMessage(message, null);
	}

	@Override
	public ClientProtocolParameters getProtocolParameters() {
		return new ClientProtocolParameters(mMajorVersion, mMinorVersion, mSessionKey);
	}

	@Override
	public String getSessionKey() {
		return mSessionKey;
	}

	@Override
	public void setSessionKey(String sessionKey) {
		this.mSessionKey = sessionKey;
	}

	@Override
	public void sendMessage(Message<ClientProtocolParameters> message, PacketSentListener listener) {
		try {
			StringBuilder builder = new StringBuilder();
			message.toXml(builder, 0);
			String xml = builder.toString();
			if(xml != null) {
				mPacketManager.sendPacket(xml, listener);
			}
		} catch (XmlFormatException e) {
			Log.e(getClass().getName(), "Error converting message to xml.");
		}
	}

	@Override
	protected void unbindSelf() {
		this.mPacketManager.unbind(this);
		this.mConnectionManager.unbind(this);
	}

	@Override
	protected void bindSelf() {
		this.mPacketManager.bind(this);
		this.mConnectionManager.bind(this);
	}

	@Override
	protected void setupInitialState() {
	}

	@Override
	protected void teardown() {}
	@Override
	protected void performInitialUpdate(MessageListener listener) {}

	@Override
	public void onPacketReceived(String packet) {
		try {
			Message<ServerProtocolParameters> message = this.mParser.parse(packet);
			onMessageReceived(message);
		} catch (Exception e) {
			Log.e(getClass().getName(), "Unable to parse packet.", e);
		}
	}

	@Override
	public void onEmptyPacketReceived() {}
	@Override
	public void onReceiveIoException() {}
	@Override
	public void onNoSocketProvided() {}
	@Override
	public void onReceiveError() {}
	@Override
	public void onPacketSent(int packetId) {}
	@Override
	public void onSendIoException(int packetId) {}
	@Override
	public void onSendNoServerAddress(int packetId) {}
	@Override
	public void onSendNoServerPort(int packetId) {}
	@Override
	public void onSendError(int packetId) {}
	@Override
	public void onConnecting() {}
	@Override
	public void onConnected() {}
	@Override
	public void onDisconnected() {
		mSessionKey = null;
	}

}
