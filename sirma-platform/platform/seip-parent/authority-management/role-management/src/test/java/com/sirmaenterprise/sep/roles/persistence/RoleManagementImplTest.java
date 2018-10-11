package com.sirmaenterprise.sep.roles.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirmaenterprise.sep.roles.ActionDefinition;
import com.sirmaenterprise.sep.roles.RoleActionChanges;
import com.sirmaenterprise.sep.roles.RoleActionModel;
import com.sirmaenterprise.sep.roles.RoleActionModel.RoleActionMapping;
import com.sirmaenterprise.sep.roles.RoleDefinition;

/**
 * Test for {@link RoleManagementImpl}
 *
 * @since 2017-03-29
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 */
public class RoleManagementImplTest {

	@InjectMocks
	private RoleManagementImpl roleManagement;
	@Mock
	private RoleActionsDao dao;
	@Mock
	private EventService eventService;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(dao.save(any())).then(a -> a.getArgumentAt(0, Entity.class));
		when(dao.saveNew(any())).then(a -> a.getArgumentAt(0, Entity.class));
		when(dao.getRoles(anyCollection())).then(a -> {
			Collection<String> ids = a.getArgumentAt(0, Collection.class);
			return ids.stream().map(id -> buildRole(id)).collect(Collectors.toList());
		});
		when(dao.getActions(anyCollection())).then(a -> {
			Collection<String> ids = a.getArgumentAt(0, Collection.class);
			return ids.stream().map(id -> buildAction(id)).collect(Collectors.toList());
		});
	}

	@Test
	public void getRoles_should_StreamAllRoleDefinitions() throws Exception {
		when(dao.getRoles()).thenReturn(Arrays.asList(buildRole("role1"), buildRole("role2")));
		Stream<RoleDefinition> roles = roleManagement.getRoles();
		assertNotNull(roles);
		long count = roles.filter(Objects::nonNull).peek(role -> {
			assertTrue(role.getId().startsWith("role"));
			assertTrue(role.getIdentifier().startsWith("role"));
			assertEquals(1, role.getGlobalPriority());
			assertEquals(1, role.getOrder());
			assertTrue(role.canRead());
			assertFalse(role.canWrite());
			assertTrue(role.isEnabled());
			assertFalse(role.isInternal());
			assertFalse(role.isUserDefined());
		}).count();

		assertEquals(2L, count);
	}

	@Test
	public void getRole_should_ReturnTheRequestedRoleDefinition() throws Exception {
		Optional<RoleDefinition> aRole = roleManagement.getRole("roleId");
		assertNotNull(aRole);
		assertTrue(aRole.isPresent());
		aRole.ifPresent(role -> {
			assertEquals("roleId", role.getId());
			assertEquals("roleId", role.getIdentifier());
			assertEquals(1, role.getGlobalPriority());
			assertEquals(1, role.getOrder());
			assertTrue(role.canRead());
			assertFalse(role.canWrite());
			assertTrue(role.isEnabled());
			assertFalse(role.isInternal());
			assertFalse(role.isUserDefined());
		});
	}

	@Test
	public void getRole_should_returnEmptyOptional_ForNullId() throws Exception {
		Optional<RoleDefinition> aRole = roleManagement.getRole(null);
		assertNotNull(aRole);
		assertFalse(aRole.isPresent());
		aRole = roleManagement.getRole("");
		assertNotNull(aRole);
		assertFalse(aRole.isPresent());
	}

	@Test
	public void getActions_should_StreamAllActionDefinitions() throws Exception {
		when(dao.getActions()).thenReturn(Arrays.asList(buildAction("action1"), buildAction("action2")));
		Stream<ActionDefinition> actions = roleManagement.getActions();
		assertNotNull(actions);
		long count = actions.filter(Objects::nonNull).peek(action -> {
			assertTrue(action.getId().startsWith("action"));
			assertTrue(action.getActionType().startsWith("action"));
			assertTrue(action.getImagePath().startsWith("/images/action"));
			assertTrue(action.isVisible());
			assertTrue(action.isEnabled());
			assertFalse(action.isImmediate());
			assertFalse(action.isUserDefined());
		}).count();

		assertEquals(2L, count);
	}

	@Test
	public void getAction_should_ReturnTheRequestedActionDefinition() throws Exception {
		Optional<ActionDefinition> anAction = roleManagement.getAction("action");
		assertNotNull(anAction);
		assertTrue(anAction.isPresent());
		anAction.ifPresent(action -> {
			assertTrue(action.getId().equals("action"));
			assertTrue(action.getActionType().startsWith("action"));
			assertTrue(action.getImagePath().equals("/images/action"));
			assertTrue(action.isVisible());
			assertTrue(action.isEnabled());
			assertFalse(action.isImmediate());
			assertFalse(action.isUserDefined());
		});
	}

	@Test
	public void getAction_should_ReturnEmptyOptionalForNullId() throws Exception {
		Optional<ActionDefinition> anAction = roleManagement.getAction(null);
		assertNotNull(anAction);
		assertFalse(anAction.isPresent());
		anAction = roleManagement.getAction("");
		assertNotNull(anAction);
		assertFalse(anAction.isPresent());
	}

	@Test
	public void getRoleActionModel_should_ReturnAllMappings() throws Exception {
		when(dao.getRoles()).thenReturn(Arrays.asList(buildRole("role1"), buildRole("role2")));
		when(dao.getActions()).thenReturn(Arrays.asList(buildAction("action1"), buildAction("action2")));
		when(dao.getRoleActions()).thenReturn(Arrays.asList(buildRoleAction("role1", "action1"),
				buildRoleAction("role1", "action2"), buildRoleAction("role2", "action1", "filter1", "filter2")));

		RoleActionModel mappings = roleManagement.getRoleActionModel();
		assertNotNull(mappings);
		Stream<RoleActionMapping> stream = mappings.roleActions();
		assertNotNull(stream);
		List<RoleActionMapping> entries = stream.collect(Collectors.toList());
		assertEquals(3, entries.size());

		RoleActionMapping entry = entries.get(0);
		assertEquals("role1", entry.getRole().getId());
		assertEquals("action1", entry.getAction().getId());
		assertTrue(entry.getFilters().isEmpty());
		entry = entries.get(2);
		assertEquals("role2", entry.getRole().getId());
		assertEquals("action1", entry.getAction().getId());
		assertFalse(entry.getFilters().isEmpty());

		assertEquals(2L, mappings.actions().count());
		assertEquals(2L, mappings.roles().count());
	}

	@Test
	public void saveActions_Should_DoNothing_OnEmptyRequest() throws Exception {
		roleManagement.saveActions(null);
		roleManagement.saveActions(Collections.emptyList());
		verify(eventService, never()).fire(any());
	}

	@Test
	public void saveActions_Should_UpdateExsitingEntries() throws Exception {
		roleManagement.saveActions(Arrays.asList(new ActionDefinition()
				.setId("action")
					.setEnabled(true)
					.setImmediate(true)
					.setActionType("serverAction")));

		verify(dao).save(any());
		verify(eventService).fire(any());
	}

	@Test
	public void saveActions_Should_CreateAction() throws Exception {
		reset(dao);
		roleManagement.saveActions(Arrays.asList(new ActionDefinition()
				.setId("action")
					.setEnabled(true)
					.setImmediate(true)
					.setActionType("serverAction")));

		verify(dao).saveNew(any());
		verify(eventService).fire(any());
	}

	@Test
	public void saveRoles_Should_DoNothing_OnEmptyRequest() throws Exception {
		roleManagement.saveRoles(null);
		roleManagement.saveRoles(Collections.emptyList());
		verify(eventService, never()).fire(any());
	}

	@Test
	public void saveRoles_Should_UpdateExsitingEntries() throws Exception {
		roleManagement.saveRoles(Arrays.asList(
				new RoleDefinition().setId("role").setEnabled(true).setCanWrite(true).setCanRead(true).setOrder(5)));

		verify(dao).save(argThat(CustomMatcher.of((RoleEntity entity) -> {
			assertTrue(entity.isCanRead());
			assertTrue(entity.isCanWrite());
			assertTrue(entity.isEnabled());
			assertEquals(5, entity.getOrder());
			assertEquals("role", entity.getId());
		})));
		verify(eventService).fire(any());
	}

	@Test
	public void saveRoles_Should_CreateRole() throws Exception {
		reset(dao);
		roleManagement.saveRoles(Arrays.asList(
				new RoleDefinition().setId("role").setEnabled(true).setCanWrite(true).setCanRead(true).setOrder(5)));

		verify(dao).saveNew(argThat(CustomMatcher.of((RoleEntity entity) -> {
			assertTrue(entity.isCanRead());
			assertTrue(entity.isCanWrite());
			assertTrue(entity.isEnabled());
			assertEquals(5, entity.getOrder());
			assertEquals("role", entity.getId());
		})));
		verify(eventService).fire(any());
	}

	@Test
	public void updateRoleActionMappings_should_DoNothingOnEmptyRequest() throws Exception {
		roleManagement.updateRoleActionMappings(null);
		roleManagement.updateRoleActionMappings(new RoleActionChanges());

		verify(eventService, never()).fire(any());
	}

	@Test
	public void updateRoleActionMappings_should_persistNewEntries() throws Exception {
		roleManagement.updateRoleActionMappings(
				new RoleActionChanges().enable("role1", "action1", Arrays.asList("filter1", "filter2")));

		verify(dao).save(argThat(CustomMatcher.of((RoleActionEntity entity) -> {
			assertEquals("role1", entity.getId().getRole());
			assertEquals("action1", entity.getId().getAction());
			assertTrue(entity.isEnabled());
			assertFalse(entity.getFilters().isEmpty());
		})));
		verify(eventService).fire(any());
	}

	@Test
	public void updateRoleActionMappings_should_UpdateExsitingEntries() throws Exception {
		when(dao.getRoleActions()).thenReturn(Arrays.asList(buildRoleAction("role1", "action1")));
		roleManagement.updateRoleActionMappings(
				new RoleActionChanges().disable("role1", "action1", Arrays.asList("filter1", "filter2")));

		verify(dao).save(argThat(CustomMatcher.of((RoleActionEntity entity) -> {
			assertEquals("role1", entity.getId().getRole());
			assertEquals("action1", entity.getId().getAction());
			assertFalse(entity.isEnabled());
			assertFalse(entity.getFilters().isEmpty());
		})));
		verify(eventService).fire(any());
	}

	@Test
	public void deleteRoleActionMapping_Should_CallCorrectDaoMethod() {
		roleManagement.deleteRoleActionMappings();
		verify(dao).deleteRoleActionMappings();
	}

	private static RoleEntity buildRole(String id) {
		RoleEntity entity = new RoleEntity();
		entity.setId(id);
		entity.setOrder(1);
		entity.setCanRead(true);
		entity.setEnabled(true);
		return entity;
	}

	private static ActionEntity buildAction(String id) {
		ActionEntity entity = new ActionEntity();
		entity.setId(id);
		entity.setActionType(id + "Type");
		entity.setImagePath("/images/" + id);
		entity.setVisible(true);
		entity.setEnabled(true);
		return entity;
	}

	private static RoleActionEntity buildRoleAction(String role, String action, String... filters) {
		RoleActionEntity entity = new RoleActionEntity(role, action);
		entity.setEnabled(true);
		if (filters != null) {
			entity.setFilters(new HashSet<>(Arrays.asList(filters)));
		}
		return entity;
	}
}
