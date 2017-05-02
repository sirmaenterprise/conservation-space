package com.sirmaenterprise.sep.bpm.camunda.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirmaenterprise.sep.bpm.camunda.MockProvider;
import com.sirmaenterprise.sep.bpm.camunda.configuration.CamundaConfiguration;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationRuntimeException;

@RunWith(MockitoJUnitRunner.class)
public class MultiEngineServicesProducerTest {
	@Mock
	private CamundaConfiguration camundaConfiguration;
	@InjectMocks
	private MultiEngineServicesProducer multiEngineServicesProducer;

	private ProcessEngine processEngine;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		processEngine = MockProvider.mockProcessEngine(MockProvider.DEFAULT_ENGINE);
		ProcessEngines.registerProcessEngine(processEngine);

	}

	@Test
	public void testProcessEngine() throws Exception {
		when(camundaConfiguration.getEngineName())
				.thenReturn(new ConfigurationPropertyMock<String>(MockProvider.DEFAULT_ENGINE));
		ProcessEngine processEngine = multiEngineServicesProducer.provideEngine();
		assertNotNull(processEngine);
		assertEquals(this.processEngine, processEngine);
	}

	@Test(expected = CamundaIntegrationRuntimeException.class)
	public void testNotRegisteredProcessEngine() throws Exception {
		when(camundaConfiguration.getEngineName()).thenReturn(new ConfigurationPropertyMock<String>("notInitialized"));
		multiEngineServicesProducer.provideEngine();
	}
}
