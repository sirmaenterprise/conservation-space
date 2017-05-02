package com.sirma.itt.seip.resources.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.resources.security.UserCredentialService;
import com.sirma.itt.seip.resources.security.UserPasswordChangeRequest;

/**
 * Tests for {@link UserSettingsRestService}.
 *
 * @author smustafov
 */
public class UserSettingsRestServiceTest {

	@InjectMocks
	private UserSettingsRestService service;

	@Mock
	private UserCredentialService credentialService;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testChangePassword_emptyRequest() throws Exception {
		UserPasswordChangeRequest request = new UserPasswordChangeRequest();

		Response result = service.changePassword(request);
		assertEquals(Status.NOT_MODIFIED.getStatusCode(), result.getStatus());
	}

	@Test
	public void testChangePassword_correctRequest() throws Exception {
		UserPasswordChangeRequest request = new UserPasswordChangeRequest();
		request.setUsername("john");
		request.setOldPassword("123456");
		request.setNewPassword("6543212");

		when(credentialService.changeUserPassword(anyString(), anyString(), anyString())).thenReturn(Boolean.TRUE);

		Response result = service.changePassword(request);
		assertEquals(Status.OK.getStatusCode(), result.getStatus());
	}

}
