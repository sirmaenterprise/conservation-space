/**
 *
 */
package com.sirma.itt.seip.instance.properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.PropertyModel;
import com.sirma.itt.seip.instance.CommonInstance;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceProperties;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.testutil.mocks.ControlDefintionMock;
import com.sirma.itt.seip.testutil.mocks.DataTypeDefinitionMock;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;
import com.sirma.itt.seip.testutil.mocks.PropertyDefinitionMock;

/**
 * Test for PropertiesConverter.
 *
 * @author A. Kunchev
 */
public class PropertiesConverterImplTest {

	@InjectMocks
	private PropertiesConverterImpl converter = new PropertiesConverterImpl();

	@Mock
	private TypeConverter typeConverter;

	@Mock
	private Instance<ResourceService> resourceService;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	// --------------------------- OutputModelConverter#convert ---------------------

	@Test
	public void convert_nullSource_nullResult() {
		PropertyDefinition definition = new PropertyDefinitionMock();
		Object actual = converter.new OutputModelConverter().convert(definition, null);
		assertNull(actual);
	}

	@Test
	public void convert_datePropertyNotNullSource_notNullResult() {
		dateConvertTestInternal(DataTypeDefinition.DATE);
	}

	@Test
	public void convert_datetimePropertyNotNullSource_notNullResult() {
		dateConvertTestInternal(DataTypeDefinition.DATETIME);
	}

	private void dateConvertTestInternal(String propertyDefinitionType) {
		PropertyDefinitionMock definition = new PropertyDefinitionMock();
		definition.setType(propertyDefinitionType);
		String date = "someDate";
		stubTypeConverter(date);
		Object actual = converter.new OutputModelConverter().convert(definition, date);
		Assert.assertEquals(date, actual);
	}

	@Test
	public void convert_propertyWithControlParam_RELATED_FIELDS_notNullResult() {
		PropertyDefinitionMock definition = new PropertyDefinitionMock();
		ControlDefinition controlDefinition = new ControlDefintionMock();
		controlDefinition.setIdentifier("RELATED_FIELDS");
		definition.setControlDefinition(controlDefinition);
		Object actual = converter.new OutputModelConverter().convert(definition, "value");
		assertEquals("value", actual);
	}

	@Test
	public void convertToJson() {
		DefinitionModel definition = new DefinitionMock();
		PropertyDefinitionMock propertyDefinition = new PropertyDefinitionMock();
		definition.getFields().add(propertyDefinition);
		propertyDefinition.setName("property1");
		propertyDefinition.setType(DataTypeDefinition.TEXT);

		PropertyModel model = new EmfInstance();
		Map<String, Serializable> properties = new HashMap<String, Serializable>();
		List<String> values = Arrays.asList("ET", "FUN", "PR");
		properties.put("property1", (Serializable) values);
		model.addAllProperties(properties);

		JsonObject result = converter.toJson(model, definition);
		assertEquals("\"[ET, FUN, PR]\"", result.get("property1").toString());
	}

	@Test
	public void convert_propertyWithControlParam_RADIO_BUTTON_GROUP_notNullResult() {
		PropertyDefinitionMock definition = new PropertyDefinitionMock();
		ControlDefinition controlDefinition = new ControlDefintionMock();
		controlDefinition.setIdentifier("RADIO_BUTTON_GROUP");
		definition.setControlDefinition(controlDefinition);
		Object actual = converter.new OutputModelConverter().convert(definition, "value");
		assertEquals("value", actual);
	}

	@Test
	public void convert_propertyWithControlParam_BYTE_FORMAT_String_notNullResult() {
		PropertyDefinitionMock definition = new PropertyDefinitionMock();
		ControlDefinition controlDefinition = new ControlDefintionMock();
		controlDefinition.setIdentifier("BYTE_FORMAT");
		definition.setControlDefinition(controlDefinition);
		Object actual = converter.new OutputModelConverter().convert(definition, "10");
		assertEquals("10 B", actual);
	}

	@Test
	public void convert_propertyWithControlParam_BYTE_FORMAT_Integet_notNullResult() {
		PropertyDefinitionMock definition = new PropertyDefinitionMock();
		ControlDefinition controlDefinition = new ControlDefintionMock();
		controlDefinition.setIdentifier("BYTE_FORMAT");
		definition.setControlDefinition(controlDefinition);
		Object actual = converter.new OutputModelConverter().convert(definition, new Integer(10));
		assertEquals("10 B", actual);
	}

	@Test
	public void convert_propertyWithControlParam_BYTE_FORMAT_Long_notNullResult() {
		PropertyDefinitionMock definition = new PropertyDefinitionMock();
		ControlDefinition controlDefinition = new ControlDefintionMock();
		controlDefinition.setIdentifier("BYTE_FORMAT");
		definition.setControlDefinition(controlDefinition);
		Object actual = converter.new OutputModelConverter().convert(definition, 10L);
		assertEquals("10 B", actual);
	}

	@Test
	public void convert_propertyWithControlParam_BYTE_FORMAT0_int_notNullResult() {
		PropertyDefinitionMock definition = new PropertyDefinitionMock();
		ControlDefinition controlDefinition = new ControlDefintionMock();
		controlDefinition.setIdentifier("BYTE_FORMAT");
		definition.setControlDefinition(controlDefinition);
		int value = 10;
		Object actual = converter.new OutputModelConverter().convert(definition, value);
		assertEquals("10 B", actual);
	}

	@Test
	public void convert_propertyWithControlParam_BYTE_FORMAT0_long_notNullResult() {
		PropertyDefinitionMock definition = new PropertyDefinitionMock();
		ControlDefinition controlDefinition = new ControlDefintionMock();
		controlDefinition.setIdentifier("BYTE_FORMAT");
		definition.setControlDefinition(controlDefinition);
		long value = 10;
		Object actual = converter.new OutputModelConverter().convert(definition, value);
		assertEquals("10 B", actual);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void convert_propertyWithControlParam_sourceCommonInstance_notNullResult() {
		PropertyDefinitionMock definition = new PropertyDefinitionMock();
		ControlDefinition controlDefinition = new ControlDefintionMock();
		definition.setControlDefinition(controlDefinition);
		CommonInstance instance = new CommonInstance();
		instance.setId("image");
		instance.add(DefaultProperties.HEADER_COMPACT, "compactHeader");
		instance.add(DefaultProperties.TITLE, null);
		instance.add(DefaultProperties.CONTENT, "content");
		Map<String, String> actual = (Map<String, String>) converter.new OutputModelConverter().convert(definition,
				instance);
		assertEquals(3, actual.size());
	}

	@Test
	public void convert_propertyWithControlParam_USER_notNullResult() {
		convertResourceInternalTest("USER");
	}

	@Test
	public void convert_propertyWithControlParam_PICKLIST_notNullResult() {
		convertResourceInternalTest("PICKLIST");
	}

	@Test
	public void converFromJson() throws Exception {

		when(typeConverter.convert(any(), any(Object.class))).then(a -> a.getArgumentAt(1, Object.class));

		DefinitionModel definition = new DefinitionMock();
		PropertyDefinitionMock propertyDefinition = new PropertyDefinitionMock();
		definition.getFields().add(propertyDefinition);
		propertyDefinition.setName("property1");
		propertyDefinition.setType(DataTypeDefinition.TEXT);
		DataTypeDefinition dataTypeDefinition = new DataTypeDefinitionMock(String.class, null);
		propertyDefinition.setDataType(dataTypeDefinition);
		propertyDefinition.setType(DataTypeDefinition.TEXT);

		JsonObject properties = Json
				.createReader(getClass().getClassLoader().getResourceAsStream("converter/properties.json"))
					.readObject();
		Map<String, Serializable> result = converter.fromJson(properties, definition);

		assertNotNull(result);
		assertEquals("value", result.get("property1"));
		assertNull(result.get("property2"));
		assertNull(result.get("property3"));
		assertNull(result.get("property4"));

		assertNull(result.get("property5"));
		assertNull(result.get("property6"));
	}

	@Test
	public void converFromJson_allowExternal() throws Exception {

		when(typeConverter.convert(any(), any(Object.class))).then(a -> a.getArgumentAt(1, Object.class));

		DefinitionModel definition = new DefinitionMock();
		PropertyDefinitionMock propertyDefinition = new PropertyDefinitionMock();
		definition.getFields().add(propertyDefinition);
		propertyDefinition.setName("property1");
		propertyDefinition.setType(DataTypeDefinition.TEXT);
		DataTypeDefinition dataTypeDefinition = new DataTypeDefinitionMock(String.class, null);
		propertyDefinition.setDataType(dataTypeDefinition);
		propertyDefinition.setType(DataTypeDefinition.TEXT);

		JsonObject properties = Json
				.createReader(getClass().getClassLoader().getResourceAsStream("converter/properties.json"))
					.readObject();
		Map<String, Serializable> result = converter.fromJson(properties, definition, true);

		assertNotNull(result);
		assertEquals("value", result.get("property1"));
		assertEquals("value2", result.get("property2"));
		assertEquals(3L, result.get("property3"));
		assertEquals(Boolean.TRUE, result.get("property4"));

		Serializable map = result.get("property5");
		assertTrue(map instanceof Map);
		assertTrue(((Map<?, ?>) map).containsKey("subProperty1"));
		assertTrue(((Map<?, ?>) map).containsKey("subProperty2"));
		Serializable list = result.get("property6");
		assertTrue(list instanceof Collection);
		assertEquals(3, ((Collection<?>) list).size());
	}

	@SuppressWarnings("unchecked")
	private void convertResourceInternalTest(String controlDefinitionIdentifier) {
		PropertyDefinitionMock definition = new PropertyDefinitionMock();
		ControlDefinition controlDefinition = new ControlDefintionMock();
		controlDefinition.setIdentifier(controlDefinitionIdentifier);
		definition.setControlDefinition(controlDefinition);
		ResourceService resService = mock(ResourceService.class);
		when(resourceService.get()).thenReturn(resService);
		when(resService.findResource(Matchers.anyString())).thenReturn(prepUser());
		Map<String, String> actual = (Map<String, String>) converter.new OutputModelConverter().convert(definition,
				"emf:user");
		assertEquals(3, actual.size());
	}

	// -------------------------- common methods ------------------------------------

	private static EmfUser prepUser() {
		EmfUser user = new EmfUser();
		user.setId("emf:user");
		user.add(ResourceProperties.FIRST_NAME, "user");
		user.add(ResourceProperties.LAST_NAME, "last");
		return user;
	}

	private void stubTypeConverter(Object toReturn) {
		when(typeConverter.convert(Matchers.any(), Matchers.any(Object.class))).thenReturn(toReturn);
	}

}
