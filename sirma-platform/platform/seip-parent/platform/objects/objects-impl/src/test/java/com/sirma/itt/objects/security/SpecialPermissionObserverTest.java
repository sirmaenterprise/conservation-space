package com.sirma.itt.objects.security;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
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
import com.sirma.itt.seip.permissions.role.PermissionsChange.PermissionsChangeBuilder;
import com.sirma.itt.seip.permissions.role.TransactionalPermissionChanges;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/***
 * Testing the addition of special permissions when creating an object.
 *
 * @author nvelkov
 */
public class SpecialPermissionObserverTest {

	@InjectMocks
	private SpecialPermissionObserver specialPermissionObserver = new SpecialPermissionObserver();

	@Mock
	private SecurityContext securityContext;

	@Mock
	private ResourceService resourceService;

	@Mock
	private TransactionalPermissionChanges transactionalPermissionChanges;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);

		when(resourceService.getAllOtherUsers()).then(a -> {
			EmfUser user = new EmfUser();
			user.setId("all_other");
			return user;
		});
	}

	@Test
	public void onCreateSavedSearch_notSavedSearch() {
		Instance project = new EmfInstance();
		project.add(DefaultProperties.SEMANTIC_TYPE, EMF.PROJECT.toString());
		AfterInstancePersistEvent<Instance, TwoPhaseEvent> event = new AfterInstancePersistEvent<>(project);

		specialPermissionObserver.onCreateSavedSearch(event);

		verifyZeroInteractions(transactionalPermissionChanges);
	}

	@Test
	public void onCreateSavedSearch_notMutable() {
		mockAuthenticationServiceInstance();

		EmfInstance savedSearch = new EmfInstance();
		savedSearch.add(DefaultProperties.SEMANTIC_TYPE, EMF.SAVED_SEARCH.toString());
		InstanceReference reference = new InstanceReferenceMock("", mock(DataTypeDefinition.class), savedSearch);
		ReflectionUtils.setFieldValue(savedSearch, "reference", reference);
		AfterInstancePersistEvent<Instance, TwoPhaseEvent> event = new AfterInstancePersistEvent<>(savedSearch);

		PermissionsChangeBuilder builder = mock(PermissionsChangeBuilder.class);
		when(transactionalPermissionChanges.builder(any(InstanceReference.class))).thenReturn(builder);

		specialPermissionObserver.onCreateSavedSearch(event);

		verify(builder).addRoleAssignmentChange("current-user-id", "MANAGER");
		verify(builder).addRoleAssignmentChange("all_other", "CONSUMER");
	}

	@Test
	public void onCreateSavedSearch_mutable() {
		mockAuthenticationServiceInstance();

		EmfInstance savedSearch = new EmfInstance();
		savedSearch.add(DefaultProperties.SEMANTIC_TYPE, EMF.SAVED_SEARCH.toString());
		savedSearch.add("mutable", true);
		InstanceReference reference = new InstanceReferenceMock("", mock(DataTypeDefinition.class), savedSearch);
		ReflectionUtils.setFieldValue(savedSearch, "reference", reference);
		AfterInstancePersistEvent<Instance, TwoPhaseEvent> event = new AfterInstancePersistEvent<>(savedSearch);

		PermissionsChangeBuilder builder = mock(PermissionsChangeBuilder.class);
		when(transactionalPermissionChanges.builder(any(InstanceReference.class))).thenReturn(builder);

		specialPermissionObserver.onCreateSavedSearch(event);

		verify(builder).addRoleAssignmentChange("current-user-id", "MANAGER");
	}

	private void mockAuthenticationServiceInstance() {
		User user = new EmfUser();
		user.setId("current-user-id");
		Mockito.when(securityContext.getAuthenticated()).thenReturn(user);
	}
}
