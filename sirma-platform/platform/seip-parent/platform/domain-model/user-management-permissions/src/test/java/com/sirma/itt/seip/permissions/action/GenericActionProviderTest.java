package com.sirma.itt.seip.permissions.action;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.TransitionDefinition;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.TransitionDefinitionMock;

/**
 * Tests for {@link GenericActionProvider}.
 *
 * @author smustafov
 */
public class GenericActionProviderTest {

	@InjectMocks
	private GenericActionProvider provider;

	@Mock
	private LabelProvider labelProvider;

	@Mock
	private DefinitionService definitionService;

	@Before
	public void before() {
		initMocks(this);
	}

	@Test
	public void should_ReturnGenericDefinition_ForDefinitionClass() {
		assertEquals(GenericDefinition.class, provider.getDefinitionClass());
	}

	@Test
	public void should_ProvideNewActions() {
		mockDefinitions(asList(ActionTypeConstants.EDIT_DETAILS, ActionTypeConstants.LOCK));
		verifyActions(provider.initializeActions(), asList(ActionTypeConstants.EDIT_DETAILS, ActionTypeConstants.LOCK));
	}

	@Test
	public void should_ProcessChildDefinitions() {
		mockDefinitions(asList(ActionTypeConstants.EDIT_DETAILS));
		verifyActions(provider.initializeActions(), asList(ActionTypeConstants.EDIT_DETAILS));
	}

	private static void verifyActions(Map<String, Action> actions, List<String> expectedActionIds) {
		for (String actionId : expectedActionIds) {
			assertNotNull(actions.get(actionId));
		}
	}

	private void mockDefinitions(List<String> actionIds) {
		DefinitionMock definition = buildDefinition(actionIds);

		when(definitionService.getAllDefinitions(GenericDefinition.class)).thenReturn(Arrays.asList(definition));
	}

	private static DefinitionMock buildDefinition(List<String> actionIds) {
		DefinitionMock definition = new DefinitionMock();
		for (String actionId : actionIds) {
			definition.getTransitions().add(buildTransition(actionId));
		}
		return definition;
	}

	private static TransitionDefinition buildTransition(String actionId) {
		TransitionDefinitionMock transition = new TransitionDefinitionMock();
		transition.setPurpose(Action.TRANSITION);
		transition.setIdentifier(actionId);
		return transition;
	}
}