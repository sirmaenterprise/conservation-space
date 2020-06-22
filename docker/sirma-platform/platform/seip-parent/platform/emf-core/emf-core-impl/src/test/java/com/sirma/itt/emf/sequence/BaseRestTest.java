package com.sirma.itt.emf.sequence;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.testng.AssertJUnit;

import com.sirma.itt.seip.rest.RestUtil;
import com.sirma.itt.seip.testutil.EmfTest;

/**
 * Base class to house methods useful for rest unit tests.
 *
 * @author BBonev
 */
public abstract class BaseRestTest extends EmfTest {

	/**
	 * Read ok data.
	 *
	 * @param response
	 *            the response
	 * @param expectedsize
	 *            the expected number of responses
	 * @return the JSON array
	 */
	protected JSONArray readOkData(Response response, int expectedsize) {
		assertNotNull(response);
		assertEquals(response.getStatus(), Status.OK.getStatusCode());
		assertNotNull(response.getEntity());
		Object data = RestUtil.readDataRequest(response.getEntity().toString());
		assertNotNull(data);
		assertTrue(data instanceof JSONArray);
		JSONArray arrayData = (JSONArray) data;
		assertEquals(arrayData.length(), expectedsize);
		return arrayData;
	}

	/**
	 * Read error response.
	 *
	 * @param response
	 *            the response
	 * @param status
	 *            the expected status code
	 */
	protected void readErrorResponse(Response response, Status status) {
		AssertJUnit.assertNotNull(response);
		AssertJUnit.assertEquals(response.getStatus(), status.getStatusCode());
	}

}