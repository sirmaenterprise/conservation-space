package com.sirma.itt.objects.web.object;

import static org.testng.Assert.assertEquals;

import java.io.Serializable;
import java.util.HashMap;

import javax.ws.rs.core.Response;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.model.EmfUser;
import com.sirma.itt.objects.ObjectsTest;
import com.sirma.itt.objects.domain.model.ObjectInstance;

/**
 * Test for PrimaryImageRestService.
 * 
 * @author sdjulgerova
 */
@Test
public class PrimaryImageRestServiceTest extends ObjectsTest {

	private static final String OBJECT_INSTANCE_ID = "emf:461c698c-943a-456c-ccbd-155f185040a7";

	protected static final Object CASE_INSTANCE = "caseinstance";

	protected static final Object OBJECT_INSTANCE = "objectinstance";

	protected static final Object SECTION_INSTANCE_ID = "emf:482c15b7-845f-4597-a9fb-a6b451a72578";

	private final PrimaryImageRestService controller;

	private final LinkService linkService;

	private TypeConverter typeConverter;

	private ObjectInstance objectInstance;
	private DocumentInstance documentInstance;

	private boolean returnNullUser = false;

	/**
	 * Instantiates a new object rest service test.
	 */
	public PrimaryImageRestServiceTest() {
		controller = new PrimaryImageRestService() {

			@Override
			@SuppressWarnings("unchecked")
			public <T extends Instance> T loadInstanceInternal(Class<T> type, Serializable id) {
				if (OBJECT_INSTANCE_ID.equals(id)) {
					return (T) documentInstance;
				}
				return null;
			}

			@Override
			public Instance fetchInstance(String instanceId, String instanceType) {
				if (OBJECT_INSTANCE.equals(instanceType)) {
					return objectInstance;
				}
				return null;
			}

			@Override
			public Resource getCurrentUser() {
				if (!returnNullUser) {
					EmfUser user = new EmfUser("admin");
					user.setId("emf:" + user.getIdentifier());
					return user;
				}
				return null;
			}

		};

		objectInstance = createObjectInstance(Long.valueOf(1));
		objectInstance.setProperties(new HashMap<String, Serializable>());

		documentInstance = createDocumentInstance(Long.valueOf(1));
		documentInstance.setProperties(new HashMap<String, Serializable>());

		linkService = Mockito.mock(LinkService.class);
		typeConverter = Mockito.mock(TypeConverter.class);

		ReflectionUtils.setField(controller, "linkService", linkService);
		ReflectionUtils.setField(controller, "typeConverter", typeConverter);

	}

	/**
	 * Adds the thumnail test.
	 */
	@SuppressWarnings("unchecked")
	public void addPrimaryImage() {
		Response response;
		String data;

		// Data is correct
		data = "{\"images\":[\"emf:461c698c-943a-456c-ccbd-155f185040a7\"], \"instanceId\":\"emf:cd44cc71-772c-4906-b19e-3369a9e62694\",\"instanceType\":\"objectinstance\"}";

		// Link Fail
		Pair<Serializable, Serializable> linkIds = new Pair<Serializable, Serializable>(null,
				"emf:a9282b5f69767b8607f9173f352b7a3047260951");

		Mockito.when(
				linkService.link(objectInstance.toReference(), documentInstance.toReference(),
						LinkConstants.HAS_PRIMARY_IMAGE, LinkConstants.IS_PRIMARY_IMAGE_OF,
						LinkConstants.DEFAULT_SYSTEM_PROPERTIES)).thenReturn(linkIds);

		response = controller.addPrimaryImage(data);
		assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

		linkIds = new Pair<Serializable, Serializable>(
				"emf:aa685231988484e753002dec0cbcc29c17ec6b9f", null);

		Mockito.when(
				linkService.link(objectInstance.toReference(), documentInstance.toReference(),
						LinkConstants.HAS_PRIMARY_IMAGE, LinkConstants.IS_PRIMARY_IMAGE_OF,
						LinkConstants.DEFAULT_SYSTEM_PROPERTIES)).thenReturn(linkIds);

		response = controller.addPrimaryImage(data);
		assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

		// Link is done
		linkIds = new Pair<Serializable, Serializable>(
				"emf:aa685231988484e753002dec0cbcc29c17ec6b9f",
				"emf:a9282b5f69767b8607f9173f352b7a3047260951");

		Mockito.when(
				linkService.link(Mockito.eq(objectInstance.toReference()),
						Mockito.eq(documentInstance.toReference()),
						Mockito.eq(LinkConstants.HAS_PRIMARY_IMAGE),
						Mockito.eq(LinkConstants.IS_PRIMARY_IMAGE_OF), Mockito.anyMap()))
				.thenReturn(linkIds);

		response = controller.addPrimaryImage(data);
		assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
	}

	/**
	 * Adds the primary image_invalid data.
	 */
	public void addPrimaryImage_invalidData() {
		Response response = controller.addPrimaryImage(null);
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

		response = controller.addPrimaryImage("");
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

		// Test if user is not logged in system
		String data = "{\"images\":[\"emf:461c698c-943a-456c-ccbd-155f185040a7\"]}";
		returnNullUser = true;
		response = controller.addPrimaryImage(data);
		assertEquals(response.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode());

		// User is logged but there's no correct instancetype
		data = "{\"images\":[\"emf:461c698c-943a-456c-ccbd-155f185040a7\"]}";
		returnNullUser = false;
		response = controller.addPrimaryImage(data);
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

		// User is logged but there's no correct instanceId
		data = "{\"images\":[\"emf:461c698c-943a-456c-ccbd-155f185040a7\"],\"instanceType\":\"objectinstance\"}";
		response = controller.addPrimaryImage(data);
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

		// User is logged but there's no correct dbId
		data = "{\"images\":[\"\"],\"instanceType\":\"objectinstance\", \"instanceId\":\"emf:cd44cc71-772c-4906-b19e-3369a9e62694\"}";
		response = controller.addPrimaryImage(data);
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

		// Object Instance is null
		data = "{\"images\":[\"emf:461c698c-943a-456c-ccbd-155f185040a7\"], \"instanceId\":\"emf:cd44cc71-772c-4906-b19e-3369a9e62694\",\"instanceType\":\"wrongType\"}";
		response = controller.addPrimaryImage(data);
		assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());

		// Document id is null
		data = "{\"images\":[\"wrongId\"], \"instanceId\":\"emf:cd44cc71-772c-4906-b19e-3369a9e62694\",\"instanceType\":\"objectinstance\"}";
		response = controller.addPrimaryImage(data);
		assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());

		// Not valid JSON is send
		data = "{\"images\":[\"This is not JSON\"], \"instanceId\":\"emf:cd44cc71-772c-4906-b19e-3369a9e62694\",\"instanceType\":\"objectinstance\"}";
		controller.addPrimaryImage(data);
	}
}
