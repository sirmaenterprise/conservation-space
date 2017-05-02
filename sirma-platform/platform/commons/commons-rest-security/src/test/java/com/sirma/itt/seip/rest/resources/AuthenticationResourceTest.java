package com.sirma.itt.seip.rest.resources;

import static org.mockito.Mockito.when;

import javax.json.JsonObject;

import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sirma.itt.seip.rest.secirity.SecurityTokensManager;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.rest.utils.JwtConfiguration;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.authentication.Authenticator;
import com.sirma.itt.seip.security.exception.AuthenticationException;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * Unit tests for {@link AuthenticationResource}
 *
 * @author yasko
 */
@Test
public class AuthenticationResourceTest {
	@InjectMocks
	private AuthenticationResource resource = new AuthenticationResource();

	@Mock
	private User user;

	@Mock
	private SecurityTokensManager tokensManager;

	@Mock
	private Authenticator authenticator;

	@Mock
	private JwtConfiguration jwtConfiguration;

	/**
	 * Init.
	 */
	@BeforeClass
	protected void init() {
		MockitoAnnotations.initMocks(this);
		when(user.canLogin()).thenReturn(Boolean.TRUE);
		Mockito.when(tokensManager.generate(user)).thenReturn("xxx.yyy.zzz");
		Mockito.when(authenticator.authenticate(Matchers.<AuthenticationContext> any())).thenReturn(user).thenReturn(
				null);
	}

	/**
	 * Test with valid authorization.
	 */
	public void testSuccessfulAuth() {
		when(user.canLogin()).thenReturn(Boolean.TRUE);

		// base64 is johndoe@doeinc.org:12345
		String header = "Basic am9obmRvZUBkb2VpbmMub3JnOjEyMzQ1";

		JsonObject response = resource.authenticate(header);
		Assert.assertNotNull(response);
		Assert.assertEquals(response.getString(JsonKeys.TOKEN), "xxx.yyy.zzz");
	}

	/**
	 * Test with valid authorization but user not allow to login
	 */
	@Test(expectedExceptions = AuthenticationException.class)
	public void testUnsuccessfulAuth_notAllowToLogin() {
		when(user.canLogin()).thenReturn(Boolean.FALSE);

		// base64 is johndoe@doeinc.org:12345
		String header = "Basic am9obmRvZUBkb2VpbmMub3JnOjEyMzQ1";

		JsonObject response = resource.authenticate(header);
		Assert.assertNotNull(response);
		Assert.assertEquals(response.getString(JsonKeys.TOKEN), "xxx.yyy.zzz");
	}

	@Test
	public void jwtConfiguration() {
		when(jwtConfiguration.getJwtParameterName()).thenReturn("APIKey");

		JsonAssert.assertJsonEquals("{\"parameterName\": \"APIKey\"}", resource.getAuthKey());
	}

	/**
	 * Test with various invalid authorization headers.
	 *
	 * @param header
	 *            Invalid authorization header.
	 */
	@Test(dependsOnMethods = "testSuccessfulAuth", dataProvider = "fail-auth-data-provider", expectedExceptions = AuthenticationException.class)
	public void testUnsuccessfulAuth(String header) {
		when(user.canLogin()).thenReturn(Boolean.TRUE);
		resource.authenticate(header);
	}

	@DataProvider(name = "fail-auth-data-provider")
	private Object[][] provide() {
		return new Object[][] { { "Basic am9obmRvZUBkb2VpbmMub3JnOjEyMzQ1" }, { null },
				{ "Digest am9obmRvZUBkb2VpbmMub3JnOjEyMzQ1" }, { "Basic \t" }, { "Basic " },
				{ " am9obmRvZUBkb2VpbmMub3JnOjEyMzQ1" }, };
	}
}
