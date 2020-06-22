package com.sirmaenterprise.sep.bpm.camunda.configuration.plugins;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirmaenterprise.sep.bpm.camunda.configuration.WorkflowConfigurations;

/**
 * {@link WorkflowSearchArgumentProvider}.
 *
 * @author A. Kunchev
 */
public class WorkflowSearchArgumentProviderTest {

	@InjectMocks
	private WorkflowSearchArgumentProvider provider;

	@Mock
	private WorkflowConfigurations workflowConfigurations;

	@Before
	public void setup() {
		provider = new WorkflowSearchArgumentProvider();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void provide() {
		when(workflowConfigurations.getWorkflowPriorityLow()).thenReturn("low");
		when(workflowConfigurations.getWorkflowPriorityNormal()).thenReturn("normal");
		when(workflowConfigurations.getWorkflowPriorityHigh()).thenReturn("high");

		Context<String, Object> context = new Context<>();
		provider.provide(new SearchRequest(), context);
		assertEquals(3, context.size());
	}
}