package com.automate.client.messaging.handlers;

import com.automate.protocol.Message;
import com.automate.protocol.server.ServerProtocolParameters;

/**
 * Delegate for handling incoming messages
 * @author jamie.bertram
 *
 * @param <M> The type of message this delegate handles
 * @param <Params> container class for additional parameters.
 */
public interface IMessageHandler<M extends Message<ServerProtocolParameters>, Params> {

	/**
	 * Handle the message upon receipt.
	 * @param majorVersion TODO
	 * @param minorVersion TODO
	 * @param sessionValid If the session was valid
	 * @param message the message received from the client
	 * @return a response message if response is required by protocol spec.
	 */
	public abstract Message<ServerProtocolParameters> handleMessage(int majorVersion, int minorVersion, M message, Params params);
	
}
