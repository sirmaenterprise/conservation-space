package com.sirma.itt.seip.instance.actions.evaluation;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.instance.DomainInstanceService;

/**
 * Tests for {@link ActionsRestService}.
 *
 * @author A. Kunchev
 */
@RunWith(MockitoJUnitRunner.class)
public class ActionsRestServiceTest {

	@InjectMocks
	private ActionsRestService service;

	@Mock
	private InstanceActionsEvaluatior actionsEvaluatior;

	@Mock
	private DomainInstanceService domainInstanceService;

	@Test
	public void getFlatActions_internalServiceCalled() {
		service.getFlatActions("targetId", "placeholder");
		verify(actionsEvaluatior).evaluate(any(InstanceActionsRequest.class));
	}

	@Test
	public void getActions_internalServiceCalled() {
		service.getActions("targetId", "placeholder");
		verify(actionsEvaluatior).evaluateAndBuildMenu(any(InstanceActionsRequest.class));
	}
}