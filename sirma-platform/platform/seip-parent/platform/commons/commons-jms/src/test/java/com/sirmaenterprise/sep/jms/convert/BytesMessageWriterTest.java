package com.sirmaenterprise.sep.jms.convert;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import javax.jms.BytesMessage;
import javax.jms.JMSContext;
import javax.jms.JMSException;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test the bytes message writer.
 * 
 * @author nvelkov
 */
public class BytesMessageWriterTest {
	BytesMessageWriter<InputStream> writer = new BytesMessageWriter.DefaultBytesMessageWriter();

	@Test
	public void should_writeInputStream_toBytesMessage() throws JMSException {
		try (JMSContext context = mock(JMSContext.class)) {
			BytesMessage message = mock(BytesMessage.class);
			when(context.createBytesMessage()).thenReturn(message);

			writer.write(Mockito.mock(InputStream.class), context);

			// It's important to verify that the inputstream is put in the message with that key.
			verify(message).setObjectProperty(eq("JMS_HQ_InputStream"), any(InputStream.class));
		}
	}
}
