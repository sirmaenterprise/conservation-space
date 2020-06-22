package com.sirmaenterprise.sep.jms.utils;

import static com.sirma.itt.seip.collections.CollectionUtils.createHashMap;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.Serializable;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;

import org.junit.Test;

import com.sirmaenterprise.sep.jms.exception.JmsRuntimeException;

/**
 * Test for {@link MessageUtil}.
 *
 * @author A. Kunchev
 */
@SuppressWarnings("static-method")
public class MessageUtilTest {

	private static final Map<String, Serializable> ATTRIBUTES;
	static {
		ATTRIBUTES = createHashMap(2);
		ATTRIBUTES.put("key-1", "value-1");
		ATTRIBUTES.put("key-2", "value-2");
	}

	@Test
	public void enrichMessage() throws JMSException {
		Message message = mock(Message.class);
		MessageUtil.enrichMessage(ATTRIBUTES, message);
		verify(message, times(2)).setObjectProperty(anyString(), any());
	}

	@Test(expected = JmsRuntimeException.class)
	public void enrichMessage_error() throws JMSException {
		Message message = mock(Message.class);
		doThrow(new JMSException("error")).when(message).setObjectProperty(eq("key-2"), any());

		MessageUtil.enrichMessage(ATTRIBUTES, message);
	}
}