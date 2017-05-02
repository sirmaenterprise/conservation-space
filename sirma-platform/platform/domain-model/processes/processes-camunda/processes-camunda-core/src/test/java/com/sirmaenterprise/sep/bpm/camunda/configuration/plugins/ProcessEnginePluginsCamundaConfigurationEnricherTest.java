package com.sirmaenterprise.sep.bpm.camunda.configuration.plugins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirmaenterprise.sep.bpm.camunda.bpmn.parse.BPMNParserEventListener;
import com.sirmaenterprise.sep.bpm.camunda.configuration.SepProcessEngineConfigurator;
import com.sirmaenterprise.sep.bpm.camunda.plugin.IdentityIntegrationPlugin;

/**
 * Tests {@link ProcessEnginePluginsCamundaConfigurationEnricher}
 * 
 * @author bbanchev
 */
public class ProcessEnginePluginsCamundaConfigurationEnricherTest {
	@Mock
	private IdentityIntegrationPlugin identityIntegrationPlugin;
	@Mock
	private BPMNParserEventListener bpmnParserEventListener;
	@InjectMocks
	private ProcessEnginePluginsCamundaConfigurationEnricher processEnginePlugins;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testEnrich() throws Exception {
		SepProcessEngineConfigurator sepProcessEngineConfigurator = new SepProcessEngineConfigurator();
		processEnginePlugins.enrich(sepProcessEngineConfigurator);
		assertNotNull(sepProcessEngineConfigurator.getProcessEnginePlugins());
		assertEquals(5, sepProcessEngineConfigurator.getProcessEnginePlugins().size());
	}

}
