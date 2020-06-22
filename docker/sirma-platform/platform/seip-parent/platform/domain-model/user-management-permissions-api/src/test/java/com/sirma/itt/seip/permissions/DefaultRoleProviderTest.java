package com.sirma.itt.seip.permissions;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

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
	public void provide_withOneExtension_extensionCalled() {
		providers.add(roleProviderExtension);
		provider.provide();
		verify(roleProviderExtension).getModel(anyMap());
	}

}
