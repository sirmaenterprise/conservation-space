package com.sirma.itt.seip.rest.utils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
		when(tokensManager.generate(any(User.class))).thenReturn("token");
	}

	@Test
	public void should_AppendTokenAsLast_When_ThereIsExistingUrlParams() {
		Map<String, Object> request = mockRequest("emf?instanceid=1");

		decorator.decorate(request);

		assertEquals("emf?instanceid=1&jwt=token", request.get(AuthenticationResponseDecorator.RELAY_STATE));
	}

	@Test
	public void should_AppendTokenAsFirst_When_ThereIsNoUrlParams() {
		Map<String, Object> request = mockRequest("emf");

		decorator.decorate(request);

		assertEquals("emf?jwt=token", request.get(AuthenticationResponseDecorator.RELAY_STATE));
	}

	private static Map<String, Object> mockRequest(String relayState) {
		User user = mock(User.class);
		Map<String, Object> request = new HashMap<>();
		request.put(AuthenticationResponseDecorator.RELAY_STATE, relayState);
		request.put(AuthenticationResponseDecorator.USER, user);
		return request;
	}

}
