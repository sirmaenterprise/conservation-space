package com.sirma.itt.seip.resources.rest.handlers;

import static com.sirma.itt.seip.resources.security.AccountConfirmationFailedException.ConfirmationFailType.ACCOUNT_LOCKED;
import static com.sirma.itt.seip.resources.security.AccountConfirmationFailedException.ConfirmationFailType.INVALID_CAPTCHA_ANSWER;
import static com.sirma.itt.seip.resources.security.AccountConfirmationFailedException.ConfirmationFailType.INVALID_CONFIRMATION_CODE;
import static com.sirma.itt.seip.resources.security.AccountConfirmationFailedException.ConfirmationFailType.INVALID_OR_EXPIRED_CONFIRMATION_CODE;
import static com.sirma.itt.seip.resources.security.AccountConfirmationFailedException.ConfirmationFailType.INVALID_PASSWORD;
import static com.sirma.itt.seip.resources.security.AccountConfirmationFailedException.ConfirmationFailType.INVALID_USERNAME;
import static com.sirma.itt.seip.resources.security.AccountConfirmationFailedException.ConfirmationFailType.UNEXPECTED_CODE;
import static com.sirma.itt.seip.resources.security.AccountConfirmationFailedException.ConfirmationFailType.UNKNOWN;
import static com.sirma.itt.seip.resources.security.AccountConfirmationFailedException.ConfirmationFailType.USER_DOES_NOT_EXIST;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.resources.security.AccountConfirmationFailedException;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * Test for {@link AccountConfirmationFailedExceptionMapper}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 30/08/2017
 */
public class AccountConfirmationFailedExceptionMapperTest {

	@InjectMocks
	private AccountConfirmationFailedExceptionMapper exceptionMapper;
	@Mock
	private LabelProvider labelProvider;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		when(labelProvider.getValue(anyString())).then(a -> a.getArgumentAt(0, String.class));
	}

	@Test
	public void toResponse_shouldReturn500_forUnknownError() throws Exception {
		AccountConfirmationFailedException exception = new AccountConfirmationFailedException(
				UNKNOWN, "newUser@tenant.com");

		Response response = exceptionMapper.toResponse(exception);
		validate(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus(), UNKNOWN,
				response.getEntity());
	}

	@Test
	public void toResponse_shouldReturn404_forInvalidUser() throws Exception {
		AccountConfirmationFailedException exception = new AccountConfirmationFailedException(
				INVALID_USERNAME, "newUser@tenant.com");

		Response response = exceptionMapper.toResponse(exception);
		validate(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus(), INVALID_USERNAME,
				response.getEntity());
	}

	@Test
	public void toResponse_shouldReturn404_forNotFoundUser() throws Exception {
		AccountConfirmationFailedException exception = new AccountConfirmationFailedException(
				USER_DOES_NOT_EXIST, "newUser@tenant.com");

		Response response = exceptionMapper.toResponse(exception);
		validate(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus(), USER_DOES_NOT_EXIST,
				response.getEntity());
	}

	@Test
	public void toResponse_shouldReturn503_forLockedUserAccount() throws Exception {
		AccountConfirmationFailedException exception = new AccountConfirmationFailedException(
				ACCOUNT_LOCKED, "newUser@tenant.com");

		Response response = exceptionMapper.toResponse(exception);
		validate(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus(), ACCOUNT_LOCKED,
				response.getEntity());
	}

	@Test
	public void toResponse_shouldReturn400_forInvalidPassword() throws Exception {
		AccountConfirmationFailedException exception = new AccountConfirmationFailedException(
				INVALID_PASSWORD, "newUser@tenant.com");

		Response response = exceptionMapper.toResponse(exception);
		validate(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus(), INVALID_PASSWORD,
				response.getEntity());
	}

	@Test
	public void toResponse_shouldReturn400_forInvalidCaptcha() throws Exception {
		AccountConfirmationFailedException exception = new AccountConfirmationFailedException(
				INVALID_CAPTCHA_ANSWER, "newUser@tenant.com");

		Response response = exceptionMapper.toResponse(exception);
		validate(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus(), INVALID_CAPTCHA_ANSWER,
				response.getEntity());
	}

	@Test
	public void toResponse_shouldReturn400_forInvalidConfirmationCode() throws Exception {
		AccountConfirmationFailedException exception = new AccountConfirmationFailedException(
				INVALID_CONFIRMATION_CODE, "newUser@tenant.com");

		Response response = exceptionMapper.toResponse(exception);
		validate(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus(), true, INVALID_CONFIRMATION_CODE,
				response.getEntity());
	}

	@Test
	public void toResponse_shouldReturn400_forExpiredOrInvalidConfirmationCode() throws Exception {
		AccountConfirmationFailedException exception = new AccountConfirmationFailedException(
				INVALID_OR_EXPIRED_CONFIRMATION_CODE, "newUser@tenant.com");

		Response response = exceptionMapper.toResponse(exception);
		validate(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus(), true,
				INVALID_OR_EXPIRED_CONFIRMATION_CODE,
				response.getEntity());
	}

	@Test
	public void toResponse_shouldReturn400_forUnexpectedCode() throws Exception {
		AccountConfirmationFailedException exception = new AccountConfirmationFailedException(
				UNEXPECTED_CODE, "newUser@tenant.com");

		Response response = exceptionMapper.toResponse(exception);
		validate(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus(), UNEXPECTED_CODE,
				response.getEntity());
	}

	private static void validate(int statusCode, int status, boolean expired,
			AccountConfirmationFailedException.ConfirmationFailType expected, Object entity) {
		assertEquals(statusCode, status);
		JsonAssert.assertJsonEquals("{\"message\":\"" + expected.getLabelId() + "\", \"expired\":" + expired + "}",
				entity);
	}

	private static void validate(int statusCode, int status,
			AccountConfirmationFailedException.ConfirmationFailType expected,
			Object entity) {
		assertEquals(statusCode, status);
		assertEquals("{\"message\":\"" + expected.getLabelId() + "\"}", entity);
	}

}
