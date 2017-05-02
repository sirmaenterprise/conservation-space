package com.sirma.itt.objects.security;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.objects.security.observers.SpecialPermissionObserver;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.event.TwoPhaseEvent;
import com.sirma.itt.seip.instance.event.AfterInstancePersistEvent;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.permissions.SecurityModel;
import com.sirma.itt.seip.permissions.role.PermissionsChange;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/***
 * Testing the addition of special permissions when creating an object.
 *
 * @author nvelkov
 */
public class SpecialPermissionObserverTest {

	@Mock
	private PermissionService permissionService;

	@Mock
	private SecurityContext securityContext;

	@Mock
	private ResourceService resourceService;

	@InjectMocks
	private SpecialPermissionObserver specialPermissionObserver = new SpecialPermissionObserver();

	@Captor
	private ArgumentCaptor<InstanceReference> instanceCaptor;

	@Captor
	private ArgumentCaptor<List<PermissionsChange>> changesCaptor;

	private static final String ALL_OTHER_USERS = "all_other";

	/**
	 * Init the mock annotations.
	 */
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);

		when(resourceService.getAllOtherUsers()).then(a -> {
			EmfUser user = new EmfUser();
			user.setId(ALL_OTHER_USERS);
			return user;
		});
	}

	/**
	 * Test the addition of permissions when creating a saved search.
	 */
	@Test
	public void testSavedSearchPermissions() {
		mockAuthenticationServiceInstance();

		EmfInstance savedSearch = new EmfInstance();
		savedSearch.add(DefaultProperties.SEMANTIC_TYPE, EMF.SAVED_SEARCH.toString());
		InstanceReference reference = new InstanceReferenceMock("", mock(DataTypeDefinition.class), savedSearch);
		savedSearch.setReference(reference);
		AfterInstancePersistEvent<Instance, TwoPhaseEvent> event = new AfterInstancePersistEvent<>(savedSearch);

		specialPermissionObserver.onCreateSavedSearch(event);
		Mockito.verify(permissionService).setPermissions(instanceCaptor.capture(), changesCaptor.capture());

		assertEquals(reference, instanceCaptor.getValue());

		List<PermissionsChange> changes = changesCaptor.getValue();

		assertEquals(2, changes.size());

		assertEquals(((PermissionsChange.AddRoleAssignmentChange) changes.get(0)).getRole(),
				SecurityModel.BaseRoles.MANAGER.getIdentifier());

		assertEquals(((PermissionsChange.AddRoleAssignmentChange) changes.get(1)).getRole(),
				SecurityModel.BaseRoles.CONSUMER.getIdentifier());
	}

	/**
	 * Mock authentication service.
	 */
	private void mockAuthenticationServiceInstance() {
		User user = new EmfUser();
		user.setId("id");
		Mockito.when(securityContext.getAuthenticated()).thenReturn(user);
	}
}
