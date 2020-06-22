package com.sirmaenterprise.sep.bpm.camunda.transitions.states;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.definition.StateTransition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.state.StateService;
import com.sirmaenterprise.sep.bpm.camunda.model.DomainProcessConstants;

/**
 * @author bbanchev
 */
@RunWith(MockitoJUnitRunner.class)
public class BPMStateTransitionProviderTest {

	@Mock
	private ProcessEngine processEngine;

	@Mock
	private StateService stateService;

	@InjectMocks
	private BPMStateTransitionProvider bpmStateTransitionProvider;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testProvideOnCompletedInstance() throws Exception {
		Instance tested = mock(Instance.class);
		when(tested.get(eq(DomainProcessConstants.COMPLETED_ON))).thenReturn(new Date());
		List<StateTransition> provide = bpmStateTransitionProvider.provide(tested);
		assertEquals(0, provide.size());
	}

	@Test
	public void testInvalidInstance() throws Exception {
		Instance tested = mock(Instance.class);
		when(tested.get(eq(DomainProcessConstants.COMPLETED_ON))).thenReturn(null);
		when(tested.getAsString(eq(DomainProcessConstants.TRANSITIONS))).thenReturn(null);
		when(tested.getAsString(eq(DomainProcessConstants.TRANSITIONS_NONPERSISTED))).thenReturn(null);
		List<StateTransition> provide = bpmStateTransitionProvider.provide(tested);
		assertEquals(0, provide.size());
		assertTrue(true);
	}

	@Test
	public void testInstanceWithTRANSITIONS() throws Exception {
		Instance tested = mock(Instance.class);
		when(tested.get(eq(DomainProcessConstants.COMPLETED_ON))).thenReturn(null);
		when(tested.getAsString(eq(DomainProcessConstants.TRANSITIONS))).thenReturn(
				"[{\"identifier\":\"complete\",\"transitionId\":\"complete\",\"name\":\"Complete\",\"properties\":{},\"mandatoryTypes\":[\"TASKST100\"]}]");
		List<StateTransition> provide = bpmStateTransitionProvider.provide(tested);
		assertEquals(1, provide.size());
		assertTrue(true);
	}

}
