package com.sirmaenterprise.sep.roles.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirmaenterprise.sep.roles.ActionDefinition;
import com.sirmaenterprise.sep.roles.RoleManagement;

/**
 * Test for {@link ActionsResource}
 *
 * @author BBonev
 */
public class ActionsResourceTest {
	private static final ActionDefinition ACTION = new ActionDefinition()
			.setId("action")
				.setActionType("serverAction")
				.setEnabled(true)
				.setImmediate(true)
				.setImagePath("/images/action.jpg")
				.setVisible(true);

	private static final ActionDefinition DISABLED_ACTION = new ActionDefinition()
			.setId("disabledAction")
				.setActionType("serverDisabledAction")
				.setEnabled(false)
				.setVisible(false);

	@InjectMocks
	private ActionsResource actionsResource;
	@Mock
	private RoleManagement roleManagement;
	@Mock
	private LabelProvider labelProvider;
	@Mock
	private UserPreferences userPreferences;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		when(labelProvider.getLabel(anyString())).then(a -> a.getArgumentAt(0, String.class));
		when(roleManagement.getActions()).then(a -> Stream.of(DISABLED_ACTION, ACTION));
		when(userPreferences.getLanguage()).thenReturn("en");
	}

	@Test
	public void should_provideAllActionsSorted() throws Exception {
		List<ActionResponse> actions = actionsResource.getAllActions();
		assertNotNull(actions);
		assertEquals(2, actions.size());

		ActionResponse action = actions.get(0);
		assertEquals("action", action.getId());
		assertEquals("serverAction", action.getActionType());
		assertEquals("action.label", action.getLabel());
		assertEquals("action.tooltip", action.getTooltip());
		assertEquals("/images/action.jpg", action.getImagePath());
		assertTrue(action.isImmediate());
		assertTrue(action.isVisible());
		assertTrue(action.isEnabled());
		action = actions.get(1);
		assertEquals("disabledAction", action.getId());
		assertEquals("disabledAction.label", action.getLabel());
		assertFalse(action.isEnabled());
		assertFalse(action.isVisible());
	}
}
