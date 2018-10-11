package com.sirma.itt.seip.definition.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.testutil.mocks.ControlDefintionMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;

/**
 * Test for {@link TransitionDefinitionImpl}.
 *
 * @author A. Kunchev
 */
public class TransitionDefinitionImplTest {

	private static final String CONFIGURATION_JSON = "transition-configuration-test.json";
	private TransitionDefinitionImpl transition;

	@Before
	public void setup() {
		transition = new TransitionDefinitionImpl();
	}

	@Test
	public void getConfigurationAsJson_withoutControl() {
		PropertyDefinitionMock property = new PropertyDefinitionMock();
		property.setValue("value");
		transition.getFields().add(property);
		assertNull(transition.getConfigurationAsJson());
	}

	@Test
	public void getConfigurationAsJson_withControlWithoutValue() {
		PropertyDefinitionMock property = new PropertyDefinitionMock();
		ControlDefintionMock control = new ControlDefintionMock();
		control.setIdentifier("configuration");
		property.setControlDefinition(control);
		transition.getFields().add(property);
		assertNull(transition.getConfigurationAsJson());
	}

	@Test
	public void getConfigurationAsJson_withConfiguration() {
		JsonObject configuration = Json
				.createReader(getClass().getClassLoader().getResourceAsStream(CONFIGURATION_JSON))
					.readObject();
		PropertyDefinitionMock property = new PropertyDefinitionMock();
		property.setValue(configuration.toString());
		ControlDefintionMock control = new ControlDefintionMock();
		control.setIdentifier("configuration");
		property.setControlDefinition(control);
		transition.getFields().add(property);

		JsonObject result = transition.getConfigurationAsJson();
		assertNotNull(result);
		assertEquals("param-1", result.getJsonObject("params").getString("parameter1"));
		assertEquals("config-1", result.getString("configuration1"));
	}

	@Test
	public void testIsVisible() throws Exception {
		TransitionDefinitionImpl aTransition = new TransitionDefinitionImpl();

		assertTrue(aTransition.isVisible());

		aTransition.setDisplayType(DisplayType.EDITABLE);
		assertTrue(aTransition.isVisible());

		aTransition.setDisplayType(DisplayType.HIDDEN);
		assertFalse(aTransition.isVisible());
	}

	@Test
	public void should_Set_Default_Properties_If_Missing() {
		transition.setDefaultProperties();

		assertNotNull(transition.getActionPath());
		assertEquals("/actions", transition.getActionPath());
	}

	@Test
	public void should_Not_Set_Default_Properties_If_Sealed() {
		transition.seal();
		transition.setDefaultProperties();

		assertNull(transition.getActionPath());
	}
}
