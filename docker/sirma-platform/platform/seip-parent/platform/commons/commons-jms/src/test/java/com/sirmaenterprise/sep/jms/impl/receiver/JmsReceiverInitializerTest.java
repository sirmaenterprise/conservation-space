package com.sirmaenterprise.sep.jms.impl.receiver;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;

import com.sirmaenterprise.sep.jms.api.ReceiverDefinition;
import com.sirmaenterprise.sep.jms.impl.JmsDefinitionProvider;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 16/05/2017
 */
public class JmsReceiverInitializerTest {
	@InjectMocks
	private JmsReceiverInitializer initializer;

	@Mock
	private JmsDefinitionProvider destinationsProvider;
	@Mock
	private JmsReceiverManager receiverManager;
	@Mock
	private ReceiverDefinition receiverDefinition;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(destinationsProvider.getDefinitions()).thenReturn(new HashSet<>(Arrays.asList(receiverDefinition)));
	}

	@Test
	public void initializeListeners() throws Exception {
		initializer.initializeListeners();

		verify(receiverManager).registerJmsListener(receiverDefinition);
		verify(receiverManager).start();
	}

}
