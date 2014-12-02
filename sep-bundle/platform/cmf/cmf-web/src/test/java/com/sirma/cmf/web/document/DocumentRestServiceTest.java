package com.sirma.cmf.web.document;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import javax.ws.rs.core.Response;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.cmf.web.instance.AttachInstanceAction;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;

/**
 * Tests for DocumentRestService.
 * 
 * @author svelikov
 */
@Test
public class DocumentRestServiceTest extends CMFTest {

	private final DocumentRestService controller;
	private final AttachInstanceAction attachInstanceAction;

	/**
	 * Instantiates a new object rest service test.
	 */
	public DocumentRestServiceTest() {
		controller = new DocumentRestService();

		attachInstanceAction = Mockito.mock(AttachInstanceAction.class);

		ReflectionUtils.setField(controller, "log", SLF4J_LOG);
		ReflectionUtils.setField(controller, "attachInstanceAction", attachInstanceAction);
	}

	/**
	 * Attach documents test.
	 */
	public void attachDocumentsTest() {
		// if data is not provided we should get error response
		Response expectedResponse = Response.status(Response.Status.BAD_REQUEST).entity("").build();
		Mockito.when(
				attachInstanceAction.attachDocuments(null, controller, null,
						ActionTypeConstants.ATTACH_DOCUMENT)).thenReturn(expectedResponse);
		Response response = controller.attachDocuments(null);
		assertNotNull(response);
		assertEquals(response.getStatus(), expectedResponse.getStatus());
	}
}
