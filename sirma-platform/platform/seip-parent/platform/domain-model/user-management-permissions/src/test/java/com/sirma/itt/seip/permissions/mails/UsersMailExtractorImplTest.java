package com.sirma.itt.seip.permissions.mails;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.permissions.SecurityModel;
import com.sirma.itt.seip.permissions.role.ResourceRole;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Group;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Tesf for {@link UsersMailExtractorImpl}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 09/10/2017
 */
public class UsersMailExtractorImplTest {
	@InjectMocks
	private UsersMailExtractorImpl mailExtractor;
	@Mock
	private ResourceService resourceService;
	@Mock
	private PermissionService permissionService;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		mailExtractor.init();

		when(resourceService.findResource("activeUser")).then(a -> {
			User user = new EmfUser("activeUser");
			user.setActive(true);
			user.setId("emf:activeUser");
			user.setEmail("activeUser@tenant.com");
			return user;
		});

		when(resourceService.findResource("roleResource")).then(a -> {
			User user = new EmfUser("roleResource");
			user.setActive(true);
			user.setId("emf:roleResource");
			user.setEmail("roleResource@tenant.com");
			return user;
		});

		when(resourceService.findResource("emf:inactiveUser")).then(a -> {
			User user = new EmfUser("inactiveUser");
			user.setActive(false);
			user.setId("emf:inactiveUser");
			user.setEmail("inactiveUser@tenant.com");
			return user;
		});

		when(resourceService.findResource("GROUP_someGroup")).then(a -> {
			Group group = new EmfGroup("GROUP_someGroup", "GROUP_someGroup");
			group.setId("emf:GROUP_someGroup");
			return group;
		});
		when(resourceService.getContainedResources("emf:GROUP_someGroup")).then(a -> {
			User active = new EmfUser("someActiveUser");
			active.setActive(true);
			active.setEmail("someActiveUser@tenant.com");
			User inactive = new EmfUser("someInactiveUser");
			inactive.setActive(false);
			inactive.setEmail("someInactiveUser@tenant.com");
			return Arrays.asList(active, inactive);
		});
	}

	@Test
	public void extractMails_shouldCollectEmailsOfActiveResources() throws Exception {
		InstanceReferenceMock reference = InstanceReferenceMock.createGeneric("emf:instance");
		Instance instance = reference.toInstance();
		instance.add("property", "propertyEmail@tenant.com");
		instance.add("userProperty", "emf:inactiveUser");

		Map<String, ResourceRole> assignments = new HashMap<>();
		ResourceRole consumer = new ResourceRole();
		consumer.setRole(SecurityModel.BaseRoles.CONSUMER);
		assignments.put("test", consumer);

		ResourceRole collaborator = new ResourceRole();
		collaborator.setRole(SecurityModel.BaseRoles.COLLABORATOR);
		assignments.put("roleResource", collaborator);

		when(permissionService.getPermissionAssignments(eq(reference))).thenReturn(assignments);

		Collection<String> mails = mailExtractor.extractMails(
				Arrays.asList("activeUser", "emf:inactiveUser", "GROUP_someGroup",
						SecurityModel.BaseRoles.COLLABORATOR.getIdentifier(), "property", "userProperty",
						"user@test.com"),
				instance);

		assertArrayEquals(new Object[] { "someActiveUser@tenant.com", "roleResource@tenant.com", "user@test.com",
				"propertyEmail@tenant.com", "activeUser@tenant.com" }, mails.toArray());
	}

	@Test
	public void extractMails_shouldDoNothingOnInvalidData() throws Exception {
		Collection<String> mails = mailExtractor.extractMails(Collections.emptyList(), new EmfInstance());
		assertTrue(mails.isEmpty());

		mails = mailExtractor.extractMails(Collections.singleton("activeUser"), null);
		assertTrue(mails.isEmpty());
	}

}
