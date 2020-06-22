package com.sirma.sep.instance.relations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.relation.InstanceRelationsService;
import com.sirma.itt.seip.permissions.InstanceAccessEvaluator;
import com.sirma.itt.seip.permissions.InstanceAccessPermissions;
import com.sirma.itt.seip.permissions.SecurityModel;
import com.sirma.itt.seip.search.ResultItemTransformer;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.search.SimpleResultItem;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Test for {@link InstanceRelationsServiceImpl}.
 *
 * @author A. Kunchev
 */
public class InstanceRelationsServiceImplTest {

	private static final String PROPERTY = "property";
	private static final String INSTANCE = "instance";

	@InjectMocks
	private InstanceRelationsService service;

	@Mock
	private ConfigurationProperty<Integer> relationsInitialLoadLimit;

	@Mock
	private DefinitionService definitionService;

	@Mock
	private InstanceAccessEvaluator instanceAccessEvaluator;

	@Mock
	private SearchService searchService;

	@Mock
	private NamespaceRegistryService registryService;

	@Mock
	private InstanceTypeResolver instanceTypeResolver;
	@Spy
	private InstancePropertyNameResolver fieldConverter = InstancePropertyNameResolver.NO_OP_INSTANCE;

	@Before
	public void setup() {
		service = new InstanceRelationsServiceImpl();
		MockitoAnnotations.initMocks(this);

		when(relationsInitialLoadLimit.get()).thenReturn(5);
		TypeConverter typeConverter = mock(TypeConverter.class);
		when(typeConverter.tryConvert(any(), any())).thenAnswer(AdditionalAnswers.returnsSecondArg());
		TypeConverterUtil.setTypeConverter(typeConverter);

		when(instanceTypeResolver.resolveReference(any(Serializable.class)))
				.thenReturn(Optional.of(InstanceReferenceMock.createGeneric("w/e")));
	}

	@Test(expected = NullPointerException.class)
	public void evaluateRelations_nullInstance() {
		service.evaluateRelations(null, "prop");
	}

	@Test(expected = NullPointerException.class)
	public void evaluateRelations_nullPropertyIdentifier() {
		service.evaluateRelations(new EmfInstance(), (String) null);
	}

	@Test(expected = EmfRuntimeException.class)
	public void evaluateRelations_propertyWithoutUri() {
		final String PROPERTY_IDENTIFIER = "prop";
		Instance instance = new EmfInstance();
		DefinitionMock definition = stubDefinition(PROPERTY_IDENTIFIER, null);
		when(definitionService.getInstanceDefinition(instance)).thenReturn(definition);
		service.evaluateRelations(instance, PROPERTY_IDENTIFIER);
	}

	private static DefinitionMock stubDefinition(String propertyIdentifier, String uri) {
		DefinitionMock definition = new DefinitionMock();
		PropertyDefinitionMock propertyDefinition = new PropertyDefinitionMock();
		propertyDefinition.setDataType(new DataTypeDefinitionMock(DataTypeDefinition.URI));
		propertyDefinition.setUri(uri);
		propertyDefinition.setName(propertyIdentifier);
		definition.setFields(Collections.singletonList(propertyDefinition));
		return definition;
	}

	@Test
	public void evaluateRelations_lockedByProperty() {
		final String PROPERTY_IDENTIFIER = "lockedBy";
		Instance instance = new EmfInstance("instance-id");
		instance.add(PROPERTY_IDENTIFIER, (Serializable) Arrays.asList("id-1"));
		DefinitionMock definition = stubDefinition(PROPERTY_IDENTIFIER, null);
		when(definitionService.getInstanceDefinition(instance)).thenReturn(definition);
		Map<Serializable, InstanceAccessPermissions> roles = new HashMap<>(2);
		roles.put("id-1", InstanceAccessPermissions.CAN_READ);
		when(instanceAccessEvaluator.isAtLeastRole(anyCollection(), eq(SecurityModel.BaseRoles.VIEWER),
				eq(SecurityModel.BaseRoles.MANAGER))).thenReturn(roles);
		List<String> result = service.evaluateRelations(instance, PROPERTY_IDENTIFIER);
		assertEquals(1, result.size());
	}

	@Test
	public void evaluateRelations_systemAndNotSystemProperties() {
		final String LOCKED_BY = "lockedBy";
		final String REFERENCES = "emf:references";
		Instance instance = new EmfInstance("instance-id");
		instance.add(LOCKED_BY, (Serializable) Collections.singleton("id-1"));
		DefinitionMock definition = stubDefinition(Collections.singleton(REFERENCES));
		definition.getFields().addAll(stubDefinition(LOCKED_BY, null).getFields());
		when(definitionService.getInstanceDefinition(instance)).thenReturn(definition);

		Map<Serializable, InstanceAccessPermissions> roles = new HashMap<>(2);
		roles.put("id-1", InstanceAccessPermissions.CAN_READ);
		when(instanceAccessEvaluator.isAtLeastRole(anyCollection(), eq(SecurityModel.BaseRoles.VIEWER),
				eq(SecurityModel.BaseRoles.MANAGER))).thenReturn(roles);
		when(registryService.getShortUri(anyString())).thenAnswer(AdditionalAnswers.returnsFirstArg());
		when(searchService.stream(any(), eq(ResultItemTransformer.asIs()))).thenReturn(
				Stream.of(SimpleResultItem.create().add(PROPERTY, REFERENCES).add(INSTANCE, "instance-id-1"),
						SimpleResultItem.create().add(PROPERTY, REFERENCES).add(INSTANCE, "instance-id-2")));

		Map<String, List<String>> result = service.evaluateRelations(instance, Arrays.asList(LOCKED_BY, REFERENCES));
		assertEquals(2, result.size());
	}

	@Test
	public void evaluateRelations_versionInstance() {
		final String PROPERTY_IDENTIFIER = "emf:references";
		Instance instance = new EmfInstance("version-instance-v1.0");
		instance.add(PROPERTY_IDENTIFIER, (Serializable) Arrays.asList("version-id-1-v1.5", "version-id-2-v1.45"));
		DefinitionMock definition = stubDefinition(PROPERTY_IDENTIFIER, PROPERTY_IDENTIFIER);
		when(definitionService.getInstanceDefinition(instance)).thenReturn(definition);
		Map<Serializable, InstanceAccessPermissions> roles = new HashMap<>(2);
		roles.put("version-id-1", InstanceAccessPermissions.CAN_READ);
		roles.put("version-id-2", InstanceAccessPermissions.CAN_READ);
		when(instanceAccessEvaluator.isAtLeastRole(anyCollection(), eq(SecurityModel.BaseRoles.VIEWER),
				eq(SecurityModel.BaseRoles.MANAGER))).thenReturn(roles);
		List<String> result = service.evaluateRelations(instance, PROPERTY_IDENTIFIER, 0, 5);
		assertNotNull(result);
		assertEquals(2, result.size());
		verifyZeroInteractions(searchService);
	}

	@Test
	public void evaluateRelations_mainFlow_singleProperty_searchServiceInvoked() {
		final String PROPERTY_IDENTIFIER = "emf:references";
		Instance instance = new EmfInstance("instance-id");
		DefinitionMock definition = stubDefinition(PROPERTY_IDENTIFIER, PROPERTY_IDENTIFIER);
		when(definitionService.getInstanceDefinition(instance)).thenReturn(definition);
		when(registryService.getShortUri(anyString())).thenAnswer(AdditionalAnswers.returnsFirstArg());
		when(searchService.stream(any(), eq(ResultItemTransformer.asIs()))).thenReturn(
				Stream.of(SimpleResultItem.create().add(PROPERTY, PROPERTY_IDENTIFIER).add(INSTANCE, "instance-id-1"),
						SimpleResultItem.create().add(PROPERTY, PROPERTY_IDENTIFIER).add(INSTANCE, "instance-id-2")));
		List<String> result = service.evaluateRelations(instance, PROPERTY_IDENTIFIER, 0, 3);
		assertNotNull(result);
		assertEquals(2, result.size());
		verify(searchService).stream(any(), eq(ResultItemTransformer.asIs()));
	}

	@Test
	public void evaluateRelations_singleProperty_noPropertiesValues() {
		final String PROPERTY_IDENTIFIER = "emf:references";
		Instance instance = new EmfInstance("instance-id");
		DefinitionMock definition = stubDefinition(PROPERTY_IDENTIFIER, PROPERTY_IDENTIFIER);
		when(definitionService.getInstanceDefinition(instance)).thenReturn(definition);
		when(registryService.getShortUri(anyString())).thenAnswer(AdditionalAnswers.returnsFirstArg());
		when(searchService.stream(any(), eq(ResultItemTransformer.asIs()))).thenReturn(Stream.empty());
		List<String> result = service.evaluateRelations(instance, PROPERTY_IDENTIFIER, 0, 3);
		assertNotNull(result);
		assertTrue(result.isEmpty());
		verify(searchService).stream(any(), eq(ResultItemTransformer.asIs()));
	}

	@Test
	public void evaluateRelations_mainFlow_multipleProperties_searchServiceInvoked() {
		final String REFERENCES = "emf:references";
		final String HAS_CHILD = "emf:hasChild";
		List<String> propertyIds = Arrays.asList(REFERENCES, HAS_CHILD);
		Instance instance = new EmfInstance("instance-id");
		DefinitionMock definition = stubDefinition(propertyIds);
		when(definitionService.getInstanceDefinition(instance)).thenReturn(definition);
		when(registryService.getShortUri(anyString())).thenAnswer(AdditionalAnswers.returnsFirstArg());
		when(searchService.stream(any(), eq(ResultItemTransformer.asIs()))).thenReturn(
				Stream.of(SimpleResultItem.create().add(PROPERTY, REFERENCES).add(INSTANCE, "instance-id-1"),
						SimpleResultItem.create().add(PROPERTY, REFERENCES).add(INSTANCE, "instance-id-2"),
						SimpleResultItem.create().add(PROPERTY, HAS_CHILD).add(INSTANCE, "instance-id-2")));
		Map<String, List<String>> resultMap = service.evaluateRelations(instance, propertyIds);
		assertNotNull(resultMap);
		assertEquals(2, resultMap.size());
		assertEquals(2, resultMap.get(REFERENCES).size());
		assertEquals(1, resultMap.get(HAS_CHILD).size());
		verify(searchService).stream(any(), eq(ResultItemTransformer.asIs()));
	}

	private static DefinitionMock stubDefinition(Collection<String> properties) {
		DefinitionMock definition = new DefinitionMock();
		definition.setFields(new ArrayList<>(properties.size()));
		properties.forEach(prop -> {
			PropertyDefinitionMock propertyDefinition = new PropertyDefinitionMock();
			propertyDefinition.setDataType(new DataTypeDefinitionMock(DataTypeDefinition.URI));
			propertyDefinition.setUri(prop);
			propertyDefinition.setName(prop);
			definition.getFields().add(propertyDefinition);
		});
		return definition;
	}

	@Test
	public void getDefaultLimitPerInstanceProperty() {
		assertEquals(5, service.getDefaultLimitPerInstanceProperty());
	}

	@Test
	public void evaluateRelations_justCreatedInstance_shouldReturnDefaulValuesWithoutSearchCall() {
		final String PROPERTY_IDENTIFIER = "emf:references";
		Instance instance = new EmfInstance("instance-id");
		instance.add(PROPERTY_IDENTIFIER, (Serializable) Collections.singleton("user-id"));
		when(instanceTypeResolver.resolveReference(instance.getId())).thenReturn(Optional.empty());
		Map<Serializable, InstanceAccessPermissions> roles = new HashMap<>(2);
		roles.put("user-id", InstanceAccessPermissions.CAN_READ);
		when(instanceAccessEvaluator.isAtLeastRole(anyCollection(), eq(SecurityModel.BaseRoles.VIEWER),
				eq(SecurityModel.BaseRoles.MANAGER))).thenReturn(roles);
		List<String> result = service.evaluateRelations(instance, PROPERTY_IDENTIFIER, 0, 3);
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertEquals("user-id", result.iterator().next());
		verifyZeroInteractions(searchService, definitionService, registryService);
	}
}
