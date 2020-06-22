package com.sirmaenterprise.sep.bpm.camunda.bpmn;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirmaenterprise.sep.bpm.camunda.bpmn.service.CamundaBPMNServiceImpl;
import com.sirmaenterprise.sep.bpm.camunda.service.BPMPropertiesConverter;
import com.sirmaenterprise.sep.bpm.exception.BPMException;
import com.sirmaenterprise.sep.bpm.model.ProcessConstants;

/**
 * The {@link CamundaIntegrationTest} tests the integration between SEP and Camunda.
 *
 * @author bbanchev
 */
public class CamundaIntegrationTest {
	@Rule
	public ProcessEngineRule processEngineRule = new ProcessEngineRule();

	@InjectMocks
	private CamundaBPMNServiceImpl processService;

	@Spy
	private ProcessEngine processEngine;

	@Mock
	private BPMPropertiesConverter modelConverter;

	@Before
	public void setUp() throws Exception {
		processService = new CamundaBPMNServiceImpl();

		processEngine = processEngineRule.getProcessEngine();
		ProcessEngines.registerProcessEngine(processEngine);

		MockitoAnnotations.initMocks(this);
	}

	@Test
	@Deployment
	public void testStartBPMN() throws BPMException {
		Instance instance = mock(Instance.class);
		when(instance.getIdentifier()).thenReturn("test");
		String businessKey = "myId";
		when(instance.getId()).thenReturn(businessKey);
		Map<String, Serializable> properties = new HashMap<>();
		properties.put("assignee", "assignee");
		properties.put("createdBy", "createdBy");

		when(instance.getOrCreateProperties()).thenReturn(properties);
		processService.startProcess(instance, new HashMap<>());

		ProcessInstanceQuery processInstanceBusinessKey = processEngine
				.getRuntimeService()
					.createProcessInstanceQuery()
					.processInstanceBusinessKey(businessKey);
		// verify that engine has started the instance
		assertEquals(1, processInstanceBusinessKey.count());
		ProcessInstance processInstance = processInstanceBusinessKey.list().get(0);
		verify(instance).add(eq(ProcessConstants.ACTIVITY_ID), eq(processInstance.getId()));
		Map<String, Object> variables = processEngine.getRuntimeService().getVariables(processInstance.getId());
		HashMap<String, Serializable> copyProperties = new HashMap<>();
		// auto updated property by the api
		copyProperties.put(ProcessConstants.OUTCOME, "start");
		assertEquals(copyProperties.keySet(), variables.keySet());
	}

	@After
	public void tearDown() throws Exception {
		ProcessEngines.destroy();
	}

}