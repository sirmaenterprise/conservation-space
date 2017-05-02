/**
 *
 */
package com.sirma.itt.seip.security.context;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * The Class ChainigAdminResolverTest.
 *
 * @author BBonev
 */
@Test
public class ChainigAdminResolverTest {

	@Mock
	AdminResolver adminResolver;

	@Spy
	List<AdminResolver> resolvers = new ArrayList<>();

	@Mock
	SecurityContext securityContext;

	@InjectMocks
	ChainigAdminResolver resolver;

	/**
	 * Before method.
	 */
	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		resolvers.clear();
	}

	/**
	 * Test_no resolvers.
	 */
	public void test_noResolvers() {
		assertFalse(resolver.isAdmin(securityContext));
	}

	/**
	 * Test_resolvers_no match.
	 */
	public void test_resolvers_noMatch() {
		resolvers.add(adminResolver);
		assertFalse(resolver.isAdmin(securityContext));
	}

	/**
	 * Test_resolvers_match.
	 */
	public void test_resolvers_match() {
		resolvers.add(adminResolver);
		when(adminResolver.isAdmin(securityContext)).thenReturn(Boolean.TRUE);
		assertTrue(resolver.isAdmin(securityContext));
	}
}
