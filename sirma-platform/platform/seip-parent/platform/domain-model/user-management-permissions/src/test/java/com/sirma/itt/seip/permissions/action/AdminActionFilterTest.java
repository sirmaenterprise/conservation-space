package com.sirma.itt.seip.permissions.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceProperties;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 05/10/2017
 */
public class AdminActionFilterTest {

	@InjectMocks
	private AdminActionFilter filter;

	@Mock
	private SecurityContextManager securityContextManager;
	@Mock
	private SecurityConfiguration securityConfiguration;
	@Mock
	private ResourceService resourceService;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		EmfGroup group = new EmfGroup("everyone", "everyone");
		group.setId("emf:everyone");
		when(resourceService.getAllOtherUsers()).thenReturn(group);

		User system = new EmfUser("system");
		system.setId("emf:system");
		when(securityContextManager.getSystemUser()).thenReturn(system);

		User admin = new EmfUser("admin");
		admin.setId("emf:admin");
		when(securityContextManager.getAdminUser()).thenReturn(admin);
	}

	@Test
	public void removeAdminDisableAction() throws Exception {
		User admin = new EmfUser("admin");
		admin.setId("emf:admin");
		when(securityContextManager.getAdminUser()).thenReturn(admin);

		Instance instance = new EmfInstance("emf:admin");
		ClassInstance type = new ClassInstance();
		type.setCategory(ObjectTypes.USER);
		instance.setType(type);

		Set<Action> actions = new HashSet<>();
		actions.add(new EmfAction("someAction"));
		actions.add(new EmfAction(ActionTypeConstants.DEACTIVATE));

		filter.removeAdminDisableAction(new ActionEvaluatedEvent(instance, actions, "" ));

		assertEquals(1, actions.size());
		assertFalse(actions.contains(new EmfAction(ActionTypeConstants.DEACTIVATE)));
	}

	@Test
	public void removeAdminDisableAction_shouldDoNothingIfNotUser() throws Exception {
		Instance instance = new EmfInstance("emf:admin");
		ClassInstance type = new ClassInstance();
		type.setCategory(ObjectTypes.CASE);
		instance.setType(type);

		Set<Action> actions = new HashSet<>();
		actions.add(new EmfAction("someAction"));
		actions.add(new EmfAction(ActionTypeConstants.DEACTIVATE));

		filter.removeAdminDisableAction(new ActionEvaluatedEvent(instance, actions, "" ));

		assertEquals(2, actions.size());
		assertTrue(actions.contains(new EmfAction(ActionTypeConstants.DEACTIVATE)));
	}

	@Test
	public void removeAdminDisableAction_shouldDoNothingIfNotAdmin() throws Exception {
		Instance instance = new EmfInstance("emf:nonAdmin");
		ClassInstance type = new ClassInstance();
		type.setCategory(ObjectTypes.USER);
		instance.setType(type);

		Set<Action> actions = new HashSet<>();
		actions.add(new EmfAction("someAction"));
		actions.add(new EmfAction(ActionTypeConstants.DEACTIVATE));

		filter.removeAdminDisableAction(new ActionEvaluatedEvent(instance, actions, "" ));

		assertEquals(2, actions.size());
		assertTrue(actions.contains(new EmfAction(ActionTypeConstants.DEACTIVATE)));
	}

	@Test
	public void removeAdminDisableAction_shouldDoNothingIfEmptyActions() throws Exception {
		Instance instance = new EmfInstance("emf:admin");
		ClassInstance type = new ClassInstance();
		type.setCategory(ObjectTypes.USER);
		instance.setType(type);

		Set<Action> actions = new HashSet<>();

		filter.removeAdminDisableAction(new ActionEvaluatedEvent(instance, actions, "" ));

		assertTrue(actions.isEmpty());
	}

	@Test
	public void removeAdminDisableAction_shouldDoRemoveDisableAction_IfAdminGroup() throws Exception {
		User admin = new EmfUser("admin");
		admin.setId("emf:admin");
		when(securityConfiguration.getAdminGroup()).thenReturn(new ConfigurationPropertyMock<>("GROUP_ADMIN"));

		when(resourceService.areEqual("GROUP_ADMIN", "emf:GROUP_ADMIN")).thenReturn(Boolean.TRUE);

		Instance instance = new EmfInstance("emf:GROUP_ADMIN");
		ClassInstance type = new ClassInstance();
		type.setCategory(ObjectTypes.GROUP);
		instance.setType(type);

		Set<Action> actions = new HashSet<>();
		actions.add(new EmfAction("someAction"));
		actions.add(new EmfAction(ActionTypeConstants.DEACTIVATE));

		filter.removeAdminDisableAction(new ActionEvaluatedEvent(instance, actions, "" ));

		assertEquals(1, actions.size());
		assertFalse(actions.contains(new EmfAction(ActionTypeConstants.DEACTIVATE)));
	}

	@Test
	public void removeAdminDisableAction_shouldDoRemoveDisableAction_IfEveryoneGroup() throws Exception {
		User admin = new EmfUser("admin");
		admin.setId("emf:admin");
		when(securityConfiguration.getAdminGroup()).thenReturn(new ConfigurationPropertyMock<>("GROUP_ADMIN"));

		when(resourceService.areEqual(anyString(), anyString())).thenReturn(Boolean.FALSE);

		Instance instance = new EmfInstance("emf:everyone");
		ClassInstance type = new ClassInstance();
		type.setCategory(ObjectTypes.GROUP);
		instance.setType(type);

		Set<Action> actions = new HashSet<>();
		actions.add(new EmfAction("someAction"));
		actions.add(new EmfAction(ActionTypeConstants.DEACTIVATE));

		filter.removeAdminDisableAction(new ActionEvaluatedEvent(instance, actions, "" ));

		assertEquals(1, actions.size());
		assertFalse(actions.contains(new EmfAction(ActionTypeConstants.DEACTIVATE)));
	}

	@Test
	public void removeAdminDisableAction_shouldDoRemoveActivateAndDeactivatedActions_IfSystemUser() throws Exception {
		User admin = new EmfUser("admin");
		admin.setId("emf:admin");
		when(securityConfiguration.getAdminGroup()).thenReturn(new ConfigurationPropertyMock<>("GROUP_ADMIN"));

		when(resourceService.areEqual(anyString(), anyString())).thenReturn(Boolean.TRUE);

		Instance instance = new EmfInstance("emf:system");
		ClassInstance type = new ClassInstance();
		type.setCategory(ObjectTypes.USER);
		instance.setType(type);

		Set<Action> actions = new HashSet<>();
		actions.add(new EmfAction("someAction"));
		actions.add(new EmfAction(ActionTypeConstants.DEACTIVATE));
		actions.add(new EmfAction(ActionTypeConstants.ACTIVATE));

		filter.removeAdminDisableAction(new ActionEvaluatedEvent(instance, actions, "" ));

		assertEquals(1, actions.size());
		assertFalse(actions.contains(new EmfAction(ActionTypeConstants.DEACTIVATE)));
		assertFalse(actions.contains(new EmfAction(ActionTypeConstants.ACTIVATE)));
	}
}
