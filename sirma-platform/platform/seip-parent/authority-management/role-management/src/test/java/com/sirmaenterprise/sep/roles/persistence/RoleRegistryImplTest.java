package com.sirmaenterprise.sep.roles.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.permissions.action.EmfAction;
import com.sirma.itt.seip.permissions.model.RoleId;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;
import com.sirma.itt.seip.testutil.fakes.EntityLookupCacheContextFake;
import com.sirmaenterprise.sep.roles.ActionDefinition;
import com.sirmaenterprise.sep.roles.RoleActionModel;
import com.sirmaenterprise.sep.roles.RoleDefinition;
import com.sirmaenterprise.sep.roles.RoleManagement;

/**
 * Test for {@link RoleRegistryImpl}
 *
 * @since 2017-03-28
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 */
public class RoleRegistryImplTest {

	private static final Action READ_ACTION = createReadAction();

	private static final ActionDefinition ACTION1 = new ActionDefinition().setId("action1").setEnabled(true);
	private static final ActionDefinition ACTION2 = new ActionDefinition().setId("action2");

	private static final RoleDefinition ROLE1 = new RoleDefinition().setId("role1").setEnabled(true);
	private static final RoleDefinition ROLE2 = new RoleDefinition().setId("role2").setEnabled(false);
	private static final RoleDefinition ROLE_WITH_READ_RIGHTS = new RoleDefinition().setId("canReadRole")
			.setEnabled(true).setCanRead(true);

	@InjectMocks
	private RoleRegistryImpl roleRegistry;

	@Mock
	private RoleManagement roleManagement;
	@Mock
	private LabelProvider labelProvider;
	@Spy
	private EntityLookupCacheContext cacheContext = EntityLookupCacheContextFake.createNoCache();

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(labelProvider.getLabel(anyString())).then(a -> a.getArgumentAt(0, String.class));

		when(roleManagement.getRoles()).then(a -> Stream.of(ROLE1, ROLE2, ROLE_WITH_READ_RIGHTS));
		when(roleManagement.getRole("role1"))
				.thenReturn(Optional.of(new RoleDefinition().setId("role1").setEnabled(true)));
		when(roleManagement.getRole("role2"))
				.thenReturn(Optional.of(new RoleDefinition().setId("role2").setEnabled(false)));
		when(roleManagement.getRoleActionModel()).thenReturn(new RoleActionModel()
				.add(ACTION1)
					.add(ACTION2)
					.add(ROLE1)
					.add(ROLE1)
					.add(ROLE_WITH_READ_RIGHTS)
					.add("role1", "action1", true, null)
					.add("role1", "action2", true, null)
					.add("role2", "action1", false, null)
					.add("role2", "action2", false, null));
		when(roleManagement.getRole("role3")).thenReturn(Optional.empty());
		when(roleManagement.getRole(ROLE_WITH_READ_RIGHTS.getIdentifier()))
				.thenReturn(Optional.of(ROLE_WITH_READ_RIGHTS));
	}

	@Test
	public void should_provideActiveolesKeys() throws Exception {
		Set<RoleIdentifier> keys = roleRegistry.getKeys();
		assertEquals(2, keys.size());
		assertTrue(keys.contains(new RoleId("role1", 1)));
		assertTrue(keys.contains(new RoleId(ROLE_WITH_READ_RIGHTS.getIdentifier(), 1)));
	}

	@Test
	public void should_returnActiveRoles() throws Exception {
		assertNotNull(roleRegistry.find(new RoleId("role1", 1)));
	}

	@Test
	public void shouldNot_returnInactiveRoles() throws Exception {
		assertNull(roleRegistry.find(new RoleId("role2", 2)));
	}

	@Test
	public void shouldNot_returnUnkownRoles() throws Exception {
		assertNull(roleRegistry.find(new RoleId("role3", 2)));
	}

	@Test
	public void should_resetCache_onReload() throws Exception {
		roleRegistry.reload();
	}

	@Test
	public void should_addReadActionToMapping_whenRoleHasReadRights() {
		Role roleWithNoReadRights = roleRegistry.find(new RoleId(ROLE1.getIdentifier(), 1));
		Role roleWithReadRights = roleRegistry.find(new RoleId(ROLE_WITH_READ_RIGHTS.getIdentifier(), 1));

		assertFalse(roleWithNoReadRights.getAllAllowedActions().contains(READ_ACTION));
		assertTrue(roleWithReadRights.getAllAllowedActions().contains(READ_ACTION));
	}

	private static Action createReadAction() {
		EmfAction action = new EmfAction(ActionTypeConstants.READ);
		action.setFilters(Collections.emptyList());
		return action;
	}

}
