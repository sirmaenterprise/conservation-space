package com.sirma.itt.seip.instance;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.definition.TypeMappingProvider;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;

/**
 * Test for {@link SemanticInstanceTypes}
 *
 * @author BBonev
 */
public class SemanticInstanceTypesTest {

	@InjectMocks
	private SemanticInstanceTypes instanceTypes;

	@Mock
	private SemanticDefinitionService semanticDefinitionService;
	@Mock
	private InstanceTypeResolver typeResolver;
	@Spy
	private InstanceProxyMock<InstanceTypeResolver> instanceTypeResolver = new InstanceProxyMock<>();
	@Mock
	private TypeConverter typeConverter;
	@Mock
	private TypeMappingProvider typeMapping;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		instanceTypeResolver.set(typeResolver);
		when(semanticDefinitionService.getClassInstance("emf:Case"))
				.then(a -> create(a.getArgumentAt(0, String.class), "case"));
		when(semanticDefinitionService.getClassInstance("emf:Document"))
				.then(a -> create(a.getArgumentAt(0, String.class), "document"));

		when(typeConverter.tryConvert(eq(String.class), any()))
				.then(a -> Objects.toString(a.getArgumentAt(1, Object.class), null));
		when(typeMapping.getDataTypeName(anyString())).then(a -> a.getArgumentAt(0, String.class));

		when(typeResolver.resolve(any(Serializable.class))).then(a -> Optional.of(create("emf:Case", "case").type()));
		when(typeResolver.resolve(anyCollection())).then(a -> {
			Collection<Serializable> collection = a.getArgumentAt(0, Collection.class);
			Map<Serializable, InstanceType> result = new HashMap<>();
			for (Serializable object : collection) {
				result.put(object, create("emf:Case", "case").type());
			}
			return result;
		});
	}

	@Test
	public void fromInvalidData() throws Exception {
		Optional<InstanceType> type = instanceTypes.from((Serializable) null);
		assertNotNull(type);
		assertFalse(type.isPresent());
		type = instanceTypes.from((Instance) null);
		assertNotNull(type);
		assertFalse(type.isPresent());
		type = instanceTypes.from((InstanceReference) null);
		assertNotNull(type);
		assertFalse(type.isPresent());
		type = instanceTypes.from((DefinitionModel) null);
		assertNotNull(type);
		assertFalse(type.isPresent());
	}

	@Test
	public void fromSerializable() throws Exception {
		Optional<InstanceType> type = instanceTypes.from("emf:Case");
		assertNotNull(type);
		assertTrue(type.isPresent());
		assertEquals("emf:Case", type.get().getId());
	}

	@Test
	public void fromInstance() throws Exception {
		EmfInstance instance = new EmfInstance();
		instance.add(SEMANTIC_TYPE, "emf:Case");
		Optional<InstanceType> type = instanceTypes.from(instance);
		assertNotNull(type);
		assertTrue(type.isPresent());
		assertNotNull("The Instance.type() should be filled after resolution", instance.type());

		type = instanceTypes.from(instance);
		assertNotNull(type);
		assertTrue(type.isPresent());
		assertNotNull("The Instance.type() should be filled after refresh", instance.type());
	}

	@Test
	public void fromInstanceReference() throws Exception {
		InstanceReference reference = new InstanceReferenceMock("emf:instanceId", EmfInstance.class);

		Optional<InstanceType> type = instanceTypes.from(reference);
		assertNotNull(type);
		assertTrue(type.isPresent());
		assertNotNull("The InstanceReference.getType() should be filled after resolution", reference.getType());

		type = instanceTypes.from(reference);
		assertNotNull(type);
		assertTrue(type.isPresent());
		assertNotNull("The InstanceReference.getType() should be filled after refresh", reference.getType());
	}

	@Test
	public void fromDefinitionModel() throws Exception {
		DefinitionModel model = new DefinitionMock();
		PropertyDefinitionMock mock = new PropertyDefinitionMock();
		mock.setName(SEMANTIC_TYPE);
		mock.setValue("emf:Case");
		model.getFields().add(mock);

		Optional<InstanceType> type = instanceTypes.from(model);
		assertNotNull(type);
		assertTrue(type.isPresent());
	}

	@Test
	public void from_Should_ResolveType_ForAllOthers() {
		String allOthersId = "sec:SYSTEM_ALL_OTHER_USERS";
		Optional<InstanceType> type = instanceTypes.from(allOthersId);
		assertNotNull(type);
		assertTrue(type.isPresent());
	}

	@Test
	public void resolveTypes() throws Exception {
		EmfInstance instance1 = new EmfInstance();
		instance1.setId("emf:instance1");
		instance1.setType(create("emf:Case", "case").type());
		EmfInstance instance2 = new EmfInstance();
		instance2.setId("emf:instance2");
		instance2.add(SEMANTIC_TYPE, "emf:Case");
		EmfInstance instance3 = new EmfInstance();
		instance3.setId("emf:instance3");

		instanceTypes.resolveTypes(null);
		instanceTypes.resolveTypes(Collections.emptyList());

		List<EmfInstance> instances = Arrays.asList(instance1, instance2, instance3);
		instanceTypes.resolveTypes(instances);

		verify(typeResolver).resolve(
				argThat(CustomMatcher.of((Collection<Serializable> c) -> c.size() == 1 && c.contains("emf:instance3"),
						"Should have only one element and it should be emf:instance3")));

		for (EmfInstance instance : instances) {
			assertNotNull(instance.type());
		}
	}

	@Test
	public void forCategory() throws Exception {
		when(semanticDefinitionService.getClasses())
				.thenReturn(Arrays.asList(create("emf:Case", "case"), create("emf:Document", "document")));

		Collection<InstanceType> forCategory = instanceTypes.forCategory(null);
		assertNotNull(forCategory);
		assertTrue(forCategory.isEmpty());

		forCategory = instanceTypes.forCategory("case");
		assertNotNull(forCategory);
		assertFalse(forCategory.isEmpty());
		assertEquals("emf:Case", forCategory.iterator().next().getId());
	}

	@Test
	public void is() throws Exception {
		InstanceReference reference = new InstanceReferenceMock("emf:instance-Id", EmfInstance.class);
		assertTrue(instanceTypes.is(reference, "emf:Case"));
		assertFalse(instanceTypes.is(reference, "emf:Document"));
		assertFalse(instanceTypes.is(reference, null));
	}

	@Test
	public void hasTrait() throws Exception {
		InstanceReference reference = new InstanceReferenceMock("emf:instance-Id", EmfInstance.class);
		assertTrue(instanceTypes.hasTrait(reference, "searchable"));
		assertFalse(instanceTypes.hasTrait(reference, "createable"));
		assertFalse(instanceTypes.hasTrait(reference, null));
	}

	private static ClassInstance create(String id, String category) {
		ClassInstance classInstance = new ClassInstance();
		classInstance.add("searchable", Boolean.TRUE);
		classInstance.setId(id);
		classInstance.setCategory(category);
		return classInstance;
	}
}
