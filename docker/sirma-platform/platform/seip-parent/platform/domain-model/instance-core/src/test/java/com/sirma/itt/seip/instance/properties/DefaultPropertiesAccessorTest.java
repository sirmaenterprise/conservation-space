/**
 *
 */
package com.sirma.itt.seip.instance.properties;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.WritablePropertyDefinition;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PrototypeDefinition;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.properties.entity.NodePropertyHelper;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;

/**
 * @author BBonev
 */
public class DefaultPropertiesAccessorTest {

	@InjectMocks
	DefaultPropertiesAccessor accessor;
	@Mock
	protected DbDao dbDao;
	@Mock
	protected DefinitionService definitionService;
	@Mock
	private EntityLookupCacheContext cacheContext;
	@Mock
	private NodePropertyHelper nodePropertyHelper;
	@Mock
	PersistentPropertiesExtension persistentExtension;
	@Spy
	private List<PersistentPropertiesExtension> persistentProperties = new ArrayList<>();

	@Mock
	RelationalNonPersistentPropertiesExtension nonPersistentExtension;
	@Spy
	List<RelationalNonPersistentPropertiesExtension> nonPersistentProperties = new ArrayList<>();

	@Mock
	private EventService eventService;

	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		TypeConverter typeConverter = mock(TypeConverter.class);
		when(typeConverter.convert(any(Class.class), anyString())).then(a -> a.getArgumentAt(1, String.class));
		TypeConverterUtil.setTypeConverter(typeConverter);

		persistentProperties.clear();
		persistentProperties.add(persistentExtension);
		nonPersistentProperties.clear();
		nonPersistentProperties.add(nonPersistentExtension);

		when(persistentExtension.getPersistentProperties()).thenReturn(new HashSet<>(Arrays.asList("prop1", "prop2")));
		when(nonPersistentExtension.getNonPersistentProperties())
				.thenReturn(new HashSet<>(Arrays.asList("prop3", "prop4")));

		accessor.initialize();
	}

	@Test
	public void test_CorrentInit() {
		Set<String> nonPersisted = accessor.getNonPersistentProperties();
		assertNotNull(nonPersisted);
		assertTrue(nonPersisted.contains("prop3"));
		assertTrue(nonPersisted.contains("prop4"));
	}

	@Test
	public void test_createPropertiesEntity() {
		assertNotNull(accessor.createPropertiesEntity());
	}

	@Test
	public void test_createPropertiesValue() {
		PropertyModelValue propertiesValue = accessor.createPropertiesValue(DataTypeDefinition.TEXT, "value");
		assertNotNull(propertiesValue);
		assertEquals(propertiesValue.getValue(DataTypeDefinition.TEXT), "value");
	}

	@Test
	public void test_filterOutForbiddenProperties() {
		Set<String> properties = new HashSet<>(Arrays.asList("prop1", "prop2", "prop3", "prop4", "prop5"));
		accessor.filterOutForbiddenProperties(properties);
		assertFalse(properties.contains("prop3"));
		assertFalse(properties.contains("prop4"));
		assertTrue(properties.contains("prop1"));
		assertTrue(properties.contains("prop2"));
		assertTrue(properties.contains("prop5"));
	}

	@Test
	public void test_notifyForChanges() {
		EmfInstance model = new EmfInstance();
		model.add("prop", "value");
		accessor.notifyForChanges(model, new HashMap<>(), new HashMap<>(), new HashMap<>());
		verify(eventService).fire(any(PropertiesChangeEvent.class));
		assertTrue(model.isPropertyPresent("prop"));
	}

	@Test
	public void test_getPropertyPrototype_fromDef() {

		DefinitionModel definitionModel = new DefinitionMock();
		definitionModel.setIdentifier("definitionId");
		WritablePropertyDefinition field = new PropertyDefinitionMock();
		definitionModel.getFields().add(field);
		field.setIdentifier("prop");
		field.setParentPath("definitionId");
		PathElement pathElement = mock(PathElement.class);
		when(pathElement.getPath()).thenReturn("definitionId");
		when(pathElement.getIdentifier()).thenReturn("definitionId");
		PrototypeDefinition prototype = accessor.getPropertyPrototype("prop", "value", pathElement, definitionModel);
		assertNotNull(prototype);
	}

	@Test
	public void test_getPropertyPrototype_AlwaysPersisted() {

		DefinitionModel definitionModel = new DefinitionMock();
		definitionModel.setIdentifier("definitionId");

		PathElement pathElement = mock(PathElement.class);
		when(pathElement.getPath()).thenReturn("definitionId");
		when(pathElement.getIdentifier()).thenReturn("definitionId");

		when(definitionService.getDefinitionByValue(eq("prop1"), eq("value")))
				.thenReturn(mock(PrototypeDefinition.class));
		PrototypeDefinition prototype = accessor.getPropertyPrototype("prop1", "value", pathElement, definitionModel);
		assertNotNull(prototype);
	}

	@Test
	public void test_getPropertyPrototype_savePropsWithoutDef() {

		DefinitionModel definitionModel = new DefinitionMock();
		definitionModel.setIdentifier("definitionId");

		PathElement pathElement = mock(PathElement.class);
		when(pathElement.getPath()).thenReturn("definitionId");
		when(pathElement.getIdentifier()).thenReturn("definitionId");

		when(definitionService.getDefinitionByValue(eq("prop"), eq("value")))
				.thenReturn(mock(PrototypeDefinition.class));
		try {
			Options.SAVE_PROPERTIES_WITHOUT_DEFINITION.enable();
			PrototypeDefinition prototype = accessor.getPropertyPrototype("prop", "value", pathElement, definitionModel);
			assertNotNull(prototype);
		} finally {
			Options.SAVE_PROPERTIES_WITHOUT_DEFINITION.disable();
		}
	}

}
