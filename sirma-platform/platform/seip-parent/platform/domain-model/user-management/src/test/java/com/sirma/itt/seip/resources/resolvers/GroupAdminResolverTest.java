package com.sirma.itt.seip.resources.resolvers;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Tests for {@link GroupAdminResolver}.
 *
 * @author smustafov
 */
public class GroupAdminResolverTest {

	private static final String ADMIN_GROUP_NAME = "ADMIN";
	private static final String ADMIN_USER_NAME = "admin";

	@Mock
	private SecurityConfiguration securityConfiguration;

	@Mock
	private ResourceService resourceService;

	@InjectMocks
	private GroupAdminResolver resolver;

	@Before
	public void before() {
		resolver = new GroupAdminResolver();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testIsAdmin_withNotAuthenticatedUser() {
		SecurityContext securityContext = mock(SecurityContext.class);
		when(securityContext.isAuthenticated()).thenReturn(Boolean.FALSE);
		ConfigurationProperty<String> adminGroupConfig = mock(ConfigurationProperty.class);
		when(adminGroupConfig.isSet()).thenReturn(Boolean.TRUE);

		when(securityConfiguration.getAdminGroup()).thenReturn(adminGroupConfig);

		assertFalse(resolver.isAdmin(securityContext));
	}

	@Test
	public void testIsAdmin_withNoAdminGroupConfig() {
		SecurityContext securityContext = mock(SecurityContext.class);
		when(securityContext.isAuthenticated()).thenReturn(Boolean.TRUE);
		ConfigurationProperty<String> adminGroupConfig = mock(ConfigurationProperty.class);
		when(adminGroupConfig.isSet()).thenReturn(Boolean.FALSE);

		when(securityConfiguration.getAdminGroup()).thenReturn(adminGroupConfig);

		assertFalse(resolver.isAdmin(securityContext));
	}

	@Test
	public void testIsAdmin_withAdminUser() {
		EmfGroup adminGroup = new EmfGroup();
		EmfUser adminUser = new EmfUser();
		adminUser.setName(ADMIN_USER_NAME);

		SecurityContext securityContext = mock(SecurityContext.class);
		when(securityContext.isAuthenticated()).thenReturn(Boolean.TRUE);
		when(securityContext.getAuthenticated()).thenReturn(adminUser);

		ConfigurationProperty<String> adminGroupConfig = mock(ConfigurationProperty.class);
		when(adminGroupConfig.isSet()).thenReturn(Boolean.TRUE);
		when(adminGroupConfig.get()).thenReturn(ADMIN_GROUP_NAME);

		when(securityConfiguration.getAdminGroup()).thenReturn(adminGroupConfig);
		when(resourceService.getResource(ADMIN_GROUP_NAME, ResourceType.GROUP)).thenReturn(adminGroup);
		when(resourceService.getContainedResourceIdentifiers(adminGroup, ResourceType.USER))
				.thenReturn(Arrays.asList(ADMIN_USER_NAME));

		assertTrue(resolver.isAdmin(securityContext));
	}

	@Test
	public void testIsAdmin_withNonAdminUser() {
		EmfGroup adminGroup = new EmfGroup();
		EmfUser nonAdminUser = new EmfUser();
		nonAdminUser.setName("regularuser");

		SecurityContext securityContext = mock(SecurityContext.class);
		when(securityContext.isAuthenticated()).thenReturn(Boolean.TRUE);
		when(securityContext.getAuthenticated()).thenReturn(nonAdminUser);

		ConfigurationProperty<String> adminGroupConfig = mock(ConfigurationProperty.class);
		when(adminGroupConfig.isSet()).thenReturn(Boolean.TRUE);
		when(adminGroupConfig.get()).thenReturn(ADMIN_GROUP_NAME);

		when(securityConfiguration.getAdminGroup()).thenReturn(adminGroupConfig);
		when(resourceService.getResource(ADMIN_GROUP_NAME, ResourceType.GROUP)).thenReturn(adminGroup);
		when(resourceService.getContainedResourceIdentifiers(adminGroup, ResourceType.USER))
				.thenReturn(Arrays.asList(ADMIN_USER_NAME));

		assertFalse(resolver.isAdmin(securityContext));
	}

	@Test
	public void testIsAdmin_withNotExistingAdminGroup() {
		EmfGroup adminGroup = new EmfGroup();
		EmfUser adminUser = new EmfUser();
		adminUser.setName(ADMIN_USER_NAME);

		SecurityContext securityContext = mock(SecurityContext.class);
		when(securityContext.isAuthenticated()).thenReturn(Boolean.TRUE);
		when(securityContext.getAuthenticated()).thenReturn(adminUser);

		ConfigurationProperty<String> adminGroupConfig = mock(ConfigurationProperty.class);
		when(adminGroupConfig.isSet()).thenReturn(Boolean.TRUE);
		when(adminGroupConfig.get()).thenReturn(ADMIN_GROUP_NAME);

		when(securityConfiguration.getAdminGroup()).thenReturn(adminGroupConfig);
		when(resourceService.getContainedResourceIdentifiers(adminGroup, ResourceType.USER))
				.thenReturn(Arrays.asList(ADMIN_USER_NAME));

		assertFalse(resolver.isAdmin(securityContext));
	}


}
