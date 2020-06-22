package com.sirma.itt.emf.instance;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.model.DataType;
import com.sirma.itt.seip.model.LinkSourceId;
import com.sirma.itt.seip.testutil.EmfTest;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.util.ReflectionUtils;

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
	private InstanceService instanceService;
	private Instance instance;
	private Collection<Instance> instances;
	private Instance owningInstance;
	private SemanticDefinitionService semanticDefinitionService;
	private InstanceTypeResolver instanceTypeResolver;

	/**
	 * Instantiates a new instance rest service test.
	 */
	@Override
	@BeforeMethod
	public void beforeMethod() {
		super.beforeMethod();

		instance = InstanceReferenceMock.createGeneric("instance").toInstance();
		owningInstance = InstanceReferenceMock.createGeneric("owning").toInstance();
		contextService.bindContext(instance, owningInstance);
		Map<String, Serializable> properties = new HashMap<>(2);
		instance.setProperties(properties);
		ClassInstance classInstance = new ClassInstance();
		instance.setType(classInstance.type());
		service = new InstanceRestService() {

			@Override
			public Instance fetchInstance(String instanceId) {
				if (StringUtils.isBlank(instanceId)) {
					return null;
				}
				return instance;
			}

			@Override
			public <T extends Instance> Class<T> getInstanceClass(String type) {
				if (type == null) {
					return null;
				}
				return (Class<T>) EmfInstance.class;
			}
		};
		ReflectionUtils.setFieldValue(service, "contextService", contextService);

		linkService = Mockito.mock(LinkService.class);
		ReflectionUtils.setFieldValue(service, "linkService", linkService);

		LabelProvider labelProvider = Mockito.mock(LabelProvider.class);
		Mockito.when(labelProvider.getValue(anyString())).thenReturn("label from bundle");
		ReflectionUtils.setFieldValue(service, "labelProvider", labelProvider);

		instanceService = Mockito.mock(InstanceService.class);
		ReflectionUtils.setFieldValue(service, "instanceService", instanceService);

		semanticDefinitionService = Mockito.mock(SemanticDefinitionService.class);
		ReflectionUtils.setFieldValue(service, "semanticDefinitionService", semanticDefinitionService);

		instanceTypeResolver = Mockito.mock(InstanceTypeResolver.class);
		ReflectionUtils.setFieldValue(service, "instanceTypeResolver", instanceTypeResolver);
		when(instanceTypeResolver.resolveReference(anyString())).then(a -> Optional
				.of(new LinkSourceId(a.getArgumentAt(0, String.class), createDataType("documentinstance"))));

		typeConverter = createTypeConverter();
		typeConverter.addConverter(String.class, InstanceReference.class, source -> {
			return new LinkSourceId(null, createDataType(source));
		});

		ReflectionUtils.setFieldValue(service, "typeConverter", typeConverter);
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
	 * Test method for document revisions.
	 */
	public void getRevisionTest() {
		Response response = service.getRevisions(null, true);
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

		response = service.getRevisions("", false);
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

		response = service.getRevisions(null, false);
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
	}

	/**
	 * Test method for members by instance identifier and instance type.
	 */
	public void getOwningInstanceSubTypeTest() {

		String rdfType = "http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#Book";
		owningInstance.add("type", "GEP10002");
		owningInstance.add(DefaultProperties.SEMANTIC_TYPE, rdfType);

		ClassInstance clazz = new ClassInstance();
		clazz.setId("chd:Book");
		clazz.setProperties(new HashMap<>());
		owningInstance.setType(clazz.type());

		Mockito.when(semanticDefinitionService.getClassInstance(rdfType)).thenReturn(clazz);

		Response response = service.getParentInstanceType("instanceId");
		assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

		response = service.getParentInstanceType(null);
		assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());

	}
}
