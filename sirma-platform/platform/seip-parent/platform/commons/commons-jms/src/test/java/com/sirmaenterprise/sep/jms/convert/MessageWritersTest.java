package com.sirmaenterprise.sep.jms.convert;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import javax.jms.BytesMessage;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link MessageWriters}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 05/06/2017
 */
public class MessageWritersTest {

	private MessageWriters writers;

	@Before
	public void setUp() throws Exception {
		writers = new MessageWriters();
	}

	@Test
	public void registerClass_doNothingOnNullClass() throws Exception {
		writers.register(Object.class, (Class<? extends MessageWriter>) null);
		Optional<MessageWriter<Object, Message>> writerOptional = writers.getWriterFor(Object.class);
		assertNotNull(writerOptional);
		assertFalse(writerOptional.isPresent());
	}

	@Test
	public void registerClass_shouldBindItToCorrectType() throws Exception {
		writers.register(Object.class, Writer.class);

		Optional<MessageWriter<Object, Message>> writerOptional = writers.getWriterFor(Object.class);
		assertNotNull(writerOptional);
		assertTrue(writerOptional.isPresent());
		assertTrue(writerOptional.get() instanceof Writer);
	}

	@Test
	public void registerInstance_doNothingOnNullClass() throws Exception {
		writers.register(Object.class, (MessageWriter) null);
		Optional<MessageWriter<Object, Message>> writerOptional = writers.getWriterFor(Object.class);
		assertNotNull(writerOptional);
		assertFalse(writerOptional.isPresent());
	}

	@Test
	public void registerInstance_shouldBindItToCorrectType() throws Exception {
		writers.register(Object.class, new Writer());

		Optional<MessageWriter<Object, Message>> writerOptional = writers.getWriterFor(Object.class);
		assertNotNull(writerOptional);
		assertTrue(writerOptional.isPresent());
		assertTrue(writerOptional.get() instanceof Writer);
	}

	@Test
	public void getWriterForShould_ResolveMapWriter_WhenRequestedFromGeneric() throws Exception {
		writers.register(Object.class, new MapWriter());
		Optional<MessageWriter<Object, Message>> writerOptional = writers.getWriterFor(Object.class);
		assertNotNull(writerOptional);
		assertTrue(writerOptional.isPresent());
		assertTrue(writerOptional.get() instanceof MapWriter);
	}

	@Test
	public void should_resolverBytesWriter_whenRequestedFromGeneric() throws Exception {
		writers.register(Object.class, new BytesWriter());
		Optional<MessageWriter<Object, Message>> writerOptional = writers.getWriterFor(Object.class);
		assertNotNull(writerOptional);
		assertTrue(writerOptional.isPresent());
		assertTrue(writerOptional.get() instanceof BytesWriter);
	}

	@Test
	public void getWriterForShould_ResolveObjectWriter_WhenRequestedFromGeneric() throws Exception {
		writers.register(Object.class, new ObjectWriter());
		Optional<MessageWriter<Object, Message>> writerOptional = writers.getWriterFor(Object.class);
		assertNotNull(writerOptional);
		assertTrue(writerOptional.isPresent());
		assertTrue(writerOptional.get() instanceof ObjectWriter);
	}

	@Test
	public void buildWriter() throws Exception {
		Optional<Writer> writer = writers.buildWriter(Writer.class);
		assertNotNull(writer);
		assertTrue(writer.isPresent());
		assertTrue(writer.get() instanceof Writer);
	}

	@Test
	public void addWriterBuilder() throws Exception {
		MessageWriterBuilder builder = mock(MessageWriterBuilder.class);
		when(builder.build(any())).thenReturn(new Writer());

		writers.addWriterBuilder(builder);

		Optional<Writer> writer = writers.buildWriter(Writer.class);
		assertNotNull(writer);
		assertTrue(writer.isPresent());
		assertTrue(writer.get() instanceof Writer);
		verify(builder).build(Writer.class);
	}

	@Test
	public void writerBuilder_shouldNotFail_IfBuilderFails() throws Exception {
		MessageWriterBuilder builder = mock(MessageWriterBuilder.class);
		when(builder.build(any())).thenThrow(RuntimeException.class);

		writers.addWriterBuilder(builder);

		Optional<NotClassicWriter> writer = writers.buildWriter(NotClassicWriter.class);
		assertNotNull(writer);
		assertFalse(writer.isPresent());
		verify(builder).build(NotClassicWriter.class);
	}

	@Test
	public void defaultWriterBuilder_shouldNotFail_IfConstructorThrowsException() throws Exception {
		Optional<FailingWriter> writer = writers.buildWriter(FailingWriter.class);
		assertNotNull(writer);
		assertFalse(writer.isPresent());
	}

	private static class Writer implements MessageWriter {

		@Override
		public Message write(Object data, JMSContext context) throws JMSException {
			return null;
		}
	}

	private static class MapWriter implements MapMessageWriter {

		@Override
		public void write(Object data, MapMessage message) throws JMSException {

		}
	}

	private static class ObjectWriter implements ObjectMessageWriter {

		@Override
		public void write(Object data, ObjectMessage message) throws JMSException {

		}
	}

	private static class BytesWriter implements BytesMessageWriter {

		@Override
		public void write(Object data, BytesMessage message) throws JMSException {
			// nothing.
		}

	}

	private static class NotClassicWriter implements ObjectMessageWriter {

		private NotClassicWriter(boolean dummy) {
			// no default constructor
		}

		@Override
		public void write(Object data, ObjectMessage message) throws JMSException {

		}
	}

	private static class FailingWriter implements ObjectMessageWriter {

		private FailingWriter() {
			throw new RuntimeException();
		}

		@Override
		public void write(Object data, ObjectMessage message) throws JMSException {

		}
	}
}
