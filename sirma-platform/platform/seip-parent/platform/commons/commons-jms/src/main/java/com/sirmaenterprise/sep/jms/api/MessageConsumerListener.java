package com.sirmaenterprise.sep.jms.api;

import javax.jms.Message;

import com.sirma.itt.seip.plugin.Plugin;

/**
 * Extension point to be notified when new message is received regardless of the source destination.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 21/06/2017
 */
public interface MessageConsumerListener extends Plugin {

	String EXTENSION_NAME = "messageConsumerListener";

	/**
	 * Called when new message is received before calling the user listener method. This is done in the receive
	 * transaction. Any error thrown will cause message redelivery if configured
	 *
	 * @param message the received read only message
	 */
	void beforeMessage(Message message);

	/**
	 * Called when the message is processed and the transaction is successfully committed. This is called outside the
	 * receive transaction.
	 */
	void onSuccess();

	/**
	 * Called when the message receiving failed and the transaction is rollbacked. This is called outside the
	 * receive transaction.
	 *
	 * @param e the exception that failed the message receiving
	 */
	void onError(Exception e);
}
