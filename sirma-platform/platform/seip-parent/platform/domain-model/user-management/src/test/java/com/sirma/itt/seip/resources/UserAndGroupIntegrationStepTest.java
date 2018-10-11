package com.sirma.itt.seip.resources;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.TITLE;
import static com.sirma.itt.seip.resources.ResourceProperties.FIRST_NAME;
import static com.sirma.itt.seip.resources.ResourceProperties.LAST_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.testutil.fakes.InstanceTypeFake;
import com.sirma.itt.seip.testutil.mocks.InstanceContextServiceMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Test for {@link UserAndGroupIntegrationStep}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 11/09/2017
 */
public class UserAndGroupIntegrationStepTest {

	@InjectMocks
	private UserAndGroupIntegrationStep step;

	@Mock
	private ResourceService resourceService;

	@Mock
	private DatabaseIdManager idManager;

	@Spy
	private InstanceContextServiceMock contextService;

	@Before
	public void before() {
		initMocks(this);
		EmfGroup group = new EmfGroup();
		group.setId("emf:everyone");
		when(resourceService.getAllOtherUsers()).thenReturn(group);
	}

	@Test
	public void onBeforeInstanceSave_ShouldDoNothing_When_InstanceIsNotUser() {
		EmfInstance instance = createInstance(null, "document");

		step.beforeSave(InstanceSaveContext.create(instance, new Operation()));

		assertNull(instance.get(TITLE));
	}

	@Test
	public void onBeforeInstanceSave_ShouldUpdateUserTitle_When_InstanceIsUser() {
		EmfInstance instance = createInstance("emf:user", "user");
		instance.add(FIRST_NAME, "John");
		instance.add(LAST_NAME, "Doe");

		when(idManager.isPersisted(instance)).thenReturn(Boolean.TRUE);
		step.beforeSave(InstanceSaveContext.create(instance, new Operation()));

		assertEquals("John Doe", instance.get(TITLE));
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void onBeforeInstanceSave_shouldFailIfUserInstanceHasAParent() {
		EmfInstance instance = createInstance("emf:user", "user");
		InstanceReferenceMock context = InstanceReferenceMock.createGeneric("emf:someContext");
		contextService.bindContext(instance, context);
		instance.add(FIRST_NAME, "John");

		step.beforeSave(InstanceSaveContext.create(instance, new Operation()));
	}

	@Test
	public void onBeforeInstanceSave_ShouldChangeInstanceId_When_InstanceIsNewUser() {
		EmfInstance instance = createInstance("emf:user", "user");
		instance.add(FIRST_NAME, "John");
		instance.add(LAST_NAME, "Doe");

		EmfUser user = new EmfUser("user@tenant.com");
		user.setId("emf:user-tenant.com");
		when(resourceService.buildUser(instance)).thenReturn(user);
		when(idManager.isPersisted(instance)).thenReturn(Boolean.FALSE);

		step.beforeSave(InstanceSaveContext.create(instance, new Operation()));

		verify(idManager).unregister(instance);
		verify(idManager).register(instance);
		assertEquals("emf:user-tenant.com", instance.getId());
	}

	@Test
	public void onBeforeInstanceSave_shouldAddEveryoneAsFutureGroupParticipant() {
		EmfInstance instance = createInstance("emf:user", "user");
		instance.add(FIRST_NAME, "John");
		instance.add(LAST_NAME, "Doe");

		EmfUser user = new EmfUser("user@tenant.com");
		user.setId("emf:user-tenant.com");
		when(resourceService.buildUser(instance)).thenReturn(user);
		when(idManager.isPersisted(instance)).thenReturn(Boolean.FALSE);

		step.beforeSave(InstanceSaveContext.create(instance, new Operation()));

		Collection<Serializable> members = instance.getAsCollection(ResourceProperties.IS_MEMBER_OF,
				Collections::emptyList);

		assertTrue(members.contains("emf:everyone"));
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void onBeforeInstanceSave_ShouldFailIfTryingToCreateUserWithSameName() {
		EmfInstance instance = createInstance("emf:john.doe", "user");
		instance.add(ResourceProperties.USER_ID, "john.doe");
		instance.add(FIRST_NAME, "John");
		instance.add(LAST_NAME, "Doe");

		EmfUser user = new EmfUser("john.doe@tenant.com");
		user.setId("emf:john.doe-tenant.com");
		when(resourceService.buildUser(instance)).thenReturn(user);
		when(resourceService.resourceExists("john.doe@tenant.com")).thenReturn(Boolean.TRUE);
		when(idManager.isPersisted(instance)).thenReturn(Boolean.FALSE);

		step.beforeSave(InstanceSaveContext.create(instance, new Operation()));
	}

	private static EmfInstance createInstance(String id, String category) {
		EmfInstance instance = new EmfInstance();
		instance.setId(id);
		instance.setType(InstanceTypeFake.buildForCategory(category));
		return instance;
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void onBeforeInstanceSave_shouldFailIfGroupInstanceHasAParent() {
		EmfInstance instance = createInstance("emf:some-group", "group");
		InstanceReferenceMock context = InstanceReferenceMock.createGeneric("emf:someContext");
		contextService.bindContext(instance, context);

		step.beforeSave(InstanceSaveContext.create(instance, new Operation()));
	}

	@Test
	public void onBeforeInstanceSave_ShouldChangeInstanceId_When_InstanceIsNewGroup() {
		EmfInstance instance = createInstance("emf:some-group", "group");
		instance.getOrCreateProperties();

		EmfGroup group = new EmfGroup("some-group", "some-group");
		group.setId("emf:some-group");
		when(resourceService.buildGroup(instance)).thenReturn(group);
		when(idManager.isPersisted(instance)).thenReturn(Boolean.FALSE);

		step.beforeSave(InstanceSaveContext.create(instance, new Operation()));

		verify(idManager).unregister(instance);
		verify(idManager).register(instance);
		assertEquals("emf:some-group", instance.getId());
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void onBeforeInstanceSave_ShouldFailIfTryingToCreateGroupWithSameName() {
		EmfInstance instance = createInstance("emf:some-group", "group");
		instance.add(ResourceProperties.GROUP_ID, "some-group");

		EmfGroup group = new EmfGroup("some-group", "some-group");
		group.setId("emf:some-group");
		when(resourceService.buildGroup(instance)).thenReturn(group);
		when(resourceService.resourceExists("some-group")).thenReturn(Boolean.TRUE);
		when(idManager.isPersisted(instance)).thenReturn(Boolean.FALSE);

		step.beforeSave(InstanceSaveContext.create(instance, new Operation()));
	}

	@Test
	public void onBeforeInstanceSave_ShouldSetGroupTitle_When_ItsMissing() {
		EmfInstance instance = createInstance("emf:some-group", "group");
		instance.add(ResourceProperties.GROUP_ID, "some-group");

		EmfGroup group = new EmfGroup("some-group", "some-group");
		group.setId("emf:some-group");
		when(resourceService.buildGroup(instance)).thenReturn(group);
		when(idManager.isPersisted(instance)).thenReturn(Boolean.TRUE);

		step.beforeSave(InstanceSaveContext.create(instance, new Operation()));

		assertEquals("some-group", instance.getString(DefaultProperties.TITLE));
	}

	@Test
	public void onBeforeInstanceSave_ShouldNotOverrideGroupTitle_When_ItHasValue() {
		EmfInstance instance = createInstance("emf:some-group", "group");
		instance.add(ResourceProperties.GROUP_ID, "some-group");
		instance.add(DefaultProperties.TITLE, "Company Group");

		EmfGroup group = new EmfGroup("some-group", "some-group");
		group.setId("emf:some-group");
		when(resourceService.buildGroup(instance)).thenReturn(group);
		when(idManager.isPersisted(instance)).thenReturn(Boolean.TRUE);

		step.beforeSave(InstanceSaveContext.create(instance, new Operation()));

		assertEquals("Company Group", instance.getString(DefaultProperties.TITLE));
	}

	@Test
	public void getName() {
		assertEquals("userAndGroupIntegration", step.getName());
	}
}
