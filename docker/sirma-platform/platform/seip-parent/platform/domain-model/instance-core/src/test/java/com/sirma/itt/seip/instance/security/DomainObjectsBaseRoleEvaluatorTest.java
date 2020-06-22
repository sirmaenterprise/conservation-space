package com.sirma.itt.seip.instance.security;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.state.PrimaryStateFactory;
import com.sirma.itt.seip.instance.state.PrimaryStates;
import com.sirma.itt.seip.instance.state.StateService;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.permissions.SecurityModel.BaseRoles;
import com.sirma.itt.seip.permissions.role.ResourceRole;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleEvaluator;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;
import com.sirma.itt.seip.permissions.role.RoleRegistry;
import com.sirma.itt.seip.resources.EmfUser;

/**
 * Test for {@link DomainObjectsBaseRoleEvaluator}.
 *
 * @author A. Kunchev
 */
public class DomainObjectsBaseRoleEvaluatorTest {

	@InjectMocks
	private DomainObjectsBaseRoleEvaluator<Instance> evaluator;

	@Spy
	private PrimaryStateFactory primaryStateFactory = new PrimaryStateFactory();

	@Mock
	private StateService stateService;

	@Mock
	private RoleRegistry roleRegistry;

	@Mock
	private PermissionService permissionService;

	@Mock
	private InstanceService instanceService;

	@Before
	public void setup() {
		evaluator = mock(DomainObjectsBaseRoleEvaluator.class, CALLS_REAL_METHODS);
		MockitoAnnotations.initMocks(this);
		evaluator.init();
		when(roleRegistry.find(any(RoleIdentifier.class)))
				.thenAnswer(a -> new Role(a.getArgumentAt(0, RoleIdentifier.class)));
		TypeConverterUtil.setTypeConverter(mock(TypeConverter.class));
	}

	@Test
	public void evaluateInternal_targetDeleted_viewerRole() {
		Instance target = new EmfInstance();
		target.addIfNotPresent(DefaultProperties.IS_DELETED, true);
		Pair<Role, RoleEvaluator<Instance>> result = evaluator.evaluateInternal(target, null, null);
		assertEquals(BaseRoles.VIEWER, result.getFirst().getRoleId());
	}

	@Test
	public void evaluateInternal_noPermissions() {
		Instance target = new EmfInstance("instance-id");
		target.addIfNotPresent(DefaultProperties.IS_DELETED, false);
		ResourceRole role = new ResourceRole();
		role.setRole(BaseRoles.NO_PERMISSION);
		when(permissionService.getPermissionAssignment(any(InstanceReference.class), any())).thenReturn(role);
		Pair<Role, RoleEvaluator<Instance>> result = evaluator.evaluateInternal(target, new EmfUser(), null);
		assertEquals(BaseRoles.NO_PERMISSION, result.getFirst().getRoleId());
	}

	@Test
	public void evaluateInternal_versionInstance_managerPermissions() {
		Instance target = new EmfInstance("instance-id-v1.5");
		target.addIfNotPresent(DefaultProperties.IS_DELETED, false);
		ResourceRole role = new ResourceRole();
		role.setRole(BaseRoles.MANAGER);
		when(permissionService.getPermissionAssignment(any(InstanceReference.class), any())).thenReturn(role);
		when(instanceService.loadDeleted(eq("instance-id"))).thenReturn(Optional.of(new EmfInstance()));
		Pair<Role, RoleEvaluator<Instance>> result = evaluator.evaluateInternal(target, new EmfUser(), null);
		assertEquals(BaseRoles.MANAGER, result.getFirst().getRoleId());
	}

	@Test(expected = EmfRuntimeException.class)
	public void evaluateInternal_versionInstanceNotFound_managerPermissions() {
		Instance target = new EmfInstance("instance-id-v5.0");
		target.addIfNotPresent(DefaultProperties.IS_DELETED, false);
		ResourceRole role = new ResourceRole();
		role.setRole(BaseRoles.MANAGER);
		when(permissionService.getPermissionAssignment(any(InstanceReference.class), any())).thenReturn(role);
		when(instanceService.loadDeleted(eq("instance-id"))).thenReturn(Optional.empty());
		evaluator.evaluateInternal(target, new EmfUser(), null);
	}
}
