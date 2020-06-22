package com.sirma.sep.keycloak.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.URI;

import javax.ws.rs.core.Response;

import org.junit.Test;

import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Tests for {@link KeycloakApiUtil}.
 *
 * @author smustafov
 */
public class KeycloakApiUtilTest {

	@Test(expected = EmfRuntimeException.class)
	public void should_ThrowException_When_ResponseStatusIsNotCreate() {
		KeycloakApiUtil.getCreatedId(Response.ok().build());
	}

	@Test
	public void should_ReturnNull_When_NoResponseLocation() {
		assertNull(KeycloakApiUtil.getCreatedId(Response.created(null).build()));
	}

	@Test
	public void should_CorrectlyExtractId_When_ResponseHaveLocation() {
		String createdId = KeycloakApiUtil.getCreatedId(Response.created(
				URI.create("http://idp/auth/admin/realms/sep.test/users/5e9dd11a-6b23-4e6d-9fd8-c5558c4c986a"))
				.build());
		assertEquals("5e9dd11a-6b23-4e6d-9fd8-c5558c4c986a", createdId);
	}

}
