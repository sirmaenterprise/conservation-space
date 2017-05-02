package com.sirma.itt.seip.instance.revision.steps;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.revision.PublishInstanceRequest;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.permissions.InstancePermissionsHierarchyResolver;
import com.sirma.itt.seip.permissions.PermissionModelType;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.permissions.SecurityModel;
import com.sirma.itt.seip.permissions.role.PermissionsChange;
import com.sirma.itt.seip.permissions.role.ResourceRole;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Test for {@link CopyPermissionsPublishStep}
 *
 * @author BBonev
 */
public class CopyPermissionsPublishStepTest {

	@InjectMocks
	private CopyPermissionsPublishStep step;

	@Mock
	private PermissionService permissionService;

	@Mock
	private InstancePermissionsHierarchyResolver hierarchyResolver;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		Map<String, ResourceRole> assignments = new HashMap<>();
		assignments.put("emf:admin", createRole(SecurityModel.BaseRoles.ADMINISTRATOR));
		assignments.put("emf:user1", createRole(SecurityModel.BaseRoles.CONSUMER));
		assignments.put("emf:user2", createRole(SecurityModel.BaseRoles.COLLABORATOR));

		when(permissionService.getPermissionAssignments(any())).thenReturn(assignments);
		when(hierarchyResolver.getLibrary(Matchers.any(InstanceReference.class))).thenReturn(InstanceReferenceMock.createGeneric("library"));
	}

	@Test
	public void shouldCopyAllAssigments() throws Exception {
		Instance instance = InstanceReferenceMock.createGeneric("emf:instance").toInstance();
		Instance revision = InstanceReferenceMock.createGeneric("emf:instance-r1.0").toInstance();

		when(permissionService.getPermissionModel(any())).thenReturn(new PermissionModelType(false, false, false));

		PublishContext publishContext = new PublishContext(
				new PublishInstanceRequest(instance, new Operation(), null, null), revision);
		step.execute(publishContext);

		verify(permissionService).setPermissions(eq(revision.toReference()),
				argThat(CustomMatcher.of((List<PermissionsChange> list) -> list.size() == 4)));
	}

	@Test
	public void shouldCopySetLibraryInheritence() throws Exception {
		Instance instance = InstanceReferenceMock.createGeneric("emf:instance").toInstance();
		Instance revision = InstanceReferenceMock.createGeneric("emf:instance-r1.0").toInstance();

		when(permissionService.getPermissionModel(any())).thenReturn(new PermissionModelType(false, true, false));

		PublishContext publishContext = new PublishContext(
				new PublishInstanceRequest(instance, new Operation(), null, null), revision);
		step.execute(publishContext);

		verify(permissionService).setPermissions(eq(revision.toReference()),
				argThat(CustomMatcher.of((List<PermissionsChange> list) -> list.size() == 6)));
	}

	@Test
	public void shouldCopySetParentInheritence() throws Exception {
		Instance instance = InstanceReferenceMock.createGeneric("emf:instance").toInstance();
		Instance revision = InstanceReferenceMock.createGeneric("emf:instance-r1.0").toInstance();

		when(permissionService.getPermissionModel(any())).thenReturn(new PermissionModelType(true, false, false));

		PublishContext publishContext = new PublishContext(
				new PublishInstanceRequest(instance, new Operation(), null, null), revision);
		step.execute(publishContext);

		verify(permissionService).setPermissions(eq(revision.toReference()),
				argThat(CustomMatcher.of((List<PermissionsChange> list) -> list.size() == 6)));
	}

	private static ResourceRole createRole(RoleIdentifier roleIdentifier) {
		ResourceRole role = new ResourceRole();
		role.setRole(roleIdentifier);
		return role;
	}
}
