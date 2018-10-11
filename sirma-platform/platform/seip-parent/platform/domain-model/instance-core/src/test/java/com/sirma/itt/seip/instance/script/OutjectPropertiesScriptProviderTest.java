
package com.sirma.itt.seip.instance.script;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.Serializable;
import java.util.Optional;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.state.Operation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for {@link OutjectPropertiesScriptProvider}.
 *
 * @author A. Kunchev
 */
public class OutjectPropertiesScriptProviderTest {

	private static final String OBJECT_PROPERTY_NAME = "objectPropertyName";
	private static final String UNIQUE_PROPERTY_NAME = "uniquePropertyName";
	private static final String TEST_JSON =
			"{\"outject\":[\"" + UNIQUE_PROPERTY_NAME + "\", \"title\", \"description\", \"" + OBJECT_PROPERTY_NAME
					+ "\"], \"outjectNotEmpty\":[\"content\", \"createdBy\", \"compact_header\", \"name\"] }";
	private static final String CURRENT_INSTANCE_OBJECT_PROPERTY_VALUE = "instanceObjectPropertyValue";
	private static final String PARENT_INSTANCE_OBJECT_PROPERTY_VALUE = "parentObjectPropertyValue";
	private static final String UNIQUE_PROPERTY_VALUE = "uniquePropertyValue";

	@InjectMocks
	private OutjectPropertiesScriptProvider provider = new OutjectPropertiesScriptProvider();
	@Mock
	private InstanceService instanceService;
	@Mock
	private DefinitionService definitionService;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getBindings_withOurjectEntry_containsOutjectKeyAndValue() {
		assertTrue(provider.getBindings().containsKey("outject"));
		assertTrue(provider.getBindings().get("outject") != null);
	}

	@Test
	public void getScripts_oneScript_sizeOne() {
		assertEquals(1, provider.getScripts().size());
	}

	@Test
	public void outjectProperties_nullSource_false() {
		assertFalse(provider.outjectProperties(null, new ScriptNode(), "{}"));
		verify(instanceService, never()).save(any(Instance.class), any(Operation.class));
	}

	@Test
	public void outjectProperties_nullPropertyObject_false() {
		assertFalse(provider.outjectProperties(new ScriptNode(), new ScriptNode(), null));
		verify(instanceService, never()).save(any(Instance.class), any(Operation.class));
	}

	@Test
	public void outjectProperties_emptyPropertyObject_false() {
		assertFalse(provider.outjectProperties(new ScriptNode(), new ScriptNode(), ""));
		verify(instanceService, never()).save(any(Instance.class), any(Operation.class));
	}

	@Test
	public void outjectProperties_noCurrentInstance_false() {
		assertFalse(provider.outjectProperties(new ScriptNode(), prepareParentScriptNode(), TEST_JSON));
		verify(instanceService, never()).save(any(Instance.class), any(Operation.class));
	}

	@Test
	public void outjectProperties_noParentInstance_false() {
		assertFalse(provider.outjectProperties(prepareCurrentScriptNode(), new ScriptNode(), TEST_JSON));
		verify(instanceService, never()).save(any(Instance.class), any(Operation.class));
	}

	@Test
	public void outjectProperties_true() {
		ScriptNode parent = prepareParentScriptNode();
		ScriptNode current = prepareCurrentScriptNode();

		assertTrue(provider.outjectProperties(current, parent, TEST_JSON));

		verify(instanceService).save(any(Instance.class), any(Operation.class));

		Instance parentInstance = parent.getTarget();
		assertEquals("", parentInstance.getString(DefaultProperties.DESCRIPTION));
		assertEquals("parentContent", parentInstance.getString(DefaultProperties.CONTENT));
		assertEquals("currentTitle", parentInstance.getString(DefaultProperties.TITLE));
		assertEquals("header", parentInstance.getString(DefaultProperties.HEADER_COMPACT));
		assertEquals("currentName", parentInstance.getString(DefaultProperties.NAME));
		assertNull(parentInstance.getString(UNIQUE_PROPERTY_NAME));
		assertEquals(CURRENT_INSTANCE_OBJECT_PROPERTY_VALUE, parentInstance.getString(OBJECT_PROPERTY_NAME));

	}

	private ScriptNode prepareCurrentScriptNode() {
		ScriptNode current = new ScriptNode();
		Instance currentInstance = prepareInstance();
		current.setTarget(currentInstance);
		return current;
	}

	private static ScriptNode prepareParentScriptNode() {
		ScriptNode parent = new ScriptNode();
		EmfInstance parentInstance = new EmfInstance();
		parentInstance.add(DefaultProperties.TITLE, "parentTitle");
		parentInstance.add(DefaultProperties.DESCRIPTION, "parentDescription");
		parentInstance.add(DefaultProperties.CONTENT, "parentContent");
		parentInstance.add(DefaultProperties.HEADER_COMPACT, "header");
		parentInstance.add(DefaultProperties.NAME, "parentName");
		parentInstance.add(OBJECT_PROPERTY_NAME, PARENT_INSTANCE_OBJECT_PROPERTY_VALUE);
		parent.setTarget(parentInstance);
		return parent;
	}

	private Instance prepareInstance() {
		DefinitionModel instanceDefinition = Mockito.mock(DefinitionModel.class);
		EmfInstance instance = new EmfInstance();
		addProperty(instance, instanceDefinition, DefaultProperties.TITLE, "currentTitle", DataTypeDefinition.TEXT);
		addProperty(instance, instanceDefinition, DefaultProperties.DESCRIPTION, "", DataTypeDefinition.TEXT);
		addProperty(instance, instanceDefinition, DefaultProperties.CONTENT, "", DataTypeDefinition.TEXT);
		addProperty(instance, instanceDefinition, DefaultProperties.HEADER_COMPACT, null, DataTypeDefinition.TEXT);
		addProperty(instance, instanceDefinition, DefaultProperties.NAME, "currentName", DataTypeDefinition.TEXT);
		addProperty(instance, instanceDefinition, OBJECT_PROPERTY_NAME, CURRENT_INSTANCE_OBJECT_PROPERTY_VALUE,
				DataTypeDefinition.URI);
		addUniqueFieldProperty(instance, instanceDefinition, UNIQUE_PROPERTY_NAME, UNIQUE_PROPERTY_VALUE,
				DataTypeDefinition.TEXT);
		Mockito.when(definitionService.getInstanceDefinition(instance)).thenReturn(instanceDefinition);
		return instance;
	}

	private static PropertyDefinition addProperty(Instance instance, DefinitionModel instanceModel, String propertyName,
			Serializable propertyValue, String dataTypeName) {
		instance.add(propertyName, propertyValue);
		PropertyDefinition propertyDefinition = Mockito.mock(PropertyDefinition.class);
		DataTypeDefinition dataType = Mockito.mock(DataTypeDefinition.class);
		Mockito.when(dataType.getName()).thenReturn(dataTypeName);
		Mockito.when(propertyDefinition.getDataType()).thenReturn(dataType);
		Mockito.when(instanceModel.getField(propertyName)).thenReturn(Optional.of(propertyDefinition));
		return propertyDefinition;
	}

	private static void addUniqueFieldProperty(Instance instance, DefinitionModel instanceModel,
			String propertyName, Serializable propertyValue, String dataTypeName) {
		PropertyDefinition propertyDefinition = addProperty(instance, instanceModel, propertyName, propertyValue,
				dataTypeName);
		Mockito.when(propertyDefinition.isUnique()).thenReturn(true);
	}
}
