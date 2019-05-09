package com.sirma.itt.seip.rest.exceptions.mappers;

import java.util.Arrays;
import java.util.HashSet;

import javax.json.JsonObject;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.api.validation.ConstraintType.Type;
import org.jboss.resteasy.api.validation.ResteasyConstraintViolation;
import org.jboss.resteasy.api.validation.ResteasyViolationException;
import org.jboss.resteasy.plugins.providers.validation.ViolationsContainer;
import org.junit.Assert;
import org.junit.Test;

import com.sirma.itt.seip.rest.utils.ErrorCode;

public class ValidationExceptionMapperTest {

	@Test
	public void testUnsupportedConstraintType() {
		ResteasyConstraintViolation violation = new ResteasyConstraintViolation(Type.FIELD, null, null, null);
		ViolationsContainer<Object> container = new ViolationsContainer<>(new HashSet<>(Arrays.asList(violation)));
		Response response = new ValidationExceptionMapper().toResponse(new ResteasyViolationException(container));

		Assert.assertNotNull(response);

		JsonObject entity = (JsonObject) response.getEntity();
		Assert.assertEquals(ErrorCode.VALIDATION, entity.getInt(ExceptionMapperUtil.CODE));

		Assert.assertNull(entity.getJsonObject(ExceptionMapperUtil.ERRORS));
	}

	@Test
	public void testParameterViolation() {
		ResteasyConstraintViolation violation = new ResteasyConstraintViolation(Type.PARAMETER, "xxx.yyy", "is not good", null);
		ViolationsContainer<Object> container = new ViolationsContainer<>(new HashSet<>(Arrays.asList(violation)));
		Response response = new ValidationExceptionMapper().toResponse(new ResteasyViolationException(container));

		Assert.assertNotNull(response);

		JsonObject entity = (JsonObject) response.getEntity();
		Assert.assertEquals(ErrorCode.VALIDATION, entity.getInt(ExceptionMapperUtil.CODE));

		JsonObject errors = entity.getJsonObject(ExceptionMapperUtil.ERRORS);
		Assert.assertNotNull(errors);
		Assert.assertEquals(1, errors.size());

		JsonObject error = (JsonObject) errors.values().iterator().next();
		Assert.assertEquals(Type.PARAMETER.name(), error.getString(ExceptionMapperUtil.TYPE));
		Assert.assertEquals("validation", error.getString(ExceptionMapperUtil.ERROR));
		Assert.assertEquals("yyy is not good", error.getString(ExceptionMapperUtil.MESSAGE));
	}
}
