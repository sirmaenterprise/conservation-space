package com.sirma.itt.seip.permissions;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.state.PrimaryStateFactory;
import com.sirma.itt.seip.instance.state.PrimaryStates;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.resources.ResourceService;

/**
 * Tests for {@link BaseRoleEvaluator}.
 *
 * @author A. Kunchev
 */
public class BaseRoleEvaluatorTest {

	@InjectMocks
	private BaseRoleEvaluator<Instance> evaluator;

	@Mock
	private ResourceService resourceService;

	@Mock
	private AuthorityService authorityService;

	@Mock
	private PermissionService permissionService;

	@Mock
	private PrimaryStateFactory stateFactory;

	@Before
	public void setup() {
		evaluator = mock(BaseRoleEvaluator.class, Mockito.CALLS_REAL_METHODS);
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void should_initializePrimaryStates_onInit() {
		evaluator.init();
		verify(stateFactory).create(PrimaryStates.DELETED_KEY);
		verify(stateFactory).create(PrimaryStates.COMPLETED_KEY);
		verify(stateFactory).create(PrimaryStates.CANCELED_KEY);
	}

}
