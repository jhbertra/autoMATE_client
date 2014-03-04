package com.automate.client.messaging.managers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.automate.client.messaging.IncomingPacketListenerThread;
import com.automate.client.messaging.MessageReceiveReceiver;
import com.automate.client.messaging.MessageSendReceiver;
import com.automate.client.messaging.PacketReceivedListener;
import com.automate.client.messaging.PacketSentListener;
import com.automate.client.messaging.services.PacketDeliveryService;
import com.automate.client.messaging.services.PacketReceiveService;
import com.automate.protocol.Message;
import com.automate.protocol.client.ClientProtocolParameters;
import com.automate.util.xml.XmlFormatException;

public class MessageManager implements IMessageManager {

	private Context mContext;
	
	private MessageReceiveReceiver mReceiveReceiver;
	private MessageSendReceiver mDeliverReceiver;
	
	private PacketReceivedListener mPacketReceivedManager;
	private IPacketSentManager mPacketSentManager;

	private IncomingPacketListenerThread mIncomingPacketListenerThread;

	private String mServerAddress;
	private String mServerPort;
	private int mBindPort;
	
	private final Object packetIdLock = new Object();
	private int nextPacketId;

	private String mSessionKey;
	private int mMajorVersion;
	private int mMinorVersion;

	public MessageManager(Context context,
			PacketReceivedListener packetReceivedManager,
			IPacketSentManager packetSentManager, String serverAddress,
			String serverPort, int bindPort, int majorVersion,
			int minorVersion) {
		this.mContext = context;
		this.mPacketReceivedManager = packetReceivedManager;
		this.mPacketSentManager = packetSentManager;
		this.mServerAddress = serverAddress;
		this.mServerPort = serverPort;
		this.mBindPort = bindPort;
		this.mMajorVersion = majorVersion;
		this.mMinorVersion = minorVersion;
	}

	@Override
	public void start() {
		Log.d(getClass().getName(), "Starting MessagingService.");

		mReceiveReceiver = new MessageReceiveReceiver(mPacketReceivedManager);
		mContext.registerReceiver(mReceiveReceiver, new IntentFilter(PacketReceiveService.class.getName()));

		mDeliverReceiver = new MessageSendReceiver(mPacketSentManager);
		mContext.registerReceiver(mDeliverReceiver, new IntentFilter(PacketDeliveryService.class.getName()));

		this.mIncomingPacketListenerThread = new IncomingPacketListenerThread(mContext, mBindPort);
	}

	@Override
	public void stop() {
		mIncomingPacketListenerThread.cancel();
	}

	@Override
	public void sendMessage(Message<ClientProtocolParameters> message, PacketSentListener listener) {
		int packetId;
		synchronized (packetIdLock) {
			packetId = this.nextPacketId++;
		}
		Intent intent = new Intent(mContext, PacketDeliveryService.class);
		intent.putExtra(PacketDeliveryService.SERVER_ADDRESS, mServerAddress);
		intent.putExtra(PacketDeliveryService.SERVER_PORT, mServerPort);
		intent.putExtra(PacketDeliveryService.PACKET_ID, packetId);
		StringBuilder sb = new StringBuilder();
		try {
			message.toXml(sb, 0);
			intent.putExtra(PacketDeliveryService.DATA, sb.toString());
		} catch (XmlFormatException e) {
			Log.e(getClass().getName(), "Error formatting outgoing message.", e);
		}
		if(listener != null) {
			mPacketSentManager.addDeliveryListener(packetId, listener);
		}
		mContext.startService(intent);
	}

	@Override
	public void sendMessage(Message<ClientProtocolParameters> message) {
		sendMessage(message, null);
	}

	@Override
	public void setSessionKey(String sessionKey) {
		this.mSessionKey = sessionKey;
	}

	@Override
	public String getSessionKey() {
		return mSessionKey;
	}

	@Override
	public ClientProtocolParameters getProtocolParameters() {
		return new ClientProtocolParameters(mMajorVersion, mMinorVersion, mSessionKey);
	}

}
