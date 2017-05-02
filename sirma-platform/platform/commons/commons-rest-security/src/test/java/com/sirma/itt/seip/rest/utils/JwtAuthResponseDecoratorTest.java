package com.sirma.itt.seip.rest.utils;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.rest.secirity.SecurityTokensManager;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.authentication.AuthenticationResponseDecorator;

/**
 * Tests for {@link JwtAuthResponseDecorator}.
 *
 * @author smustafov
 */
public class JwtAuthResponseDecoratorTest {

	@InjectMocks
	private JwtAuthResponseDecorator decorator = new JwtAuthResponseDecorator();

	@Mock
	private SecurityTokensManager tokensManager;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Tests {@link JwtAuthResponseDecorator#decorate(Map)} with user and relay state.
	 */
	@Test
	public void testDecorate() {
		User user = Mockito.mock(User.class);
		Map<String, Object> request = new HashMap<>();
		request.put(AuthenticationResponseDecorator.RELAY_STATE, "emf?instanceid=1");
		request.put(AuthenticationResponseDecorator.USER, user);

		Mockito.when(tokensManager.generate(user)).thenReturn("token");

		decorator.decorate(request);
		Assert.assertEquals("emf?instanceid=1&jwt=token", request.get(AuthenticationResponseDecorator.RELAY_STATE));
	}

}
