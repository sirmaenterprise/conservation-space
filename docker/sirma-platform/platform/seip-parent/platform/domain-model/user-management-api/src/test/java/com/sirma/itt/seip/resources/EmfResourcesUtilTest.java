package com.sirma.itt.seip.resources;

import static com.sirma.itt.seip.resources.ResourceProperties.GROUP_PREFIX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.Test;

import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.model.vocabulary.Security;

/**
 * Test for {@link EmfResourcesUtil}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 20/09/2017
 */
public class EmfResourcesUtilTest {

	@Test
	public void extractExternalProperties() throws Exception {
		Instance oldValue = new EmfInstance();
		oldValue.add("prop3", "value3");
		oldValue.add("prop4", "value3");
		oldValue.add("prop5", "value3");
		Instance newValue = new EmfInstance();
		newValue.add("prop3", "value33");
		newValue.add("prop4", "value4");
		newValue.add("prop7", "value7");
		DefinitionModel model = mock(DefinitionModel.class);
		when(model.getIdentifier()).thenReturn("userDefinition");
		when(model.fieldsStream()).then(a -> Stream.of(objectProperty("prop1", true),
				objectProperty("prop2", false),
				dataProperty("prop3", false),
				dataProperty("prop4", true),
				dataProperty("prop5", true),
				dataProperty("prop6", true)));

		Map<String, Serializable> properties = EmfResourcesUtil.extractExternalProperties(oldValue, newValue,
				i -> model);
		Map<String, Serializable> expected = new HashMap<>();
		expected.put("prop3", "value33");
		expected.put("prop4", "value4");

		assertEquals(expected, properties);
	}

	@Test
	public void getComparatorForSynchronization_shouldDetectNewValuesInTrackedFields() throws Exception {
		Instance oldValue = new EmfInstance();
		Instance newValue = new EmfInstance();
		newValue.add("prop3", "value33");
		DefinitionModel model = mock(DefinitionModel.class);
		when(model.getIdentifier()).thenReturn("userDefinition");
		when(model.fieldsStream()).then(a -> Stream.of(objectProperty("prop1", true),
				objectProperty("prop2", false),
				dataProperty("prop3", false),
				dataProperty("prop4", true),
				dataProperty("prop5", true),
				dataProperty("prop6", true)));

		BiPredicate<Instance, Instance> comparator = EmfResourcesUtil.getComparatorForSynchronization(
				i -> model, Function.identity());
		assertFalse(comparator.test(oldValue, newValue));
	}

	@Test
	public void getComparatorForSynchronization_shouldDetectChangedValuesInTrackedFields() throws Exception {
		Instance oldValue = new EmfInstance();
		oldValue.add("prop3", "value3");
		Instance newValue = new EmfInstance();
		newValue.add("prop3", "value33");
		DefinitionModel model = mock(DefinitionModel.class);
		when(model.getIdentifier()).thenReturn("userDefinition");
		when(model.fieldsStream()).then(a -> Stream.of(objectProperty("prop1", true),
				objectProperty("prop2", false),
				dataProperty("prop3", false),
				dataProperty("prop4", true),
				dataProperty("prop5", true),
				dataProperty("prop6", true)));

		BiPredicate<Instance, Instance> comparator = EmfResourcesUtil.getComparatorForSynchronization(
				i -> model, Function.identity());
		assertFalse(comparator.test(oldValue, newValue));
	}

	@Test
	public void getComparatorForSynchronization_shouldDetectRemovedValuesInTrackedFields() throws Exception {
		Instance oldValue = new EmfInstance();
		oldValue.add("prop3", "value3");
		Instance newValue = new EmfInstance();
		DefinitionModel model = mock(DefinitionModel.class);
		when(model.getIdentifier()).thenReturn("userDefinition");
		when(model.fieldsStream()).then(a -> Stream.of(objectProperty("prop1", true),
				objectProperty("prop2", false),
				dataProperty("prop3", false),
				dataProperty("prop4", true),
				dataProperty("prop5", true),
				dataProperty("prop6", true)));

		BiPredicate<Instance, Instance> comparator = EmfResourcesUtil.getComparatorForSynchronization(
				i -> model, Function.identity());
		assertFalse(comparator.test(oldValue, newValue));
	}

	@Test
	public void getComparatorForSynchronization_shouldNotDetectChangesInTrackedFields() throws Exception {
		Instance oldValue = new EmfInstance();
		oldValue.add("prop3", "value3");
		Instance newValue = new EmfInstance();
		newValue.add("prop3", "value3");
		DefinitionModel model = mock(DefinitionModel.class);
		when(model.getIdentifier()).thenReturn("userDefinition");
		when(model.fieldsStream()).then(a -> Stream.of(objectProperty("prop1", true),
				objectProperty("prop2", false),
				dataProperty("prop3", false),
				dataProperty("prop4", true),
				dataProperty("prop5", true),
				dataProperty("prop6", true)));

		BiPredicate<Instance, Instance> comparator = EmfResourcesUtil.getComparatorForSynchronization(
				i -> model, Function.identity());
		assertTrue(comparator.test(oldValue, newValue));
	}

	@Test
	public void getComparatorForSynchronization_shouldNotDetectChangesInNonTrackedFields() throws Exception {
		Instance oldValue = new EmfInstance();
		oldValue.add("prop1", "value3");
		oldValue.add("prop7", "value2");
		Instance newValue = new EmfInstance();
		newValue.add("prop1", "value322");
		newValue.add("prop7", "value23");
		DefinitionModel model = mock(DefinitionModel.class);
		when(model.getIdentifier()).thenReturn("userDefinition");
		when(model.fieldsStream()).then(a -> Stream.of(objectProperty("prop1", true),
				objectProperty("prop2", false),
				dataProperty("prop3", false),
				dataProperty("prop4", true),
				dataProperty("prop5", true),
				dataProperty("prop6", true)));

		BiPredicate<Instance, Instance> comparator = EmfResourcesUtil.getComparatorForSynchronization(
				i -> model, Function.identity());
		assertTrue(comparator.test(oldValue, newValue));
	}

	@Test
	public void cleanGroupId_shouldNotModfiyId_when_itsEmpty() {
		assertNull(EmfResourcesUtil.cleanGroupId(null));
		assertEquals("", EmfResourcesUtil.cleanGroupId(""));
	}

	@Test
	public void cleanGroupId_shouldNotModfiyId_when_hasNoPrefix() {
		assertEquals("SystemAdministrators", EmfResourcesUtil.cleanGroupId("SystemAdministrators"));
	}

	@Test
	public void cleanGroupId_shouldClearPrefix_when_hasPrefix() {
		assertEquals("SystemAdministrators", EmfResourcesUtil.cleanGroupId(GROUP_PREFIX + "SystemAdministrators"));
		assertEquals("PT_Conservators", EmfResourcesUtil.cleanGroupId(GROUP_PREFIX + "PT_Conservators"));
	}

	@Test
	public void should_CreateCorrectEveryoneGroup() {
		Resource group = EmfResourcesUtil.createEveryoneGroup();
		assertEquals(Security.PREFIX + ":" + ResourceService.EVERYONE_GROUP_ID, group.getId());
		assertEquals(ResourceService.EVERYONE_GROUP_ID, group.getName());
		assertEquals(ResourceType.GROUP, group.getType());
		assertEquals(InstanceType.create(EMF.GROUP.toString()), group.type());
	}

	@Test
	public void should_CreateCorrectAdminGroup() {
		Resource group = EmfResourcesUtil.createSystemAdminGroup();
		assertEquals(EMF.PREFIX + ":" + GROUP_PREFIX + ResourceService.SYSTEM_ADMIN_GROUP_ID, group.getId());
		assertEquals(GROUP_PREFIX + ResourceService.SYSTEM_ADMIN_GROUP_ID, group.getName());
		assertEquals(ResourceType.GROUP, group.getType());
		assertEquals(InstanceType.create(EMF.GROUP.toString()), group.type());
	}

	@Test
	public void getGroupDisplayName_ShouldReturnTitle_IfAvailable() {
		Map<String, Serializable> properties = new HashMap<>();
		properties.put(DefaultProperties.TITLE, "A group");

		String displayName = EmfResourcesUtil.getGroupDisplayName(properties);
		assertEquals("A group", displayName);
	}

	@Test
	public void getGroupDisplayName_ShouldReturnGroupId_IfTitleMissing() {
		Map<String, Serializable> properties = new HashMap<>();
		properties.put(ResourceProperties.GROUP_ID, "test-group");

		String displayName = EmfResourcesUtil.getGroupDisplayName(properties);
		assertEquals("test-group", displayName);
	}

	private static PropertyDefinition objectProperty(String name, boolean hasDmsType) {
		return property(name, hasDmsType, true);
	}

	private static PropertyDefinition dataProperty(String name, boolean hasDmsType) {
		return property(name, hasDmsType, false);
	}

	private static PropertyDefinition property(String name, boolean hasDmsType, boolean isObjectProperty) {
		PropertyDefinition property = mock(PropertyDefinition.class);
		when(property.getName()).thenReturn(name);
		if (hasDmsType) {
			when(property.getDmsType()).thenReturn("usc:" + name);
		}
		DataTypeDefinition typeDefinition = mock(DataTypeDefinition.class);
		when(property.getDataType()).thenReturn(typeDefinition);
		if (isObjectProperty) {
			when(typeDefinition.getName()).thenReturn(DataTypeDefinition.URI);
		} else {
			when(typeDefinition.getName()).thenReturn(DataTypeDefinition.TEXT);
		}
		return property;
	}

}
