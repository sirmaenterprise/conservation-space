package com.sirma.itt.seip.permissions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.permissions.role.ResourceRole;
import com.sirma.itt.seip.permissions.role.RoleAssignments;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.User;

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

	@Before
	public void setup() {
		evaluator = Mockito.mock(BaseRoleEvaluator.class, Mockito.CALLS_REAL_METHODS);
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void hasUnlockPermissions_lockedByUser() {
		Instance instance = new EmfInstance();
		instance.add(DefaultProperties.LOCKED_BY, "Batman");
		User user = new EmfUser();
		Mockito.when(resourceService.areEqual("Batman", user)).thenReturn(true);
		assertTrue(evaluator.hasUnlockPermissions(instance, user));
	}

	@Test
	public void hasUnlockPermissions_createdByUser() {
		EmfInstance instance = new EmfInstance();
		instance.setReference(mock(InstanceReference.class));
		instance.add(DefaultProperties.CREATED_BY, "Batman");
		User user = new EmfUser();
		Mockito.when(resourceService.areEqual("Batman", user)).thenReturn(true);
		assertTrue(evaluator.hasUnlockPermissions(instance, user));
	}

	@Test
	public void hasUnlockPermissions_adminUser() {
		Instance instance = new EmfInstance();
		User user = new EmfUser();
		Mockito.when(authorityService.isAdminOrSystemUser(user)).thenReturn(true);
		assertTrue(evaluator.hasUnlockPermissions(instance, user));
	}

	@Test
	public void hasUnlockPermissions_managerUser() {
		EmfInstance instance = new EmfInstance();
		instance.setReference(mock(InstanceReference.class));

		final String USERNAME = "admin";
		EmfUser user = new EmfUser();
		user.setId(USERNAME);
		Mockito.when(permissionService.getPermissionAssignments(instance.toReference())).thenAnswer((a) -> {
			Map<String, ResourceRole> assignments = new HashMap<>();
			ResourceRole resourceRole = new ResourceRole();
			resourceRole.setRoleAssignments(new RoleAssignments("MANAGER"));
			resourceRole.getRoleAssignments().addAssignment("MANAGER", PermissionModelType.SPECIAL);
			assignments.put(USERNAME, resourceRole);
			return assignments;
		});
		assertTrue(evaluator.hasUnlockPermissions(instance, user));
	}

	@Test
	public void hasUnlockPermissions_noPermissions() {
		EmfInstance instance = new EmfInstance();
		instance.setReference(mock(InstanceReference.class));
		instance.add(DefaultProperties.CREATED_BY, "Batman");
		instance.add(DefaultProperties.LOCKED_BY, "Batman");
		User user = new EmfUser();
		Mockito.when(resourceService.areEqual("Batman", user)).thenReturn(false);
		Mockito.when(authorityService.isAdminOrSystemUser(user)).thenReturn(false);
		Mockito.when(permissionService.getPermissionAssignments(instance.toReference())).thenReturn(
				Collections.emptyMap());
		assertFalse(evaluator.hasUnlockPermissions(instance, user));
	}

}
