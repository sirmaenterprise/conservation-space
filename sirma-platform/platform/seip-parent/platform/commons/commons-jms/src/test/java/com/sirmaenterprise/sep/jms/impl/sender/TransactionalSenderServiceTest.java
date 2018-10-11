package com.sirmaenterprise.sep.jms.impl.sender;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.HashMap;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirmaenterprise.sep.jms.api.JmsContextProvider;
import com.sirmaenterprise.sep.jms.api.JmsDestinationResolver;
import com.sirmaenterprise.sep.jms.api.MessageSender;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.convert.MessageWriters;
import com.sirmaenterprise.sep.jms.exception.JmsRuntimeException;
import com.sirmaenterprise.sep.jms.exception.MissingMessageWriterException;

/**
 * Test for {@link TransactionalSenderService}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 22/05/2017
 */
public class TransactionalSenderServiceTest {
	@InjectMocks
	private TransactionalSenderService senderService;
	@Mock
	private JmsContextProvider contextProvider;
	@Mock
	private JmsDestinationResolver destinationResolver;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private SecurityContextManager securityContextManager;
	@Mock
	private User user;
	@Mock
	private JMSContext context;
	@Mock
	private Destination destination;
	@Mock
	private JMSProducer producer;
	@Mock
	private Message message;
	@Mock
	private ObjectMessage objectMessage;
	@Mock
	private MapMessage mapMessage;
	@Mock
	private BytesMessage bytesMessage;
	@Mock
	private TextMessage textMessage;

	@Spy
	private MessageWriters messageWriters;

	@Before
	public void setUp() throws Exception {
		messageWriters = new MessageWriters();

		MockitoAnnotations.initMocks(this);

		when(securityContextManager.getCurrentContext()).thenReturn(securityContext);
		when(user.getSystemId()).thenReturn("emf:user");
		when(securityContext.getAuthenticated()).thenReturn(user);
		when(securityContext.getEffectiveAuthentication()).thenReturn(user);
		when(securityContext.getCurrentTenantId()).thenReturn("tenant.com");
		when(securityContext.getCurrentTenantId()).thenReturn("request-id");

		when(contextProvider.provide()).thenReturn(context);
		when(context.createProducer()).thenReturn(producer);
		when(destinationResolver.resolve(anyString())).thenReturn(destination);

		when(context.createMessage()).thenReturn(message);
		when(context.createTextMessage()).thenReturn(textMessage);
		when(context.createTextMessage(anyString())).thenReturn(textMessage);
		when(context.createMapMessage()).thenReturn(mapMessage);
		when(context.createObjectMessage()).thenReturn(objectMessage);
		when(context.createBytesMessage()).thenReturn(bytesMessage);

		when(producer.setAsync(any())).thenReturn(producer);
		when(producer.setDeliveryDelay(anyLong())).thenReturn(producer);
		when(producer.setDeliveryMode(anyInt())).thenReturn(producer);
		when(producer.setJMSType(any())).thenReturn(producer);
		when(producer.setPriority(anyInt())).thenReturn(producer);
		when(producer.setTimeToLive(anyLong())).thenReturn(producer);
		when(producer.setJMSCorrelationID(any())).thenReturn(producer);
		when(producer.setJMSReplyTo(any())).thenReturn(producer);
	}

	@After
	public void tearDown() throws Exception {
		senderService.onTransactionCommit();
		verify(context, atLeastOnce()).close();
	}

	@Test
	public void send_objectAfterGenericConvert() throws Exception {
		messageWriters.register(Object.class, (data, ctx) -> ctx.createTextMessage());
		senderService.send("testQueue", new Object());
		verify(producer).send(destination, textMessage);
	}

	@Test
	public void sendHeadersOnlyMessageViaBuilder() throws Exception {
		senderService.send("testQueue", message -> message.setStringProperty("key", "value"));
		verify(producer).send(destination, message);
	}

	@Test(expected = JmsRuntimeException.class)
	public void sendHeadersOnly_ShouldFail_OnBuilderFailure() throws Exception {
		senderService.send("testQueue", message -> { throw new JMSException(""); });
	}

	@Test
	public void sendMessageUsingCustomWriter() throws Exception {
		senderService.send("testQueue", "serializableData", (data, ctx) -> {
			ObjectMessage message = ctx.createObjectMessage();
			message.setObject(data);
			return message;
		}, SendOptions.create());
		verify(producer).send(destination, objectMessage);
	}

	@Test
	public void sendMessageUsingRegisteredWriter() throws Exception {
		senderService.registerObjectWriter(String.class, (data, message) -> {
			message.setObject(data);
		});
		senderService.send("testQueue", "serializableData", null, SendOptions.create());
		verify(producer).send(destination, objectMessage);
	}

	@Test
	public void sendMessage_shouldUseDefaultMapWriterForMap() throws Exception {
		senderService.send("testQueue", new HashMap<>(), null, SendOptions.create());
		verify(producer).send(destination, mapMessage);
	}

	@Test
	public void sendMessage_shouldUseDefaultObjectWriterForSerializable() throws Exception {
		senderService.send("testQueue", "some serializable data", null, SendOptions.create());
		verify(producer).send(destination, objectMessage);
	}

	@Test
	public void sendMessage_shouldUseDefaultBytesWriterForInputStream() throws Exception {
		senderService.send("testQueue", Mockito.mock(InputStream.class), null, SendOptions.create());
		verify(producer).send(destination, bytesMessage);
	}
	
	@Test(expected = MissingMessageWriterException.class)
	public void sendMessage_shouldFailIfMissingWriter() throws Exception {
		senderService.send("testQueue", new Object(), null, SendOptions.create());
	}

	@Test(expected = JmsRuntimeException.class)
	public void sendMessageUsingTheOptionsWriter_ShouldFail_OnWriterFailure() throws Exception {
		senderService.send("testQueue", "serializableData", SendOptions.create().withWriter((data, ctx) -> {
			throw new JMSException("");
		}));
		verify(producer).send(destination, objectMessage);
	}

	@Test
	public void sendObjectUsingObjectBuilder() throws Exception {
		senderService.sendObject("testQueue", msg -> msg.setObject("testData"));
		verify(producer).send(destination, objectMessage);
		verify(objectMessage).setObject("testData");
	}

	@Test
	public void sendObjectUsingGivenObjectWriter() throws Exception {
		senderService.sendObject("testQueue", "testData", ((data, message1) -> {
			message1.setObject("test" + data);
		}));
		verify(producer).send(destination, objectMessage);
		verify(objectMessage).setObject("testtestData");
	}

	@Test
	public void sendObjectUsingRegisteredObjectWriter() throws Exception {
		senderService.registerObjectWriter(String.class, ((data, message1) -> {
			message1.setObject("test" + data);
		}));
		senderService.sendObject("testQueue", "testData", null);
		verify(producer).send(destination, objectMessage);
		verify(objectMessage).setObject("testtestData");
	}

	@Test(expected = JmsRuntimeException.class)
	public void sendObjectUsingObjectBuilder_ShouldFail_OnWriterFailure() throws Exception {
		senderService.sendObject("testQueue", msg -> { throw new JMSException(""); });
	}

	@Test
	public void sendText() throws Exception {
		senderService.sendText("testQueue", "textToSend");
		verify(producer).send(destination, textMessage);
		verify(context).createTextMessage("textToSend");
	}

	@Test
	public void sendTextViaMessageBuilder() throws Exception {
		senderService.sendText("testQueue", msg -> msg.setText("customBuilder"));
		verify(producer).send(destination, textMessage);
		verify(textMessage).setText("customBuilder");
	}

	@Test(expected = JmsRuntimeException.class)
	public void sendTextViaMessageBuilder_ShouldFail_OnWriterFailure() throws Exception {
		senderService.sendText("testQueue", msg -> { throw new JMSException(""); });
		verify(producer).send(destination, textMessage);
		verify(textMessage).setText("customBuilder");
	}

	@Test
	public void sendMapViaMessageBuilder() throws Exception {
		senderService.sendMap("testQueue", msg -> msg.setObject("key", "builderValue"));
		verify(producer).send(destination, mapMessage);
		verify(mapMessage).setObject("key", "builderValue");
	}

	@Test(expected = JmsRuntimeException.class)
	public void sendMapViaMessageBuilder_ShouldFail_OnWriterFailure() throws Exception {
		senderService.sendMap("testQueue", msg -> { throw new JMSException(""); });
	}

	@Test
	public void sendMapForCustomObject() throws Exception {
		senderService.sendMap("testQueue", new Object(),
				(data, msg) -> msg.setObject("key", data.getClass().getName()));
		verify(producer).send(destination, mapMessage);
		verify(mapMessage).setObject("key", Object.class.getName());
	}

	@Test(expected = MissingMessageWriterException.class)
	public void sendMapForCustomObject_ShouldFail_OnMissingWriter() throws Exception {
		senderService.sendMap("testQueue", new Object(),
				null, SendOptions.create().withMapWriter(null));
	}

	@Test(expected = JmsRuntimeException.class)
	public void sendMapForCustomObject_ShouldFail_OnWriterFailure() throws Exception {
		senderService.sendMap("testQueue", new Object(),
				(data, msg) -> { throw new JMSException(""); });
	}

	@Test
	public void createSender() throws Exception {
		MessageSender sender = senderService.createSender("testQueue");
		assertNotNull(sender);
		assertTrue(sender.isActive());

		senderService.onTransactionCommit();
		assertFalse(sender.isActive());
	}

	@Test
	public void sendShouldSetCustomPropertiesFromOptions() throws Exception {
		senderService.sendText("testQueue", "textToSend", SendOptions.create()
				.replyTo("replyQueue")
				.withProperty("headerProp1", "value1")
				.withProperty("headerProp2", "value2")
				.noCorrelationId());
		verify(producer).send(destination, textMessage);
		verify(producer).setJMSReplyTo(destination);
		verify(textMessage, times(6)).setObjectProperty(any(), any());
		verify(producer, times(1)).setJMSCorrelationID(null);
	}

	@Test
	public void sendShouldNotSetAuthenticatedUserIfSystem() throws Exception {
		when(securityContextManager.isCurrentUserSystem()).thenReturn(Boolean.TRUE);
		senderService.sendText("testQueue", "textToSend");
		verify(producer).send(destination, textMessage);
		verify(textMessage, times(2)).setObjectProperty(any(), any());
	}

	@Test(expected = JmsRuntimeException.class)
	public void send_ShouldFail_OnSendError() throws Exception {
		when(producer.send(destination, textMessage)).thenThrow(JMSException.class);
		senderService.sendText("testQueue", "textToSend");
	}

	@Test(expected = JmsRuntimeException.class)
	public void send_ShouldFail_OnPropertiesWriterFailure() throws Exception {
		doThrow(JMSException.class).when(textMessage).setObjectProperty(anyString(), any());
		when(producer.send(destination, textMessage)).thenThrow(JMSException.class);
		senderService.sendText("testQueue", "textToSend", SendOptions.create().withProperty("key", "value"));
	}

	@Test
	public void getCurrentContext() {
		assertEquals(context, senderService.getCurrentContext());
	}

}
