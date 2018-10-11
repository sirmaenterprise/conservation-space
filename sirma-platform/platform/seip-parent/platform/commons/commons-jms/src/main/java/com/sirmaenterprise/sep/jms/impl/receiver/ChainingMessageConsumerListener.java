package com.sirmaenterprise.sep.jms.impl.receiver;

import javax.inject.Inject;
import javax.jms.Message;

import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirmaenterprise.sep.jms.api.MessageConsumerListener;

/**
 * Default implementation of the message listener that calls all other extensions in order they are defined.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 21/06/2017
 */
class ChainingMessageConsumerListener implements MessageConsumerListener {

	@Inject
	@ExtensionPoint(value = MessageConsumerListener.EXTENSION_NAME)
	private Plugins<MessageConsumerListener> listeners;

	@Override
	public void beforeMessage(Message message) {
		for (MessageConsumerListener listener : listeners) {
			listener.beforeMessage(message);
		}
	}

	@Override
	public void onSuccess() {
		for (MessageConsumerListener listener : listeners) {
			listener.onSuccess();
		}
	}

	@Override
	public void onError(Exception e) {
		for (MessageConsumerListener listener : listeners) {
			listener.onError(e);
		}
	}
}
