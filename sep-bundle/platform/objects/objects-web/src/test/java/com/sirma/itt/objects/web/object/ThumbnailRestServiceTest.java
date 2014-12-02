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
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkReference;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.rendition.RenditionService;
import com.sirma.itt.emf.rendition.ThumbnailService;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.model.EmfUser;
import com.sirma.itt.objects.ObjectsTest;
import com.sirma.itt.objects.domain.model.ObjectInstance;

/**
 * Test for ThumbnailRestService.
 * 
 * @author svelikov
 */
@Test
public class ThumbnailRestServiceTest extends ObjectsTest {

	private static final String OBJECT_INSTANCE_ID = "objectInstanceId";

	private static final String MISSING_OBJECT_ID = "missingObjectId";

	protected static final Object CASE_INSTANCE = "caseinstance";

	protected static final Object OBJECT_INSTANCE = "objectinstance";

	protected static final Object SECTION_INSTANCE_ID = "emf:482c15b7-845f-4597-a9fb-a6b451a72578";

	private final ThumbnailRestService controller;

	private final LinkService linkService;

	private TypeConverter typeConverter;

	private ObjectInstance objectInstance;
	private DocumentInstance documentInstance;

	ThumbnailService synchronizationService;

	private boolean linkExists;

	private RenditionService renditionService;

	private boolean returnNullUser = false;

	/**
	 * Instantiates a new object rest service test.
	 */
	public ThumbnailRestServiceTest() {
		controller = new ThumbnailRestService() {

			@Override
			@SuppressWarnings("unchecked")
			public <T extends Instance> T loadInstanceInternal(Class<T> type, Serializable id) {
				if (OBJECT_INSTANCE_ID.equals(id)) {
					return (T) documentInstance;
				}
				return null;
			}

			@Override
			@SuppressWarnings("unchecked")
			public Instance fetchInstance(String instanceId, String instanceType) {
				if (OBJECT_INSTANCE.equals(instanceType)) {
					return objectInstance;
				}
				return null;
			}

			@Override
			protected LinkReference linkExists(InstanceReference instance, String linkType) {
				if (linkExists) {
					return new LinkReference();
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
		synchronizationService = Mockito.mock(ThumbnailService.class);
		renditionService = Mockito.mock(RenditionService.class);

		ReflectionUtils.setField(controller, "linkService", linkService);
		ReflectionUtils.setField(controller, "typeConverter", typeConverter);
		ReflectionUtils.setField(controller, "synchronizationService", synchronizationService);
		ReflectionUtils.setField(controller, "renditionService", renditionService);

	}

	/**
	 * Adds the thumnail test.
	 */
	public void addThumnailTest() {
		Response response = controller.addThumbnail(null);
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

		response = controller.addThumbnail("");
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

		// Test if user is not logged in system
		String data = "{\"documentId\":\"emf:7d4629aa-3efe-4fb8-8575-cbe0d0d1377d\",\"instanceId\":\"emf:cd44cc71-772c-4906-b19e-3369a9e62694\",\"instanceType\":\"objectinstance\"}";
		returnNullUser = true;
		response = controller.addThumbnail(data);
		assertEquals(response.getStatus(), Response.Status.UNAUTHORIZED.getStatusCode());

		// InstanceID is null
		data = "{\"documentId\":\"emf:7d4629aa-3efe-4fb8-8575-cbe0d0d1377d\",\"instanceId\":\"\",\"instanceType\":\"objectinstance\"}";
		returnNullUser = false;
		response = controller.addThumbnail(data);
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

		// InstanceType is null
		data = "{\"documentId\":\"emf:7d4629aa-3efe-4fb8-8575-cbe0d0d1377d\",\"instanceId\":\"emf:cd44cc71-772c-4906-b19e-3369a9e62694\",\"instanceType\":\"\"}";
		response = controller.addThumbnail(data);
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

		// DocumentId is null
		data = "{\"documentId\":null,\"instanceId\":\"emf:cd44cc71-772c-4906-b19e-3369a9e62694\",\"instanceType\":\"objectinstance\"}";
		response = controller.addThumbnail(data);
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

		// Wrong object instance
		data = "{\"documentId\":\"emf:7d4629aa-3efe-4fb8-8575-cbe0d0d1377d\",\"instanceId\":\"emf:cd44cc71-772c-4906-b19e-3369a9e62694\",\"instanceType\":\"wrongInstance\"}";
		response = controller.addThumbnail(data);
		assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

		// Document Instance is null
		data = "{\"documentId\":\"emf:7d4629aa-3efe-4fb8-8575-cbe0d0d1377d\",\"instanceId\":\"emf:cd44cc71-772c-4906-b19e-3369a9e62694\",\"instanceType\":\"objectinstance\"}";
		response = controller.addThumbnail(data);
		assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

		// Correct data
		data = "{\"documentId\":\"objectInstanceId\",\"instanceId\":\"emf:cd44cc71-772c-4906-b19e-3369a9e62694\",\"instanceType\":\"objectinstance\"}";

		// Link Fail
		Pair<Serializable, Serializable> linkIds = new Pair<Serializable, Serializable>(null,
				"emf:a9282b5f69767b8607f9173f352b7a3047260951");

		Mockito.when(
				linkService.link(objectInstance.toReference(), documentInstance.toReference(),
						LinkConstants.HAS_THUMBNAIL, LinkConstants.IS_THUMBNAIL_OF,
						LinkConstants.DEFAULT_SYSTEM_PROPERTIES)).thenReturn(linkIds);

		response = controller.addThumbnail(data);
		assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

		linkIds = new Pair<Serializable, Serializable>(
				"emf:aa685231988484e753002dec0cbcc29c17ec6b9f", null);

		Mockito.when(
				linkService.link(objectInstance.toReference(), documentInstance.toReference(),
						LinkConstants.HAS_THUMBNAIL, LinkConstants.IS_THUMBNAIL_OF,
						LinkConstants.DEFAULT_SYSTEM_PROPERTIES)).thenReturn(linkIds);

		response = controller.addThumbnail(data);
		assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

		// Link is done
		linkIds = new Pair<Serializable, Serializable>(
				"emf:aa685231988484e753002dec0cbcc29c17ec6b9f",
				"emf:a9282b5f69767b8607f9173f352b7a3047260951");

		Mockito.when(
				linkService.link(objectInstance.toReference(), documentInstance.toReference(),
						LinkConstants.HAS_THUMBNAIL, LinkConstants.IS_THUMBNAIL_OF,
						LinkConstants.DEFAULT_SYSTEM_PROPERTIES)).thenReturn(linkIds);

		response = controller.addThumbnail(data);
		assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
		
		
		// Document Instance is null
		//data = "{\"documentId\":\"objectInstanceId\",\"instanceId\":\"emf:cd44cc71-772c-4906-b19e-3369a9e62694\",\"instanceType\":\"objectinstance\"}";
		linkExists = true;
		// Link is done
		linkIds = new Pair<Serializable, Serializable>(
				"emf:aa685231988484e753002dec0cbcc29c17ec6b9f",
				"emf:a9282b5f69767b8607f9173f352b7a3047260951");

		Mockito.when(
				linkService.link(objectInstance.toReference(), documentInstance.toReference(),
						LinkConstants.HAS_THUMBNAIL, LinkConstants.IS_THUMBNAIL_OF,
						LinkConstants.DEFAULT_SYSTEM_PROPERTIES)).thenReturn(linkIds);

		response = controller.addThumbnail(data);
		assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
	}

	/**
	 * Checks if has thumbanil method will return properly if requested object has a primary image
	 * attached.
	 */
	public void hasThumbnailTest() {
		Response response = controller.getThumbnail(null, null, true, null);
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

		response = controller.getThumbnail("", null, true, null);
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

		response = controller.getThumbnail(MISSING_OBJECT_ID, null, true, null);
		assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
		assertEquals(response.getEntity(), "false");

		// if requested object already has a primary image
		Mockito.when(
				renditionService.getThumbnail(OBJECT_INSTANCE_ID, RenditionService.DEFAULT_PURPOSE))
				.thenReturn("somethumbnailvalue");

		response = controller.getThumbnail(OBJECT_INSTANCE_ID, null, true,
				RenditionService.DEFAULT_PURPOSE);
		assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
		assertEquals(response.getEntity(), "true");
		
		// if requested object already has a primary image
		Mockito.when(
				renditionService.getThumbnail(OBJECT_INSTANCE_ID, RenditionService.DEFAULT_PURPOSE))
				.thenReturn("somethumbnailvalue");

		response = controller.getThumbnail(OBJECT_INSTANCE_ID, null, false,
				RenditionService.DEFAULT_PURPOSE);
		assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
		assertEquals(response.getEntity(), "somethumbnailvalue");
	}
}
