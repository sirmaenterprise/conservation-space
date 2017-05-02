package com.sirma.itt.seip.instance.actions.evaluation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.context.InstanceContextInitializer;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.permissions.action.EmfAction;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Tests for {@link InstanceActionsList}.
 *
 * @author A. Kunchev
 */
public class InstanceActionsListTest {

	@InjectMocks
	private InstanceActionsList actions;

	@Mock
	private InstanceTypeResolver instanceTypeResolver;

	@Mock
	private AuthorityService authorityService;

	@Mock
	private InstanceContextInitializer instanceContextInitializer;

	@Mock
	private LabelProvider labelProvider;

	@Before
	public void setup() {
		actions = new InstanceActionsList();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getName() {
		assertEquals(ActionsListRequest.ACTIONS_LIST, actions.getName());
	}

	@Test(expected = BadRequestException.class)
	public void getActions_nullRequest() {
		actions.perform(null);
	}

	@Test(expected = BadRequestException.class)
	public void getActions_nullId() {
		actions.perform(buildRequest(null, "contextId", "placeholder", Arrays.asList("path1", "path2")));
	}

	@Test(expected = BadRequestException.class)
	public void getActions_emptyId() {
		actions.perform(buildRequest("", "contextId", "placeholder", Arrays.asList("path1", "path2")));
	}

	@Test(expected = BadRequestException.class)
	public void getActions_nullPlaceholder_emptySet() {
		actions.perform(buildRequest("id", "contextId", null, Arrays.asList("path1", "path2")));
	}

	@Test(expected = InstanceNotFoundException.class)
	public void getActions_nullInstance_emptySet() {
		String id = "id";
		when(instanceTypeResolver.resolveReference(id)).thenReturn(Optional.empty());
		actions.perform(buildRequest(id, "contextId", "placeholder", Arrays.asList("path1", "path2")));
	}

	@Test
	public void getActions_noActions_noPermissionAction() {
		Set<Action> result = getActionsInternal(Collections.emptySet());
		assertEquals(1, result.size());
		assertEquals(ActionTypeConstants.NO_PERMISSIONS, result.iterator().next().getActionId());
		verify(labelProvider).getValue("cmf.btn.actions.no_permissions");
	}

	@Test
	public void getActions_withActions_() {
		Set<Action> result = getActionsInternal(Collections.singleton(new EmfAction(ActionTypeConstants.CREATE)));
		assertEquals(1, result.size());
		assertEquals(ActionTypeConstants.CREATE, result.iterator().next().getActionId());
	}

	@SuppressWarnings("unchecked")
	private Set<Action> getActionsInternal(Set<Action> actionsToReturn) {
		String placeholder = "placeholder";
		String id = "id";
		Instance instance = new EmfInstance();
		instance.setId(id);
		when(instanceTypeResolver.resolveReference(id))
				.thenReturn(Optional.of(new InstanceReferenceMock(id, new DataTypeDefinitionMock(instance), instance)));
		when(instanceTypeResolver.resolveReference("contextId")).thenReturn(Optional.of(new InstanceReferenceMock()));
		when(authorityService.getAllowedActions(instance, placeholder)).thenReturn(actionsToReturn);

		return (Set<Action>) actions
				.perform(buildRequest(id, "contextId", placeholder, Arrays.asList("path1", "path2")));
	}

	private static ActionsListRequest buildRequest(String id, String contextId, String placeholder,
			List<Serializable> path) {
		ActionsListRequest request = new ActionsListRequest();
		request.setTargetId(id);
		request.setContextId(contextId);
		request.setPlaceholder(placeholder);
		request.setContextPath(path);
		return request;
	}

}
