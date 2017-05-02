package com.sirma.itt.emf.instance;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONObject;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.rest.BadRequestException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.notification.NotificationSupport;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.model.DataType;
import com.sirma.itt.seip.model.LinkSourceId;
import com.sirma.itt.seip.testutil.EmfTest;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * Test for InstanceRestService.
 *
 * @author svelikov
 */
@Test
public class InstanceRestServiceTest extends EmfTest {

	private InstanceRestService service;
	private TypeConverter typeConverter;

	private LinkService linkService;
	@SuppressWarnings("rawtypes")
	private InstanceService instanceService;
	private EmfInstance instance;
	private NotificationSupport notificationSupport;
	private String detachRequest;
	private Collection<Instance> instances;
	private EmfInstance owningInstance;
	private SemanticDefinitionService semanticDefinitionService;
	private InstanceTypeResolver instanceTypeResolver;
	private InstanceLoadDecorator instanceLoadDecorator;

	/**
	 * Instantiates a new instance rest service test.
	 */
	@Override
	@BeforeMethod
	public void beforeMethod() {
		super.beforeMethod();
		service = new InstanceRestService() {

			@Override
			public Instance fetchInstance(String instanceId, String instanceType) {
				if (StringUtils.isNullOrEmpty(instanceId) || StringUtils.isNullOrEmpty(instanceType)) {
					return null;
				}
				instance = new EmfInstance();
				instance.setId(instanceId);
				instance.setOwningInstance(owningInstance);
				Map<String, Serializable> properties = new HashMap<>(2);
				instance.setProperties(properties);
				ClassInstance classInstance = new ClassInstance();
				instance.setType(classInstance.type());
				return instance;
			}

			@Override
			public JSONObject convertInstanceToJSON(Instance instance) {
				JSONObject object = new JSONObject();
				JsonUtil.addToJson(object, "dbId", instance.getId());
				JsonUtil.addToJson(object, "type", instance.type().getCategory());
				return object;
			}

			@Override
			public Collection<Instance> loadInstances(List<InstanceReference> itemsForLoad) {
				return instances;
			}

			@Override
			public <T extends Instance> Class<T> getInstanceClass(String type) {
				if (type == null) {
					return null;
				}
				return (Class<T>) EmfInstance.class;
			}
		};

		linkService = Mockito.mock(LinkService.class);
		ReflectionUtils.setField(service, "linkService", linkService);

		LabelProvider labelProvider = Mockito.mock(LabelProvider.class);
		Mockito.when(labelProvider.getValue(anyString())).thenReturn("label from bundle");
		ReflectionUtils.setField(service, "labelProvider", labelProvider);

		instanceService = Mockito.mock(InstanceService.class);
		ReflectionUtils.setField(service, "instanceService", instanceService);

		notificationSupport = Mockito.mock(NotificationSupport.class);
		ReflectionUtils.setField(service, "notificationSupport", notificationSupport);

		semanticDefinitionService = Mockito.mock(SemanticDefinitionService.class);
		ReflectionUtils.setField(service, "semanticDefinitionService", semanticDefinitionService);

		instanceTypeResolver = Mockito.mock(InstanceTypeResolver.class);
		ReflectionUtils.setField(service, "instanceTypeResolver", instanceTypeResolver);
		when(instanceTypeResolver.resolveReference(anyString()))
				.then(a -> new LinkSourceId(a.getArgumentAt(0, String.class), createDataType("documentinstance")));

		typeConverter = createTypeConverter();
		typeConverter.addConverter(String.class, InstanceReference.class, source -> {
			return new LinkSourceId(null, createDataType(source));
		});

		ReflectionUtils.setField(service, "typeConverter", typeConverter);
	}

	private static DataTypeDefinition createDataType(String source) {
		DataType type = new DataType();
		if (source.contains(".")) {
			type.setJavaClassName(source);
			type.setName(type.getJavaClass().getSimpleName().toLowerCase());
		} else {
			type.setName(source);
		}
		return type;
	}

	/**
	 * Test unsuccessful publish due to some back-end error.
	 */
	@SuppressWarnings("unchecked")
	@Test(expectedExceptions = BadRequestException.class)
	public void testUnsuccessfulPublish() {
		Mockito
				.when(instanceService.publish(Matchers.any(EmfInstance.class), Matchers.any(Operation.class)))
					.thenReturn(null);
		service.publishRevision("{ \"instanceId\" : \"123\", \"instanceType\" : \"documentinstance\" }");
	}

	/**
	 * Test publish instance with bad request payload.
	 *
	 * @param payload
	 *            Request payload.
	 */
	@Test(dataProvider = "testPublishRevisionBadRequestProvider", expectedExceptions = BadRequestException.class)
	public void testPublishRevisionBadRequest(String payload) {
		new InstanceRestService().publishRevision(payload);
	}

	/**
	 * Data provider for {@link #testPublishRevisionBadRequest(String)}.
	 *
	 * @return Test data.
	 */
	@DataProvider(name = "testPublishRevisionBadRequestProvider")
	protected Object[][] testPublishRevisionBadRequestProvider() {
		return new Object[][] { { null }, { "" }, { "{}" }, { "{ \"instanceType\" : \"type\" }" } };
	}

	/**
	 * Publish revision test.
	 */
	@SuppressWarnings("unchecked")
	public void publishRevisionTest() {
		// successful publish
		Mockito.when(instanceService.publish(any(EmfInstance.class), any(Operation.class))).thenReturn(instance);
		String request = "{ \"instanceId\" : \"123\", \"instanceType\" : \"documentinstance\" }";
		Response response = service.publishRevision(request);
		assertNotNull(response);
		assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

		request = "{ \"instanceId\" : \"123\" }";
		response = service.publishRevision(request);
		assertNotNull(response);
		assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

		request = "{ \"instanceId\" : \"123\", \"instanceType\" : \"objectinstance\" }";
		response = service.publishRevision(request);
		assertNotNull(response);
		assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
	}

	/**
	 * Test - detach with empty parameters test.
	 */
	public void detachWithEmptyDataOrParameters() {
		// no data passed
		Response response = service.detach(null);
		assertNotNull(response);
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

		String request = "";
		response = service.detach(request);
		assertNotNull(response);
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

		// empty data object passed
		request = "{}";
		response = service.detach(request);
		assertNotNull(response);
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

		// missing target type
		request = "{\"targetId\":\"123\",\"operationId\":\"detachObject\",\"linked\""
				+ ":[{\"instanceId\":\"456\",\"instanceType\":\"objectinstance\"}]}";
		response = service.detach(request);
		assertNotNull(response);
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

		// missing operationId
		request = "{\"targetId\":\"123\",\"targetType\":\"projectinstance\",\"linked\""
				+ ":[{\"instanceId\":\"456\",\"instanceType\":\"objectinstance\"}]}";
		response = service.detach(request);
		assertNotNull(response);
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

		// missing linked instances parameter
		request = "{\"targetId\":\"123\",\"targetType\":\"projectinstance\",\"operationId\":\"detachObject\"}";
		response = service.detach(request);
		assertNotNull(response);
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
	}

	/**
	 * Successful detach test.
	 */
	public void successfulDetachOperation() {
		prepSuccessfulDetach();
		Response response = service.detach(detachRequest);
		assertNotNull(response);
		assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
	}

	/**
	 * Setups instances for testing successful detach.
	 */
	private void prepSuccessfulDetach() {
		EmfInstance parentInstnace = new EmfInstance();
		parentInstnace.setId("testIdentifierInstance1");
		EmfInstance childInstance = new EmfInstance();
		parentInstnace.setId("testIdentifierInstance2");
		linkService.linkSimple(parentInstnace.toReference(), childInstance.toReference(), LinkConstants.HAS_ATTACHMENT,
				LinkConstants.IS_ATTACHED_TO);

		detachRequest = "{\"targetId\":\"" + parentInstnace.getId()
				+ "\",\"targetType\":\"projectinstance\",\"operationId\":\"detachObject\",\"linked\""
				+ ":[{\"instanceId\":\"" + childInstance.getId() + "\",\"instanceType\":\"objectinstance\"}]}";
		instances = Mockito.mock(Collection.class);
		instances.add(childInstance);
	}

	/** Tests removeSemanticLinks when the id is empty. */
	public void removeSemanticLinks_emptyId_returnBadRequest() {
		Response response = service.removeInstanceLinks("", "projectinstance");
		assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
	}

	/** Tests removeSemanticLinks when the type is empty. */
	public void removeSemanticLinks_emptyType_returnBadRequest() {
		Response response = service.removeInstanceLinks("testIdentifier", "");
		assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
	}

	/** Tests removeSemanticLinks when the id is empty. */
	public void removeSemanticLinks_nullId_returnBadRequest() {
		Response response = service.removeInstanceLinks(null, "projectinstance");
		assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
	}

	/** Tests removeSemanticLinks when the type is empty. */
	public void removeSemanticLinks_nullType_returnBadRequest() {
		Response response = service.removeInstanceLinks("testIdentifier", null);
		assertEquals(response.getStatus(), Status.BAD_REQUEST.getStatusCode());
	}

	/**
	 * Tests removeSemanticLinks when the id is persisted.
	 * <p>
	 * <b>!!!</b> The id is not really persisted. The method isIdPersisted(Serializable id) in InstanceUtils will return
	 * <b>true</b> if the id is not null or not tracked. }
	 */
	public void removeSemanticLinks_persistedId_returnOK() {
		Response response = service.removeInstanceLinks("persistedIdentifier", "projectinstance");
		assertEquals(response.getStatus(), Status.OK.getStatusCode());
	}

	/**
	 * Tests removeSemanticLinks when the id is not persisted and haven't links.
	 */
	public void removeSemanticLinks_notPersistedNoneLinks_returnOK() {
		idManager.registerId("emf:8b98033d-f590-46b5-9df2-549d937bbc12");
		Response response = service.removeInstanceLinks("emf:8b98033d-f590-46b5-9df2-549d937bbc12", "projectinstance");
		assertEquals(response.getStatus(), Status.OK.getStatusCode());
	}

	/**
	 * Tests removeSemanticLinks when the id is not persisted and have links.
	 */
	public void removeSemanticLinks_notPersistedHaveLinks_returnOK() {
		idManager.registerId("testIdentifierInstance1");
		InstanceReference reference1 = Mockito.mock(InstanceReference.class);
		reference1.setIdentifier("testIdentifierInstance1");
		InstanceReference reference2 = Mockito.mock(InstanceReference.class);
		reference2.setIdentifier("testIdentifierInstance2");
		linkService.linkSimple(reference1, reference2, LinkConstants.TREE_PARENT_TO_CHILD,
				LinkConstants.TREE_CHILD_TO_PARENT);
		Response response = service.removeInstanceLinks("testIdentifierInstance1", "projectinstance");
		assertEquals(response.getStatus(), Status.OK.getStatusCode());
	}

	/**
	 * Test method for document revisions.
	 */
	public void getRevisionTest() {
		Response response = service.getRevisions(null, null, true);
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

		response = service.getRevisions("", "documentinstance", false);
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

		response = service.getRevisions("instanceId", "", false);
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

		response = service.getRevisions("instanceId", null, false);
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

		response = service.getRevisions(null, "documentinstance", false);
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
	}

	/**
	 * Test method for allowed task.
	 */
	@SuppressWarnings("unchecked")
	public void getAllowedTasks() {
		String response = service.getAllowedTasks("123", "documentinstance");

		Mockito.when(instanceService.getAllowedChildren(instance, "task")).thenReturn(
				new LinkedList<DefinitionModel>());
		JsonAssert.assertJsonEquals(response, "[]");
	}

	/**
	 * Test method for members by instance identifier and instance type.
	 */
	@SuppressWarnings("unchecked")
	public void getOwningInstanceSubTypeTest() {

		owningInstance = new EmfInstance();
		owningInstance.setId("owningInstanceId");

		String rdfType = "http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#Book";
		Map<String, Serializable> properties = new HashMap<>();
		properties.put("type", "GEP10002");
		properties.put("rdf:type", rdfType);

		owningInstance.setProperties(properties);

		ClassInstance clazz = new ClassInstance();
		clazz.setId("chd:Book");
		owningInstance.setType(clazz.type());

		Mockito.when(semanticDefinitionService.getClassInstance(rdfType)).thenReturn(clazz);
		service.getParentInstanceType("instanceId", "documentinstance");

		Response response = service.getParentInstanceType("instanceId", "documentinstance");
		assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

		response = service.getParentInstanceType(null, null);
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

		response = service.getParentInstanceType(null, "documentinstance");
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

		response = service.getParentInstanceType("instanceId", null);
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
	}
}
