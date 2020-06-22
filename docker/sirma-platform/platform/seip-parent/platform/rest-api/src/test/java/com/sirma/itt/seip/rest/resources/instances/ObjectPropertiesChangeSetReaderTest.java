package com.sirma.itt.seip.rest.resources.instances;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyMap;
import static com.sirma.itt.seip.collections.CollectionUtils.emptySet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;

/**
 * Test for {@link ObjectPropertiesChangeSetReader}.
 *
 * @author A. Kunchev
 */
@SuppressWarnings("static-method")
public class ObjectPropertiesChangeSetReaderTest {

	@Test(expected = NullPointerException.class)
	public void transform_nullDefinition() {
		ObjectPropertiesChangeSetReader.transform(null, emptyMap(), new EmfInstance());
	}

	@Test(expected = NullPointerException.class)
	public void transform_nullProperties() {
		ObjectPropertiesChangeSetReader.transform(new DefinitionMock(), null, new EmfInstance());
	}

	@Test(expected = NullPointerException.class)
	public void transform_nullInstance() {
		ObjectPropertiesChangeSetReader.transform(new DefinitionMock(), emptyMap(), null);
	}

	@Test
	public void transform_changeSet_nullAddAndRemove_noChanges() {
		final int PROPERTYIES_NUMBER = 1;
		Map<String, Serializable> properties = new HashMap<>(PROPERTYIES_NUMBER);
		properties.put("identifier-1", prepareChangeSet(null, null));

		Instance instance = new EmfInstance();
		instance.add("identifier-1", (Serializable) Arrays.asList("id-10", "id-11"));

		ObjectPropertiesChangeSetReader.transform(prepareDefinition(PROPERTYIES_NUMBER), properties, instance);

		assertTrue(properties.isEmpty());
	}

	@Test
	public void transform_changeSet_emptyAddAndRemove_noChanges() {
		final int PROPERTYIES_NUMBER = 1;
		Map<String, Serializable> properties = new HashMap<>(PROPERTYIES_NUMBER);
		properties.put("identifier-1", prepareChangeSet(emptySet(), emptySet()));

		Instance instance = new EmfInstance();
		instance.add("identifier-1", (Serializable) Arrays.asList("id-10", "id-11"));

		ObjectPropertiesChangeSetReader.transform(prepareDefinition(PROPERTYIES_NUMBER), properties, instance);

		assertTrue(properties.isEmpty());
	}

	@Test
	public void transform_changeSet_noNewAndOldValues() {
		final int PROPERTYIES_NUMBER = 1;
		Map<String, Serializable> properties = new HashMap<>(PROPERTYIES_NUMBER);
		properties.put("identifier-1", null);

		Instance instance = new EmfInstance();
		instance.add("identifier-1", null);

		ObjectPropertiesChangeSetReader.transform(prepareDefinition(PROPERTYIES_NUMBER), properties, instance);

		assertTrue(properties.isEmpty());
		assertNull(properties.get("identifier-1"));
	}

	@Test
	public void transform_changeSet_oldValuesExist_changeSetNullOrEmptyMap_noChanges() {
		final int PROPERTYIES_NUMBER = 2;
		Map<String, Serializable> properties = new HashMap<>(PROPERTYIES_NUMBER);
		Map<String, Serializable> nullMap = null;
		properties.put("identifier-1", (Serializable) nullMap);
		properties.put("identifier-2", (Serializable) emptyMap());

		Instance instance = new EmfInstance();
		instance.add("identifier-1", (Serializable) Collections.singleton("id-1"));
		instance.add("identifier-2", (Serializable) Collections.singleton("id-2"));

		ObjectPropertiesChangeSetReader.transform(prepareDefinition(PROPERTYIES_NUMBER), properties, instance);

		assertTrue(properties.isEmpty());
	}

	@Test
	public void transform_changeSet_noOldValues_onlyAdd_propertyAdded() {
		final int PROPERTYIES_NUMBER = 2;
		Map<String, Serializable> properties = new HashMap<>(PROPERTYIES_NUMBER);
		properties.put("identifier-1",
				(Serializable) Collections.singletonMap("add", (Serializable) Collections.singleton("id-1")));
		properties.put("identifier-2",
				(Serializable) Collections.singletonMap("add", (Serializable) Arrays.asList("id-2", "id-3")));

		ObjectPropertiesChangeSetReader.transform(prepareDefinition(PROPERTYIES_NUMBER), properties, new EmfInstance());

		assertFalse(properties.isEmpty());
		assertEquals(Collections.singleton("id-1"), properties.get("identifier-1"));
		assertEquals(2, ((Collection<?>) properties.get("identifier-2")).size());
	}

	@Test
	public void transform_changeSet_withOldValueCollection_onlyRemove_propertyValueRemoved() {
		final int PROPERTYIES_NUMBER = 2;
		Map<String, Serializable> properties = new HashMap<>(PROPERTYIES_NUMBER);
		properties.put("identifier-1",
				(Serializable) Collections.singletonMap("remove", (Serializable) Collections.singleton("id-1")));
		properties.put("identifier-2",
				(Serializable) Collections.singletonMap("remove", (Serializable) Arrays.asList("id-4", "id-5")));

		Instance instance = new EmfInstance();
		instance.add("identifier-1", (Serializable) Arrays.asList("id-1", "id-2", "id-3"));
		instance.add("identifier-2", (Serializable) Arrays.asList("id-4", "id-5", "id-6"));

		ObjectPropertiesChangeSetReader.transform(prepareDefinition(PROPERTYIES_NUMBER), properties, instance);

		assertFalse(properties.isEmpty());
		assertEquals(2, ((Collection<?>) properties.get("identifier-1")).size());
		assertEquals(Collections.singletonList("id-6"), properties.get("identifier-2"));
	}

	@Test
	public void transform_changeSet_oldValueNotCollection_oldValueRemoved() {
		final int PROPERTYIES_NUMBER = 1;
		Map<String, Serializable> properties = new HashMap<>(PROPERTYIES_NUMBER);
		properties.put("identifier-1", prepareChangeSet(emptySet(), Collections.singleton("id-1")));

		Instance instance = new EmfInstance();
		instance.add("identifier-1", "id-1");

		ObjectPropertiesChangeSetReader.transform(prepareDefinition(PROPERTYIES_NUMBER), properties, instance);

		assertTrue(properties.containsKey("identifier-1"));
		assertNull(properties.get("identifier-1"));
	}

	@Test
	public void transform_changeSet_notEmptyAdd_emptyRemove() {
		final int PROPERTYIES_NUMBER = 2;
		Map<String, Serializable> properties = new HashMap<>(PROPERTYIES_NUMBER);
		properties.put("identifier-1", prepareChangeSet(Collections.singleton("id-1"), emptySet()));
		properties.put("identifier-2", prepareChangeSet(Arrays.asList("id-2", "id-3"), emptySet()));

		Instance instance = new EmfInstance();
		instance.add("identifier-1", (Serializable) Arrays.asList("id-10", "id-11"));

		ObjectPropertiesChangeSetReader.transform(prepareDefinition(PROPERTYIES_NUMBER), properties, instance);

		assertFalse(properties.isEmpty());
		assertEquals(3, ((Collection<?>) properties.get("identifier-1")).size());
		assertEquals(2, ((Collection<?>) properties.get("identifier-2")).size());
	}

	@Test
	public void transform_changeSet_emptyAdd_notEmptyRemove() {
		final int PROPERTYIES_NUMBER = 2;
		Map<String, Serializable> properties = new HashMap<>(PROPERTYIES_NUMBER);
		properties.put("identifier-1", prepareChangeSet(emptySet(), Collections.singleton("id-1")));
		properties.put("identifier-2", prepareChangeSet(emptySet(), Arrays.asList("id-2", "id-3")));

		Instance instance = new EmfInstance();
		instance.add("identifier-1", (Serializable) Arrays.asList("id-1", "id-22"));
		instance.add("identifier-2", (Serializable) Arrays.asList("id-2", "id-11"));

		ObjectPropertiesChangeSetReader.transform(prepareDefinition(PROPERTYIES_NUMBER), properties, instance);

		assertFalse(properties.isEmpty());
		assertEquals(1, ((Collection<?>) properties.get("identifier-1")).size());
		assertEquals(1, ((Collection<?>) properties.get("identifier-2")).size());
	}

	@Test
	public void transform_changeSet_addedNotEmpty_removedNotEmpty() {
		final int PROPERTYIES_NUMBER = 2;
		Map<String, Serializable> properties = new HashMap<>(PROPERTYIES_NUMBER);
		properties.put("identifier-1", prepareChangeSet(Collections.singleton("id-1"), Collections.singleton("id-2")));
		properties.put("identifier-2", prepareChangeSet(Arrays.asList("id-3", "id-4"), Arrays.asList("id-5", "id-6")));

		Instance instance = new EmfInstance();
		instance.add("identifier-1", (Serializable) Arrays.asList("id-10", "id-11", "id-2"));
		instance.add("identifier-2", (Serializable) Arrays.asList("id-5", "id-6"));

		ObjectPropertiesChangeSetReader.transform(prepareDefinition(PROPERTYIES_NUMBER), properties, instance);

		assertFalse(properties.isEmpty());
		assertEquals(3, ((Collection<?>) properties.get("identifier-1")).size());
		assertEquals(2, ((Collection<?>) properties.get("identifier-2")).size());
	}

	private static Serializable prepareChangeSet(Collection<String> add, Collection<String> remove) {
		Map<String, Serializable> changeSet = new HashMap<>(2);
		changeSet.put("add", (Serializable) add);
		changeSet.put("remove", (Serializable) remove);
		return (Serializable) changeSet;
	}

	private static DefinitionModel prepareDefinition(int propertiesNumber) {
		List<PropertyDefinition> properties = new ArrayList<>(propertiesNumber);
		for (int i = 1; i < propertiesNumber + 1; i++) {
			properties.add(preparePropertyDefinition("identifier-" + i));
		}

		DefinitionMock definition = new DefinitionMock();
		definition.setFields(properties);
		return definition;
	}

	private static PropertyDefinition preparePropertyDefinition(String name) {
		PropertyDefinitionMock property = new PropertyDefinitionMock();
		property.setName(name);
		DataTypeDefinitionMock departmentDataType = new DataTypeDefinitionMock(String.class, null);
		departmentDataType.setName(DataTypeDefinition.URI);
		property.setDataType(departmentDataType);
		return property;
	}

	@Test
	public void transform_usersModel() {
		final int PROPERTYIES_NUMBER = 2;
		Map<String, Serializable> properties = new HashMap<>(PROPERTYIES_NUMBER);
		properties.put("identifier-1", buildUserProperty("user-1"));
		properties.put("identifier-2", buildUserProperty("user-2"));

		ObjectPropertiesChangeSetReader.transform(prepareDefinition(PROPERTYIES_NUMBER), properties, new EmfInstance());

		assertEquals("user-1", properties.get("identifier-1"));
		assertEquals("user-2", properties.get("identifier-2"));
	}

	private Serializable buildUserProperty(String userId) {
		Map<String, Serializable> userData = new HashMap<>(3);
		userData.put("id", userId);
		userData.put("title", "user-title");
		userData.put("email", "user-email");
		return (Serializable) userData;
	}
}