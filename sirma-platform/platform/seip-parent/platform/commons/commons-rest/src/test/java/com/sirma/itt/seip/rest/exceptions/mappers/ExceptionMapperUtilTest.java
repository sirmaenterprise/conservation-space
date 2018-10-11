package com.sirma.itt.seip.rest.exceptions.mappers;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.rest.models.Error;
import com.sirma.itt.seip.rest.models.ErrorData;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonObject;
import java.util.Map;

/**
 * Tests the logic in {@link ExceptionMapperUtil}.
 *
 * @author Mihail Radkov
 */
public class ExceptionMapperUtilTest {

	@Test
	public void testErrorToJson() {
		ErrorData errorData = new ErrorData();
		errorData.setCode(410).setMessage("Resource no longer exists.");

		Error error = new Error();
		error.setMessage("Some message");
		error.setError("Some error");
		error.setType("Some type");

		Map<String, Error> errors = CollectionUtils.createHashMap(1);
		errors.put("error1", error);

		errorData.setErrors(errors);

		JsonObject errorJson = ExceptionMapperUtil.errorToJson(errorData);

		Assert.assertEquals(410, errorJson.getInt("code"));
		Assert.assertEquals("Resource no longer exists.", errorJson.getString("message"));

		JsonObject errorsObject = errorJson.getJsonObject("errors");
		Assert.assertNotNull(errorsObject);

		JsonObject error1Object = errorsObject.getJsonObject("error1");
		Assert.assertNotNull(error1Object);

		Assert.assertEquals("Some message", error1Object.getString("message"));
		Assert.assertEquals("Some error", error1Object.getString("error"));
		Assert.assertEquals("Some type", error1Object.getString("type"));
	}
}
