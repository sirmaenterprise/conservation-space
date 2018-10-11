package com.sirmaenterprise.sep.jms.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.jms.Message;
import javax.jms.TextMessage;

import org.junit.Test;

/**
 * Test for {@link MethodMessageConsumer}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 17/05/2017
 */
public class MethodMessageConsumerTest {

	private MethodMessageConsumer messageConsumer;

	@Test
	public void getExpectedType() throws Exception {
		messageConsumer = new MethodMessageConsumer(getClass().getDeclaredMethod("onMessage", Message.class));
		assertEquals(Message.class, messageConsumer.getExpectedType());

		messageConsumer = new MethodMessageConsumer(getClass().getDeclaredMethod("onTextMessage", TextMessage.class));
		assertEquals(TextMessage.class, messageConsumer.getExpectedType());
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorShouldRejectInvalidMethods() throws Exception {
		new MethodMessageConsumer(getClass().getDeclaredMethod("onText", String.class));
	}

	@Test
	public void isAcceptable() throws Exception {
		assertTrue(
				MethodMessageConsumer.isAcceptable(getClass().getDeclaredMethod("onTextMessage", TextMessage.class)));
		assertTrue(MethodMessageConsumer.isAcceptable(getClass().getDeclaredMethod("onMessage", Message.class)));
		assertFalse(MethodMessageConsumer.isAcceptable(getClass().getDeclaredMethod("onText", String.class)));
	}

	void onMessage(Message message) {
		assertNotNull(message);
	}

	void onTextMessage(TextMessage message) {
		assertNotNull(message);
	}

	void onText(String text) {
	}
}
