package com.automate.client.managers.packet;

import java.util.HashMap;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.automate.client.managers.IListener;
import com.automate.client.managers.ManagerBase;
import com.automate.client.managers.security.ISecurityManager;
import com.automate.client.managers.security.ProtocolSecurityException;
import com.automate.client.packet.services.PacketDeliveryService;
import com.automate.client.packet.services.PacketReceiveService;
import com.automate.client.packet.services.PacketDeliveryService.PacketDeliveryServiceBinder;

public class PacketManager extends ManagerBase<PacketListener> implements IPacketManager {

	private Context mContext;
	
	private PacketReceiveReceiver mReceiveReceiver;
	private PacketSendReceiver mDeliverReceiver;

	private IncomingPacketListenerThread mIncomingPacketListenerThread;

	private String mServerAddress;
	private String mServerPort;
	
	private final Object packetIdLock = new Object();
	private int nextPacketId = 0;
	
	private ISecurityManager mSecurityManager;
	
	private HashMap<Integer, PacketSentListener> mTemporaryListeners;
	
	public PacketManager(Context mContext, IncomingPacketListenerThread mIncomingPacketListenerThread,
			String mServerAddress, String mServerPort, int mBindPort, ISecurityManager mSecurityManager) {
		super(PacketListener.class);
		this.mContext = mContext;
		this.mIncomingPacketListenerThread = mIncomingPacketListenerThread;
		this.mServerAddress = mServerAddress;
		this.mServerPort = mServerPort;
		this.mSecurityManager = mSecurityManager;
		
		this.mReceiveReceiver = new PacketReceiveReceiver(this);
		this.mDeliverReceiver = new PacketSendReceiver(this);
		
		this.mTemporaryListeners = new HashMap<Integer, PacketSentListener>();
	}

	@Override
	public void onPacketReceived(String packet) {
		synchronized (mListeners) {
			for(PacketListener listener : mListeners) {
				listener.onPacketReceived(packet);
			}
		}
	}

	@Override
	public void onEmptyPacketReceived() {
		synchronized (mListeners) {
			for(PacketListener listener : mListeners) {
				listener.onEmptyPacketReceived();
			}
		}
	}

	@Override
	public void onReceiveIoException() {
		synchronized (mListeners) {
			for(PacketListener listener : mListeners) {
				listener.onReceiveIoException();
			}
		}
	}

	@Override
	public void onNoSocketProvided() {
		synchronized (mListeners) {
			for(PacketListener listener : mListeners) {
				listener.onNoSocketProvided();
			}
		}
	}

	@Override
	public void onReceiveError() {
		synchronized (mListeners) {
			for(PacketListener listener : mListeners) {
				listener.onReceiveError();
			}
		}
	}

	@Override
	public void onBind(Class<? extends IListener> listenerClass) {}
	@Override
	public void onUnbind(Class<? extends IListener> listenerClass) {}

	@Override
	public void onPacketSent(int packetId) {
		if(mTemporaryListeners.containsKey(packetId)) {
			PacketSentListener listener = mTemporaryListeners.remove(packetId);
			listener.onPacketSent(packetId);
		}
		synchronized (mListeners) {
			for(PacketListener listener : mListeners) {
				listener.onPacketSent(packetId);
			}
		}
	}

	@Override
	public void onSendIoException(int packetId) {
		if(mTemporaryListeners.containsKey(packetId)) {
			PacketSentListener listener = mTemporaryListeners.remove(packetId);
			listener.onSendIoException(packetId);
		}
		synchronized (mListeners) {
			for(PacketListener listener : mListeners) {
				listener.onSendIoException(packetId);
			}
		}
	}

	@Override
	public void onSendNoServerAddress(int packetId) {
		if(mTemporaryListeners.containsKey(packetId)) {
			PacketSentListener listener = mTemporaryListeners.remove(packetId);
			listener.onSendNoServerAddress(packetId);
		}
		synchronized (mListeners) {
			for(PacketListener listener : mListeners) {
				listener.onSendNoServerAddress(packetId);
			}
		}
	}

	@Override
	public void onSendNoServerPort(int packetId) {
		if(mTemporaryListeners.containsKey(packetId)) {
			PacketSentListener listener = mTemporaryListeners.remove(packetId);
			listener.onSendNoServerPort(packetId);
		}
		synchronized (mListeners) {
			for(PacketListener listener : mListeners) {
				listener.onSendNoServerPort(packetId);
			}
		}
	}

	@Override
	public void onSendError(int packetId) {
		if(mTemporaryListeners.containsKey(packetId)) {
			PacketSentListener listener = mTemporaryListeners.remove(packetId);
			listener.onSendError(packetId);
		}
		synchronized (mListeners) {
			for(PacketListener listener : mListeners) {
				listener.onSendError(packetId);
			}
		}
	}

	@Override
	public void sendPacket(String packet) {
		sendPacket(packet, null);
	}

	@Override
	public void sendPacket(String packet, PacketSentListener listener) {
		int packetId;
		synchronized (packetIdLock) {
			packetId = this.nextPacketId++;
		}
		Intent intent = new Intent(mContext, PacketDeliveryService.class);
		intent.putExtra(PacketDeliveryService.SERVER_ADDRESS, mServerAddress);
		intent.putExtra(PacketDeliveryService.SERVER_PORT, mServerPort);
		intent.putExtra(PacketDeliveryService.PACKET_ID, packetId);
		try {
			intent.putExtra(PacketDeliveryService.DATA, mSecurityManager.encrypt(packet));
		} catch (ProtocolSecurityException e) {
			Log.w(getClass().getName(), "Error encrypting outgoing packet.", e);
		}
		if(listener != null) {
			mTemporaryListeners.put(packetId, listener);
		}
		mContext.startService(intent);
		mContext.bindService(intent, new ServiceConnection() {
			public void onServiceDisconnected(ComponentName name) {}
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				((PacketDeliveryServiceBinder)service).setListenThread(mIncomingPacketListenerThread);
			}
		}, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	public void receivePacket(String packet) {
		try {
			this.onPacketReceived(mSecurityManager.decrypt(packet));
		} catch (ProtocolSecurityException e) {
			Log.w(getClass().getName(), "SecurityManager could not decrypt packet.", e);
		}
	}

	@Override
	protected void unbindSelf() {		
		this.mSecurityManager.unbind(this);
	}

	@Override
	protected void bindSelf() {
		this.mSecurityManager.bind(this);
	}

	@Override
	protected void setupInitialState() {
		mIncomingPacketListenerThread.start();
		mContext.registerReceiver(mDeliverReceiver, new IntentFilter(PacketDeliveryService.class.getName()));
		mContext.registerReceiver(mReceiveReceiver, new IntentFilter(PacketReceiveService.class.getName()));
	}
	
	@Override
	protected void teardown() {
		mIncomingPacketListenerThread.cancel();
		mContext.unregisterReceiver(mDeliverReceiver);
		mContext.unregisterReceiver(mReceiveReceiver);
	}

	@Override
	protected void performInitialUpdate(PacketListener listener) {}

	@Override
	public void onDisconnected() {
		mIncomingPacketListenerThread.onDisconnected();
	}

}
