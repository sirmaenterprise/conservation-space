package com.sirma.itt.seip.rest.security;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;

import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.rest.annotations.security.PublicResource;
import com.sirma.itt.seip.rest.filters.AuthorizationFilter;
import com.sirma.itt.seip.rest.filters.TenantInitializationForPublicAccessFilter;
import com.sirma.itt.seip.rest.secirity.AuthorizationRegistrationFeature;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.CustomMatcher;

/**
 * Test for {@link AuthorizationRegistrationFeature}.
 * @author yasko
 */
@Test
public class AuthorizationRegistrationFeatureTest {

	@InjectMocks
	private AuthorizationRegistrationFeature feature;

	@Mock
	private FeatureContext context;
	@Mock
	private ResourceInfo info;
	@Mock
	private AuthorizationFilter filter;
	@Mock
	private SecurityContextManager securityContextManager;

	/**
	 * Init tests.
	 */
	@BeforeMethod
	protected void init() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test that the filter is not registered for public resources.
	 * @throws Exception reflection stuff.
	 */
	public void testPublicResource() throws Exception {
		Mockito.when(info.getResourceMethod()).thenReturn(AuthorizationRegistrationFeatureTest.class.getDeclaredMethod("publicResource"));
		feature.configure(info, context);
		Mockito.verify(context, Mockito.never()).register(filter);
	}

	/**
	 * Test that the filter is registered for secured resources.
	 * @throws Exception reflection stuff.
	 */
	public void testSecuredResource() throws Exception {
		Mockito.when(info.getResourceMethod()).thenReturn(AuthorizationRegistrationFeatureTest.class.getDeclaredMethod("securedResource"));
		feature.configure(info, context);
		Mockito.verify(context).register(filter);
	}

	/**
	 * Test that the filter is registered for per tenant public resources.
	 *
	 * @throws Exception
	 *             reflection stuff.
	 */
	public void testTenantSpecificPublicResource() throws Exception {
		Mockito.when(info.getResourceMethod()).thenReturn(
				AuthorizationRegistrationFeatureTest.class.getDeclaredMethod("tenantSpecificPublicResource"));
		feature.configure(info, context);
		// this should call the method that accepts the Object as argument
		// if the cast is removed the test will fail
		verify(context).register((Object)argThat(CustomMatcher.of(TenantInitializationForPublicAccessFilter.class::isInstance)));
	}

	@PublicResource
	protected void publicResource() {
		// used as test data
	}

	@PublicResource(tenantParameterName = "tenant")
	public void tenantSpecificPublicResource() {
		// used as test data
	}

	protected void securedResource() {
		// used as test data
	}
}
