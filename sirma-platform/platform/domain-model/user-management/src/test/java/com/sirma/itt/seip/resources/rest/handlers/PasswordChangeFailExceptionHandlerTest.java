package com.sirma.itt.seip.resources.rest.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.resources.security.PasswordChangeFailException;
import com.sirma.itt.seip.resources.security.PasswordChangeFailException.PasswordFailType;

/**
 * Tests for {@link PasswordChangeFailExceptionHandler}.
 *
 * @author smustafov
 */
public class PasswordChangeFailExceptionHandlerTest {

	@InjectMocks
	private PasswordChangeFailExceptionHandler handler;

	@Mock
	private LabelProvider labelProvider;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(labelProvider.getValue(anyString())).thenReturn("label");
	}

	@Test
	public void testToResponse_wrongPassword() {
		Response response = handler
				.toResponse(new PasswordChangeFailException(PasswordFailType.WRONG_OLD_PASSWORD, "wrong pass"));

		assertNotNull(response);
		assertNotNull(response.getEntity());
		assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
		verify(labelProvider).getValue(anyString());
	}

	@Test
	public void testToResponse_shortPassword() {
		Response response = handler
				.toResponse(new PasswordChangeFailException(PasswordFailType.SHORT_PASSWORD, "short pass"));

		assertNotNull(response);
		assertNotNull(response.getEntity());
		assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
		verify(labelProvider).getValue(anyString());
	}

}
