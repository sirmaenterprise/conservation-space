package com.sirma.itt.seip.instance.actions;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.event.AfterInstancePersistEvent;
import com.sirma.itt.seip.instance.event.BeforeInstancePersistEvent;
import com.sirma.itt.seip.instance.script.TransitionScriptEvaluator;
import com.sirma.itt.seip.instance.state.AfterOperationExecutedEvent;
import com.sirma.itt.seip.instance.state.BeforeOperationExecutedEvent;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Test for {@link UserActionObserver}.
 *
 * @author A. Kunchev
 */
public class UserActionObserverTest {

	@InjectMocks
	private UserActionObserver observer;

	@Mock
	private TransitionScriptEvaluator transitionScriptEvaluator;

	@Mock
	private DefinitionService definitionService;

	@Before
	public void setup() {
		observer = new UserActionObserver();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void onAfterUserOperation_versionInstance_scriptsNotExecuted() {
		Instance target = new EmfInstance();
		target.setId("instance-id-v1.3");
		AfterOperationExecutedEvent event = new AfterOperationExecutedEvent(new Operation("operation", true), target);

		observer.onAfterUserOperation(event);
		verify(transitionScriptEvaluator, never()).executeScriptsForTransition(any(Instance.class), anyString(),
				anyBoolean(), anyMapOf(String.class, Object.class));
	}

	@Test
	public void onBeforeUserOperation_versionInstance_scriptsNotExecuted() {
		Instance target = new EmfInstance();
		target.setId("instance-id-v1.3");
		BeforeOperationExecutedEvent event = new BeforeOperationExecutedEvent(new Operation("operation", true), target);

		observer.onBeforeUserOperation(event);
		verify(transitionScriptEvaluator, never()).executeScriptsForTransition(any(Instance.class), anyString(),
				anyBoolean(), anyMapOf(String.class, Object.class));
	}
}
