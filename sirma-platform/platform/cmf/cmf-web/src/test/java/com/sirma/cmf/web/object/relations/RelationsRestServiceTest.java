/*
 *
 */
package com.sirma.cmf.web.object.relations;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.Serializable;
import java.util.Arrays;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;

/**
 * Tests for RelationsRestService.
 *
 * @author svelikov
 * @author A. Kunchev
 */
@Test
public class RelationsRestServiceTest extends CMFTest {

	private static final String TARGET_ID = "targetId";
	private static final String TARGET_TYPE = "targetType";
	private static final String DESTINATION_ID = "destId";
	private static final String DESTINATION_TYPE = "destType";
	private static final String ID = "id";
	private static final String DOCUMENT_INSTANCE_TYPE = "documentinstance";
	private static final String CASE_INSTANCE_TYPE = "caseinstance";
	private static final String OUTGOING = "outgoing";
	private static final String SELECTED_ITEMS = "selectedItems";
	private static final String RELATION_TYPE = "relType";
	private static final String REVERSE_RELATION_TYPE = "reverseRelType";

	private RelationsRestService controller;

	private DictionaryService dictionaryService;

	private TypeConverter typeConverter;

	private LinkService linkService;

	protected JSONArray responseData;

	protected boolean callSuperBuildData;

	protected boolean getNullUser;

	private EventService eventService;

	private SemanticDefinitionService semanticDefinitionService;

	private InstanceLoadDecorator instanceLoadDecorator;

	private LabelProvider labelProvider;

	/**
	 * Instantiates a new objects explorer controller test.
	 */
	public RelationsRestServiceTest() {
		controller = new RelationsRestService() {

			@Override
			public InstanceReference getInstanceReference(String instanceId, String instanceType) {
				if (CASE_INSTANCE_TYPE.equalsIgnoreCase(instanceType)) {
					return createInstanceReference(instanceId, EmfInstance.class);
				} else if (DOCUMENT_INSTANCE_TYPE.equalsIgnoreCase(instanceType)) {
					return createInstanceReference(instanceId, EmfInstance.class);
				} else {
					return null;
				}
			}

			@Override
			protected JSONArray buildData(InstanceReference instance, String mode, String fields, String linkId) {
				if (callSuperBuildData) {
					return super.buildData(instance, mode, fields, linkId);
				}
				return responseData;
			}

			@Override
			public Resource getCurrentUser() {
				if (getNullUser) {
					return null;
				}
				EmfUser user = new EmfUser("admin");
				user.setId("emf:" + user.getName());
				return user;
			}

		};

		dictionaryService = mock(DictionaryService.class);
		typeConverter = mock(TypeConverter.class);
		linkService = mock(LinkService.class);
		eventService = mock(EventService.class);
		instanceLoadDecorator = mock(InstanceLoadDecorator.class);
		labelProvider = mock(LabelProvider.class);

		semanticDefinitionService = mock(SemanticDefinitionService.class);
		when(semanticDefinitionService.isSystemRelation("emf:hasChild")).thenReturn(Boolean.TRUE);
		when(semanticDefinitionService.isSystemRelation("ptop:partOf")).thenReturn(Boolean.TRUE);
		when(labelProvider.getValue(anyString())).thenReturn("Some message");

		LinkConstants.init(mock(SecurityContextManager.class), ContextualMap.create());

		responseData = new JSONArray();

		ReflectionUtils.setField(controller, "LOG", SLF4J_LOG);
		ReflectionUtils.setField(controller, "dictionaryService", dictionaryService);
		ReflectionUtils.setField(controller, "typeConverter", typeConverter);
		ReflectionUtils.setField(controller, "linkService", linkService);
		ReflectionUtils.setField(controller, "semanticDefinitionService",
				new InstanceProxyMock<>(semanticDefinitionService));
		ReflectionUtils.setField(controller, "eventService", eventService);
		ReflectionUtils.setField(controller, "instanceLoadDecorator", instanceLoadDecorator);
		ReflectionUtils.setField(controller, "labelProvider", labelProvider);
	}

	/**
	 * Load data test.
	 */
	public void loadDataTest() {
		callSuperBuildData = false;

		// if we don't pass all required arguments
		Response expectedResponse = controller.loadData(null, null, null, null, null, OUTGOING, null, null);
		assertEquals(expectedResponse.getStatus(), Status.BAD_REQUEST.getStatusCode());
		assertNotNull(expectedResponse.getEntity());

		// if we don't pass all required arguments
		expectedResponse = controller.loadData("1", null, null, null, null, OUTGOING, null, null);
		assertEquals(expectedResponse.getStatus(), Status.BAD_REQUEST.getStatusCode());
		assertNotNull(expectedResponse.getEntity());

		// if we don't pass all required arguments
		expectedResponse = controller.loadData(null, CASE_INSTANCE_TYPE, null, null, null, OUTGOING, null, null);
		assertEquals(expectedResponse.getStatus(), Status.BAD_REQUEST.getStatusCode());
		assertNotNull(expectedResponse.getEntity());

		// if we pass all required data but no instance is found
		expectedResponse = controller.loadData("1", "caseinstanceinvalid", null, null, null, OUTGOING, null, null);
		assertEquals(expectedResponse.getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
		assertNotNull(expectedResponse.getEntity());

		// if we pass all required data and is found an instance we expect OK
		// and json array as
		// response
		expectedResponse = controller.loadData("1", CASE_INSTANCE_TYPE, null, null, null, OUTGOING, null, null);
		assertEquals(expectedResponse.getStatus(), Status.OK.getStatusCode());
		assertEquals(expectedResponse.getEntity(), responseData.toString());
	}

	/**
	 * Creates the relation test.
	 */
	public void createRelationTest() {

		Response expectedResponse = controller.createRelation(null);
		assertEquals(expectedResponse.getStatus(), Status.BAD_REQUEST.getStatusCode());
		assertNotNull(expectedResponse.getEntity());

		// we have data but missing required arguments
		JSONObject data = new JSONObject();
		expectedResponse = controller.createRelation(data.toString());
		assertEquals(expectedResponse.getStatus(), Status.BAD_REQUEST.getStatusCode());
		assertNotNull(expectedResponse.getEntity());

		// we have data but missing required arguments
		JsonUtil.addToJson(data, RELATION_TYPE, null);
		expectedResponse = controller.createRelation(data.toString());
		assertEquals(expectedResponse.getStatus(), Status.BAD_REQUEST.getStatusCode());
		assertNotNull(expectedResponse.getEntity());

		// we have data but missing required arguments
		JsonUtil.addToJson(data, RELATION_TYPE, "emf:hasChild");
		expectedResponse = controller.createRelation(data.toString());
		assertEquals(expectedResponse.getStatus(), Status.BAD_REQUEST.getStatusCode());
		assertNotNull(expectedResponse.getEntity());

		// we have data but missing required arguments
		JsonUtil.addToJson(data, SELECTED_ITEMS, null);
		expectedResponse = controller.createRelation(data.toString());
		assertEquals(expectedResponse.getStatus(), Status.BAD_REQUEST.getStatusCode());
		assertNotNull(expectedResponse.getEntity());

		// we have data but missing required arguments
		JSONObject items = new JSONObject();
		JsonUtil.addToJson(data, SELECTED_ITEMS, items);
		expectedResponse = controller.createRelation(data.toString());
		assertEquals(expectedResponse.getStatus(), Status.BAD_REQUEST.getStatusCode());
		assertNotNull(expectedResponse.getEntity());

		// we have data but link service can't create link because of some
		// issues
		when(linkService.link(any(InstanceReference.class), any(InstanceReference.class), anyString(),
				isNull(String.class), anyMap())).thenReturn(Pair.nullPair());

		when(dictionaryService.getDataTypeDefinition(DOCUMENT_INSTANCE_TYPE))
				.thenReturn(new DataTypeDefinitionMock(EmfInstance.class, null));
		when(dictionaryService.getDataTypeDefinition(CASE_INSTANCE_TYPE))
				.thenReturn(new DataTypeDefinitionMock(EmfInstance.class, null));
		JSONObject item = new JSONObject();
		JsonUtil.addToJson(item, DESTINATION_TYPE, DOCUMENT_INSTANCE_TYPE);
		JsonUtil.addToJson(item, DESTINATION_ID, "1");
		JsonUtil.addToJson(item, TARGET_TYPE, CASE_INSTANCE_TYPE);
		JsonUtil.addToJson(item, TARGET_ID, "1");
		JsonUtil.addToJson(items, "item1", item);
		JsonUtil.addToJson(data, SELECTED_ITEMS, items);
		expectedResponse = controller.createRelation(data.toString());

		// forbidden user relation : emf:hasChild
		assertEquals(expectedResponse.getStatus(), Status.BAD_REQUEST.getStatusCode());
		assertNotNull(expectedResponse.getEntity());

		// forbidden user relation : ptop:partOf
		JsonUtil.addToJson(data, RELATION_TYPE, "ptop:partOf");
		expectedResponse = controller.createRelation(data.toString());
		// forbidden user relation : hasChild
		assertEquals(expectedResponse.getStatus(), Status.BAD_REQUEST.getStatusCode());
		assertNotNull(expectedResponse.getEntity());

		// with allowed relation type
		JsonUtil.addToJson(data, RELATION_TYPE, "blockedBy");
		expectedResponse = controller.createRelation(data.toString());
		assertEquals(expectedResponse.getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
		assertNotNull(expectedResponse.getEntity());

		// we have data but link service can't create link because of some
		// issues
		when(linkService.link(any(InstanceReference.class), any(InstanceReference.class), anyString(),
				isNull(String.class), anyMap())).thenReturn(new Pair<Serializable, Serializable>(ID, ID));
		expectedResponse = controller.createRelation(data.toString());
		assertEquals(expectedResponse.getStatus(), Status.OK.getStatusCode());

		// with passed operationId
		JsonUtil.addToJson(data, "operationId", "someOperation");
		expectedResponse = controller.createRelation(data.toString());
		assertEquals(expectedResponse.getStatus(), Status.OK.getStatusCode());
	}

	// ----------------------------- ADDITIONAL CREATE RELATION TESTS -------------------------------------

	/**
	 * Wrong JSON format.
	 */
	public void createRelation_wrongJsonData_serverErrorResponse() {
		Response response = controller.createRelation("dffafa: \"dgfadgasfassg");
		assertEquals(response.getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
	}

	/**
	 * Empty selected items JSON parameter.
	 */
	public void createRelation_emptySelectedItems_badRequestResponse() {
		JSONObject data = new JSONObject();
		JsonUtil.addToJson(data, RELATION_TYPE, LinkConstants.HAS_FAVOURITE);
		JsonUtil.addToJson(data, SELECTED_ITEMS, new JSONObject());
		Response response = controller.createRelation(data.toString());
		assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
	}

	/**
	 * No target instance.
	 */
	public void createRelation_noTargetInstance_badRequestResponse() {
		JSONObject item = new JSONObject();
		JsonUtil.addToJson(item, TARGET_ID, "");
		JsonUtil.addToJson(item, TARGET_TYPE, "");
		JsonUtil.addToJson(item, DESTINATION_ID, "1233");
		JsonUtil.addToJson(item, DESTINATION_TYPE, CASE_INSTANCE_TYPE);
		Response response = controller.createRelation(buildRequestData(item, null));
		assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
	}

	/**
	 * No destination instance.
	 */
	public void createRelation_noDestinationInstance_badRequestResponse() {
		JSONObject item = new JSONObject();
		JsonUtil.addToJson(item, TARGET_ID, "1223");
		JsonUtil.addToJson(item, TARGET_TYPE, DOCUMENT_INSTANCE_TYPE);
		JsonUtil.addToJson(item, DESTINATION_ID, "");
		JsonUtil.addToJson(item, DESTINATION_TYPE, "");
		Response response = controller.createRelation(buildRequestData(item, null));
		assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
	}

	/**
	 * No selectedItems in JSON.
	 */
	public void createRelation_noItems_badRequestResponse() {
		String data = buildRequestData(new JSONObject(), null).replaceFirst(SELECTED_ITEMS, "");
		Response response = controller.createRelation(data);
		assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
	}

	/**
	 * Same instance.
	 *
	 * @throws JSONException
	 *             when there is no 'messages' parameter in response entity
	 */
	public void createRelation_sameInstance_responseMessagesNotEmpty() throws JSONException {
		JSONObject item = new JSONObject();
		JsonUtil.addToJson(item, TARGET_ID, "1223");
		JsonUtil.addToJson(item, TARGET_TYPE, DOCUMENT_INSTANCE_TYPE);
		JsonUtil.addToJson(item, DESTINATION_ID, "1223");
		JsonUtil.addToJson(item, DESTINATION_TYPE, DOCUMENT_INSTANCE_TYPE);
		Response response = controller.createRelation(buildRequestData(item, null));
		assertEquals(response.getStatus(), Status.OK.getStatusCode());
		JSONObject object = new JSONObject(response.getEntity().toString());
		JSONArray messages = (JSONArray) object.get("messages");
		assertEquals(messages.length(), 1);
	}

	private static String buildRequestData(JSONObject item, String oprationId) {
		JSONObject data = new JSONObject();
		JsonUtil.addToJson(data, RELATION_TYPE, LinkConstants.HAS_ATTACHMENT);
		JsonUtil.addToJson(data, REVERSE_RELATION_TYPE, LinkConstants.IS_ATTACHED_TO);
		JsonUtil.addToJson(data, "oprationId", oprationId);
		JSONObject selectedItems = new JSONObject();
		JsonUtil.addToJson(selectedItems, "key1", item);
		JsonUtil.addToJson(data, SELECTED_ITEMS, selectedItems);
		return data.toString();
	}

	// ------------------------------ UPDATE RELATION ----------------------------------------

	/**
	 * Empty relaton id.
	 */
	public void updateRelation_emptyRelationId_badRequestResponse() {
		Response response = controller.updateRelation("", new JSONObject().toString());
		assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
	}

	/**
	 * Null relation id.
	 */
	public void updateRelation_nullRelationId_badRequestResponse() {
		Response response = controller.updateRelation(null, new JSONObject().toString());
		assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
	}

	/**
	 * Wrong JSON.
	 */
	public void updateRelation_wrongJsonFormat_serverErrorResponse() {
		Response response = controller.updateRelation(LinkConstants.HAS_FAVOURITE, "gdsgasdgsadg: 'agsdgsdgsgds");
		assertEquals(response.getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
	}

	/**
	 * Disallowed relation.
	 */
	public void updateRelation_disallowedRelation_badRequestResponse() {
		JSONObject data = new JSONObject();
		JsonUtil.addToJson(data, RELATION_TYPE, LinkConstants.PART_OF_URI);
		Response response = controller.updateRelation(LinkConstants.PART_OF_URI, data.toString());
		assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
	}

	/**
	 * No selected items.
	 */
	public void updateRelation_noSelectedItems_serverErrorResponse() {
		Response response = controller.updateRelation(LinkConstants.HAS_FAVOURITE,
				buildRequestData(null, null).replaceAll(SELECTED_ITEMS, ""));
		assertEquals(response.getStatus(), Status.INTERNAL_SERVER_ERROR.getStatusCode());
	}

	/**
	 * Successful update relation.
	 */
	public void updateRelation_okResponse() {
		JSONObject item = new JSONObject();
		JsonUtil.addToJson(item, TARGET_ID, "12523");
		JsonUtil.addToJson(item, TARGET_TYPE, CASE_INSTANCE_TYPE);
		JsonUtil.addToJson(item, DESTINATION_ID, "1223");
		JsonUtil.addToJson(item, DESTINATION_TYPE, DOCUMENT_INSTANCE_TYPE);
		doNothing().when(linkService).removeLinkById(anyString());
		when(linkService.link(any(InstanceReference.class), any(InstanceReference.class), anyString(), anyString(),
				anyMap())).thenReturn(new Pair<Serializable, Serializable>("12523", "1223"));
		Response response = controller.updateRelation(LinkConstants.HAS_FAVOURITE,
				buildRequestData(item, "attachTo").replaceFirst("key1", "0"));
		verify(linkService, Mockito.atLeastOnce()).removeLinkById(anyString());
		assertEquals(response.getStatus(), Status.OK.getStatusCode());
	}

	/**
	 * Builds the data test.
	 */
	public void buildDataTest() {
		callSuperBuildData = true;

		// we must not get nulls here
		JSONArray data = controller.buildData(null, OUTGOING, null, null);
		assertEquals(data.toString(), "[]");

	}

	/**
	 * Null instance id.
	 */
	public void buildData_nullInstanceId_emptyArray() {
		callSuperBuildData = true;
		InstanceReference instance = getMockedInstanceReference("null");
		JSONArray result = controller.buildData(instance, OUTGOING, null, null);
		assertEquals(result.toString(), "[]");
	}

	/**
	 * Null user.
	 */
	public void buildData_nullCurrentUser_emptyArray() {
		callSuperBuildData = true;
		getNullUser = true;
		InstanceReference instance = getMockedInstanceReference("123123");
		JSONArray result = controller.buildData(instance, OUTGOING, null, null);
		assertEquals(result.toString(), "[]");
	}

	/**
	 * Outgoing mode.
	 */
	public void buildData_outgoingMode_emptyArray() {
		InstanceReference instance = prepareBuildDataMocks();
		JSONArray result = controller.buildData(instance, OUTGOING, null, null);
		assertEquals(result.toString(), "[]");
	}

	/**
	 * All mode.
	 */
	public void buildData_allMode_emptyArray() {
		InstanceReference instance = prepareBuildDataMocks();
		JSONArray result = controller.buildData(instance, "all", null, null);
		assertEquals(result.toString(), "[]");
	}

	/**
	 * Ingoing mode.
	 */
	public void buildData_ingoingMode_emptyArray() {
		InstanceReference instance = prepareBuildDataMocks();
		JSONArray result = controller.buildData(instance, "ingoing", null, null);
		assertEquals(result.toString(), "[]");
	}

	private InstanceReference prepareBuildDataMocks() {
		callSuperBuildData = true;
		getNullUser = false;
		LinkReference linkReference = new LinkReference();
		linkReference.setFrom(mock(InstanceReference.class));
		linkReference.setTo(mock(InstanceReference.class));
		when(linkService.getLinks(any(InstanceReference.class))).thenReturn(Arrays.asList(linkReference));
		doNothing().when(instanceLoadDecorator).decorateResult(any());
		return getMockedInstanceReference("123123");
	}

	private static InstanceReference getMockedInstanceReference(String idToReturn) {
		InstanceReference instance = mock(InstanceReference.class);
		when(instance.getIdentifier()).thenReturn(idToReturn);
		return instance;
	}

	/**
	 * Deactivate relation test.
	 */
	public void deactivateRelationTest() {

		Response expectedResponse = controller.deactivateRelation(null, null, null, null);

		// if missing link id
		assertEquals(expectedResponse.getStatus(), Status.BAD_REQUEST.getStatusCode());
		assertNotNull(expectedResponse.getEntity());

		// if link id is provided, we expect OK
		expectedResponse = controller.deactivateRelation("1", null, null, null);
		assertEquals(expectedResponse.getStatus(), Status.OK.getStatusCode());
	}

	// ----------------------- REMOVE RELATION O.O -------------------------------

	/**
	 * Null data.
	 */
	public void removeRelation_nullData_badRequestResponse() {
		Response response = controller.removeRelation(null);
		assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
	}

	/**
	 * Empty data.
	 */
	public void removeRelation_emptyData_badRequestResponse() {
		Response response = controller.removeRelation("");
		assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
	}

	/**
	 * Some data.
	 */
	public void removeRelation_someData_okRequestResponse() {
		Response response = controller.removeRelation("{}");
		assertEquals(response.getStatus(), Status.OK.getStatusCode());
	}

}
