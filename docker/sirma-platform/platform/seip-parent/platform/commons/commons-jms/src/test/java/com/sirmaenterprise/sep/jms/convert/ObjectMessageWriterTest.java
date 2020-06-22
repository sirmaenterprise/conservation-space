package com.sirmaenterprise.sep.jms.convert;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSContext;
import javax.jms.ObjectMessage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test for {@link ObjectMessageWriter}
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 05/06/2017
 */
public class ObjectMessageWriterTest {
	@Mock
	private JMSContext context;
	@Mock
	private ObjectMessage message;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(context.createObjectMessage()).thenReturn(message);
	}

	@Test
	public void defaultWriterShouldSuccessed_WritingSerializableData() throws Exception {
		Map<String, Serializable> data = new HashMap<>();

		ObjectMessageWriter.instance().write(data, context);
		verify(message).setObject((Serializable) data);
	}

	@Test(expected = IllegalArgumentException.class)
	public void defaultWriterShouldFail_onNonSerializableData() throws Exception {
		ObjectMessageWriter.instance().write(new Object(), context);
	}

}
