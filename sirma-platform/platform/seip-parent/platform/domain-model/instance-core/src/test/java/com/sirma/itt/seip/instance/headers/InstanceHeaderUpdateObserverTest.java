package com.sirma.itt.seip.instance.headers;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.instance.event.InstanceChangeEvent;

/**
 * Test for {@link InstanceHeaderUpdateObserver}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 27/11/2017
 */
public class InstanceHeaderUpdateObserverTest {
	@InjectMocks
	private InstanceHeaderUpdateObserver observer;
	@Mock
	private InstanceHeaderService headerService;
	@Spy
	private InstancePropertyNameResolver fieldConverter = InstancePropertyNameResolver.NO_OP_INSTANCE;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void onInstanceChange_setNonNullHeader() throws Exception {
		when(headerService.evaluateHeader(any())).thenReturn(Optional.of("headerValue"));

		Instance instance = new EmfInstance();
		observer.onInstanceChange(new InstanceChangeEvent<>(instance));

		assertEquals("headerValue", instance.get(DefaultProperties.HEADER_LABEL));
	}

	@Test
	public void onInstanceChange_setNullHeader() throws Exception {
		when(headerService.evaluateHeader(any())).thenReturn(Optional.empty());

		Instance instance = new EmfInstance();
		observer.onInstanceChange(new InstanceChangeEvent<>(instance));

		assertNull(instance.get(DefaultProperties.HEADER_LABEL));
	}
}
