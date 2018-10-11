package com.sirma.itt.seip.definition;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.testutil.fakes.EntityLookupCacheContextFake;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;

/**
 * Test for {@link DefinitionServiceImpl}.
 *
 * @author A. Kunchev
 */
public class DefinitionServiceImplTest {

	@InjectMocks
	private DefinitionServiceImpl service = new DefinitionServiceImpl();

	@Mock
	private DatabaseIdManager databaseIdManager;

	@Spy
	private EntityLookupCacheContext cacheContext = EntityLookupCacheContextFake.createNoCache();

	@Mock
	private DefinitionAccessor accessor;

	@Spy
	protected InstanceProxyMock<DefinitionAccessor> accessors = new InstanceProxyMock<>();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		accessors.set(accessor);
		when(accessor.getSupportedObjects())
				.thenReturn(new HashSet<>(Arrays.asList(Object.class, DefinitionModel.class, EmfInstance.class)));
		service.init();
	}

	@Test
	public void getAllDefinitionAsStream() {
		when(accessor.getAllDefinitions()).thenReturn(Arrays.asList(new DefinitionMock()));
		Stream<DefinitionModel> definitions = service.getAllDefinitions();
		assertNotNull(definitions);
		assertEquals(1L, definitions.count());
	}

	@Test
	public void getAllDefinitionByInstanceType() {
		DefinitionMock definition1 = new DefinitionMock();
		PropertyDefinitionMock semanticType = new PropertyDefinitionMock();
		semanticType.setIdentifier(SEMANTIC_TYPE);
		semanticType.setValue("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Case");
		definition1.getFields().add(semanticType);

		DefinitionMock definition2 = new DefinitionMock();
		semanticType = new PropertyDefinitionMock();
		semanticType.setIdentifier(SEMANTIC_TYPE);
		semanticType.setValue("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Project");
		definition2.getFields().add(semanticType);

		when(accessor.getAllDefinitions()).thenReturn(Arrays.asList(definition1, definition2));

		ClassInstance classInstance = new ClassInstance();
		classInstance.setId("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Case");

		Stream<DefinitionModel> definitions = service.getAllDefinitions(classInstance);
		assertNotNull(definitions);
		assertEquals(1L, definitions.count());
	}

	@Test
	public void getAllDefinitionByInstanceType_subTypes() {
		InstanceType instanceType = mock(InstanceType.class);
		DefinitionMock definition = new DefinitionMock();
		PropertyDefinitionMock propertyDefinition = new PropertyDefinitionMock();
		propertyDefinition.setIdentifier(SEMANTIC_TYPE);
		propertyDefinition.setValue("http://www.sirma.com/ontologies/2013/10/culturalHeritageDomain#CulturalObject");
		definition.getFields().add(propertyDefinition);

		when(accessor.getAllDefinitions()).thenReturn(Arrays.asList(definition));

		Stream<DefinitionModel> definitions = service.getAllDefinitions(instanceType);

		assertEquals(0, definitions.count());

		when(instanceType.instanceOf(any(Serializable.class))).thenReturn(Boolean.FALSE);
		when(instanceType.hasSubType(any(Serializable.class))).thenReturn(Boolean.TRUE);

		definitions = service.getAllDefinitions(instanceType);

		assertEquals(1L, definitions.count());
	}

	@Test
	public void getAllDefinitionByCategory() throws Exception {
		DefinitionMock definition1 = new DefinitionMock();
		PropertyDefinitionMock semanticType = new PropertyDefinitionMock();
		semanticType.setIdentifier(SEMANTIC_TYPE);
		semanticType.setValue("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Case");
		definition1.getFields().add(semanticType);

		DefinitionMock definition2 = new DefinitionMock();
		semanticType = new PropertyDefinitionMock();
		semanticType.setIdentifier(SEMANTIC_TYPE);
		semanticType.setValue("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Project");
		definition2.getFields().add(semanticType);

		when(accessor.getAllDefinitions()).thenReturn(Arrays.asList(definition1, definition2));

		ClassInstance classInstance = new ClassInstance();
		classInstance.setId("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Case");

		Stream<DefinitionModel> definitions = service.getAllDefinitions(classInstance);
		assertNotNull(definitions);
		assertEquals(1L, definitions.count());
	}

	// ---------------------------------- getDefinitionIdentifier -------------------------------------

	@Test
	public void getDefinitionIdentifier_nullModel_nullResult() {
		assertNull(service.getDefinitionIdentifier(null));
	}

	@Test
	public void getDefinitionIdentifier_nullDefinitionId_nullResult() {
		assertNull(service.getDefinitionIdentifier(mock(DefinitionModel.class)));
	}

	@Test
	public void getDefinitionIdentifier_nullDefinitionType_nullResult() {
		DefinitionModel model = mock(DefinitionModel.class);
		model.setIdentifier("definitionModel");
		assertNull(service.getDefinitionIdentifier(model));
	}

	@Test
	public void getDefinitionIdentifier_withIdAndType_buildedUniqueIdentifierResult() {
		DefinitionModel model = mock(DefinitionModel.class);
		when(model.getIdentifier()).thenReturn("definitionModel");
		when(model.getType()).thenReturn("definitionModelType");
		when(databaseIdManager.getValidId("definitionModelType-definitionModel"))
				.thenReturn("emf:definitionModelType-definitionModel");
		assertEquals("emf:definitionModelType-definitionModel", service.getDefinitionIdentifier(model));
	}

	@Test(expected = EmfRuntimeException.class)
	public void test_getDefaultDefinitionId_nullInstance() throws Exception {
		service.getDefaultDefinitionId(null);
	}

	@Test
	public void getDefaultDefinitionId() {
		when(accessor.getDefaultDefinitionId(any())).thenReturn("defaultDefinition");

		String definitionId = service.getDefaultDefinitionId(new EmfInstance());
		assertEquals("defaultDefinition", definitionId);
	}

	@Test
	public void getInstanceObjectProperties_nullModel_emptyStream() {
		when(accessor.getDefinition(any(Instance.class))).thenReturn(null);
		Stream<PropertyDefinition> result = service.getInstanceObjectProperties(new EmfInstance());
		assertEquals(0, result.count());
	}

	@Test
	public void getInstanceObjectProperties_withModelWithObjectProperty_notEmptyStream() {
		PropertyDefinitionMock objectProperty = new PropertyDefinitionMock();
		objectProperty.setName("objectProperty");
		objectProperty.setDataType(new DataTypeDefinitionMock(DataTypeDefinition.URI));

		PropertyDefinitionMock dataProperty = new PropertyDefinitionMock();
		dataProperty.setName("dataProperty");
		dataProperty.setDataType(new DataTypeDefinitionMock(DataTypeDefinition.TEXT));

		DefinitionMock definition = new DefinitionMock();
		definition.setFields(Arrays.asList(objectProperty, dataProperty));
		when(accessor.getDefinition(any(Instance.class))).thenReturn(definition);
		Stream<PropertyDefinition> result = service.getInstanceObjectProperties(new EmfInstance());
		assertEquals(1, result.count());
	}

}
