package com.sirma.itt.seip.instance.headers.batch;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.instance.headers.InstanceHeaderService;

/**
 * Test for {@link InstanceHeaderItemProcessor}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 27/11/2017
 */
public class InstanceHeaderItemProcessorTest {
	@InjectMocks
	private InstanceHeaderItemProcessor processor;
	@Mock
	private InstanceHeaderService instanceHeaderService;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void processItem_shouldEvaluateHeader() throws Exception {
		Instance instance = new EmfInstance();
		instance.setId("emf:instance");
		instance.setIdentifier("definitionId");
		instance.setType(InstanceType.create("emf:Case"));
		instance.add(DefaultProperties.HEADER_LABEL, "oldHeaderValue");
		when(instanceHeaderService.evaluateHeader(instance)).thenReturn(Optional.of("headerValue"));

		Object value = processor.processItem(instance);

		assertTrue(value instanceof GeneratedHeaderData);
		GeneratedHeaderData item = (GeneratedHeaderData) value;
		assertEquals("headerValue", item.getHeader());
		assertEquals("emf:instance", item.getInstanceId());
	}

	@Test
	public void processItem_shouldHandleNoHeader() throws Exception {
		Instance instance = new EmfInstance();
		when(instanceHeaderService.evaluateHeader(instance)).thenReturn(Optional.empty());

		Object value = processor.processItem(instance);
		assertNull(value);
	}

}
