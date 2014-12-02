/*
 *
 */
package com.sirma.cmf.web.object.relations;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.SemanticDefinitionService;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.event.EventService;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.link.LinkService;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.model.EmfUser;
import com.sirma.itt.emf.util.InstanceProxyMock;
import com.sirma.itt.emf.util.JsonUtil;

/**
 * Tests for RelationsRestService.
 * 
 * @author svelikov
 */
@Test
public class RelationsRestServiceTest extends CMFTest {

	/** The controller under test. */
	private RelationsRestService controller;

	/** The dictionary service. */
	private DictionaryService dictionaryService;

	/** The type converter. */
	private TypeConverter typeConverter;

	/** The instance. */
	private CaseInstance caseInstance;

	/** The document instance. */
	private DocumentInstance documentInstance;

	/** The link service. */
	private LinkService linkService;

	/** The json array. */
	private JSONArray responseData;

	private boolean callSuperBuildData;

	private EventService eventService;

	/** The semantic definition service. */
	private SemanticDefinitionService semanticDefinitionService;
	/**
	 * Instantiates a new objects explorer controller test.
	 */
	public RelationsRestServiceTest() {
		controller = new RelationsRestService() {

			@Override
			public InstanceReference getInstanceReferense(String instanceId, String instanceType) {
				if ("caseinstance".equalsIgnoreCase(instanceType)) {
					return createInstanceReference(instanceId, CaseInstance.class);
				} else if ("documentinstance".equalsIgnoreCase(instanceType)) {
					return createInstanceReference(instanceId, DocumentInstance.class);
				} else {
					return null;
				}
			}

			@Override
			protected JSONArray buildData(InstanceReference instance, String mode) {
				if (callSuperBuildData) {
					return super.buildData(instance, mode);
				}
				return responseData;
			}

			@Override
			public Resource getCurrentUser() {
				EmfUser user = new EmfUser("admin");
				user.setId("emf:" + user.getIdentifier());
				return user;
			}
		};

		dictionaryService = Mockito.mock(DictionaryService.class);
		typeConverter = Mockito.mock(TypeConverter.class);
		linkService = Mockito.mock(LinkService.class);
		eventService = Mockito.mock(EventService.class);

		semanticDefinitionService = Mockito.mock(SemanticDefinitionService.class);

		responseData = new JSONArray();
		caseInstance = createCaseInstance(Long.valueOf(1));
		documentInstance = createDocumentInstance(Long.valueOf(1));

		ReflectionUtils.setField(controller, "log", SLF4J_LOG);
		ReflectionUtils.setField(controller, "dictionaryService", dictionaryService);
		ReflectionUtils.setField(controller, "typeConverter", typeConverter);
		ReflectionUtils.setField(controller, "linkService", linkService);
		ReflectionUtils.setField(controller, "semanticDefinitionService",
				new InstanceProxyMock<SemanticDefinitionService>(semanticDefinitionService));
		ReflectionUtils.setField(controller, "eventService", eventService);
	}

	/**
	 * Load data test.
	 */
	public void loadDataTest() {
		callSuperBuildData = false;

		// if we don't pass all required arguments
		Response expectedResponse = controller.loadData(null, null, null, null, null, "outgoing");
		assertEquals(expectedResponse.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
		assertNotNull(expectedResponse.getEntity());

		// if we don't pass all required arguments
		expectedResponse = controller.loadData("1", null, null, null, null, "outgoing");
		assertEquals(expectedResponse.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
		assertNotNull(expectedResponse.getEntity());

		// if we don't pass all required arguments
		expectedResponse = controller.loadData(null, "caseinstance", null, null, null, "outgoing");
		assertEquals(expectedResponse.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
		assertNotNull(expectedResponse.getEntity());

		// if we pass all required data but no instance is found
		caseInstance = null;
		expectedResponse = controller.loadData("1", "caseinstanceinvalid", null, null, null,
				"outgoing");
		assertEquals(expectedResponse.getStatus(),
				Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		assertNotNull(expectedResponse.getEntity());

		// if we pass all required data and is found an instance we expect OK and json array as
		// response
		caseInstance = createCaseInstance(Long.valueOf(1));
		expectedResponse = controller.loadData("1", "caseinstance", null, null, null, "outgoing");
		assertEquals(expectedResponse.getStatus(), Response.Status.OK.getStatusCode());
		assertEquals(expectedResponse.getEntity(), responseData.toString());
	}

	/**
	 * Creates the relation test.
	 */
	public void createRelationTest() {

		Response expectedResponse = controller.createRelation(null);
		assertEquals(expectedResponse.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
		assertNotNull(expectedResponse.getEntity());

		// we have data but missing required arguments
		JSONObject data = new JSONObject();
		expectedResponse = controller.createRelation(data.toString());
		assertEquals(expectedResponse.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
		assertNotNull(expectedResponse.getEntity());

		// we have data but missing required arguments
		JsonUtil.addToJson(data, "relType", null);
		expectedResponse = controller.createRelation(data.toString());
		assertEquals(expectedResponse.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
		assertNotNull(expectedResponse.getEntity());

		// we have data but missing required arguments
		JsonUtil.addToJson(data, "relType", "hasChild");
		expectedResponse = controller.createRelation(data.toString());
		assertEquals(expectedResponse.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
		assertNotNull(expectedResponse.getEntity());

		// we have data but missing required arguments
		JsonUtil.addToJson(data, "selectedItems", null);
		expectedResponse = controller.createRelation(data.toString());
		assertEquals(expectedResponse.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
		assertNotNull(expectedResponse.getEntity());

		// we have data but missing required arguments
		JSONObject items = new JSONObject();
		JsonUtil.addToJson(data, "selectedItems", items);
		expectedResponse = controller.createRelation(data.toString());
		assertEquals(expectedResponse.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
		assertNotNull(expectedResponse.getEntity());

		// we have data but link service can't create link because of some issues
		Mockito.when(
				linkService.link(Mockito.any(InstanceReference.class),
						Mockito.any(InstanceReference.class), Mockito.anyString(),
						Mockito.anyString(), Mockito.anyMap())).thenReturn(
				Pair.<Serializable, Serializable> nullPair());

		Mockito.when(dictionaryService.getDataTypeDefinition("documentinstance")).thenReturn(
				new MockDatatype(DocumentInstance.class, null));
		Mockito.when(dictionaryService.getDataTypeDefinition("caseinstance")).thenReturn(
				new MockDatatype(CaseInstance.class, null));
		JSONObject item = new JSONObject();
		JsonUtil.addToJson(item, "destType", "documentinstance");
		JsonUtil.addToJson(item, "destId", "1");
		JsonUtil.addToJson(item, "targetType", "caseinstance");
		JsonUtil.addToJson(item, "targetId", "1");
		JsonUtil.addToJson(items, "item1", item);
		JsonUtil.addToJson(data, "selectedItems", items);
		expectedResponse = controller.createRelation(data.toString());
		// forbidden user relation : hasChild
		assertEquals(expectedResponse.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
		assertNotNull(expectedResponse.getEntity());

		// forbidden user relation : ptop:partOf
		JsonUtil.addToJson(data, "relType", "ptop:partOf");
		expectedResponse = controller.createRelation(data.toString());
		// forbidden user relation : hasChild
		assertEquals(expectedResponse.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
		assertNotNull(expectedResponse.getEntity());

		// with allowed relation type
		JsonUtil.addToJson(data, "relType", "blockedBy");
		expectedResponse = controller.createRelation(data.toString());
		assertEquals(expectedResponse.getStatus(),
				Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		assertNotNull(expectedResponse.getEntity());

		// we have data but link service can't create link because of some issues
		Mockito.when(
				linkService.link(Mockito.any(InstanceReference.class),
						Mockito.any(InstanceReference.class), Mockito.anyString(),
						Mockito.anyString(), Mockito.anyMap())).thenReturn(
				new Pair<Serializable, Serializable>("id", "id"));
		expectedResponse = controller.createRelation(data.toString());
		assertEquals(expectedResponse.getStatus(), Response.Status.OK.getStatusCode());
	}

	/**
	 * Builds the data test.
	 */
	public void buildDataTest() {
		callSuperBuildData = true;

		// we must not get nulls here
		JSONArray data = controller.buildData(null, "outgoing");
		assertEquals(data.toString(), "[]");

		//
		// data = controller.buildData(caseInstance);
		// assertEquals(data.toString(), "[]");
	}

	/**
	 * Deactivate relation test.
	 */
	public void deactivateRelationTest() {

		Response expectedResponse = controller.deactivateRelation(null, null, null, null);

		// if missing link id
		assertEquals(expectedResponse.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
		assertNotNull(expectedResponse.getEntity());

		// if link id is provided, we expect OK
		expectedResponse = controller.deactivateRelation("1", null, null, null);
		assertEquals(expectedResponse.getStatus(), Response.Status.OK.getStatusCode());
	}

	/**
	 * The Class MockDatatype.
	 */
	private class MockDatatype implements DataTypeDefinition {
		/** The clazz. */
		private final Class<?> clazz;
		/** The uri. */
		private final String uri;

		/**
		 * Instantiates a new mock datatype.
		 * 
		 * @param clazz
		 *            the clazz
		 * @param uri
		 *            the uri
		 */
		public MockDatatype(Class<?> clazz, String uri) {
			super();
			this.clazz = clazz;
			this.uri = uri;
		}

		@Override
		public Long getId() {
			return null;
		}

		@Override
		public void setId(Long id) {

		}

		@Override
		public String getName() {
			return getJavaClass().getSimpleName().toLowerCase();
		}

		@Override
		public String getTitle() {
			return null;
		}

		@Override
		public String getDescription() {
			return null;
		}

		@Override
		public String getJavaClassName() {
			return getJavaClass().getName();
		}

		@Override
		public Class<?> getJavaClass() {
			return clazz;
		}

		@Override
		public String getFirstUri() {
			return uri;
		}

		@Override
		public Set<String> getUries() {
			return Collections.emptySet();
		}

	}
}
