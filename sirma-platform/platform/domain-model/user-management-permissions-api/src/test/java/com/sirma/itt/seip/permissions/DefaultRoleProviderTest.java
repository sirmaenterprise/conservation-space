package com.sirma.itt.seip.permissions;

import static com.sirma.itt.seip.permissions.SecurityModel.BaseRoles.NO_PERMISSION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleIdentifier;
import com.sirma.itt.seip.permissions.role.RoleProviderExtension;

/**
 * Test for {@link DefaultRoleProvider}.
 *
 * @author A. Kunchev
 */
public class DefaultRoleProviderTest {

	@InjectMocks
	private DefaultRoleProvider provider;

	@Spy
	private List<RoleProviderExtension> providers = new ArrayList<>();

	@Mock
	private RoleProviderExtension roleProviderExtension;

	@Before
	public void setup() {
		provider = new DefaultRoleProvider();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void provide_noExtensions_onlyNoPersmissionRole() {
		Map<RoleIdentifier, Role> result = provider.provide();
		assertEquals(1, result.size());
		assertNotNull(result.get(NO_PERMISSION));
	}

	@Test
	public void provide_withOneExtension_extensionCalled() {
		providers.add(roleProviderExtension);
		provider.provide();
		verify(roleProviderExtension).getModel(anyMap());
	}

}
