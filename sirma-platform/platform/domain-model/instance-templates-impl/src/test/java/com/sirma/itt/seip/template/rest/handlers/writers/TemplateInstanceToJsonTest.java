package com.sirma.itt.seip.template.rest.handlers.writers;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;

import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.template.TemplateInstance;
import com.sirma.itt.seip.template.TemplateProperties;

/**
 * Test class for {@link TemplateInstanceToJson}
 *
 * @author simeon iliev
 */
public class TemplateInstanceToJsonTest {

	private TemplateInstance templateInstance;

	private JsonGenerator generator;

	private ByteArrayOutputStream out;

	@Before
	public void setUp() throws Exception {
		templateInstance = new TemplateInstance();
		out = new ByteArrayOutputStream();
		generator = Json.createGenerator(out);
	}

	@Test
	public void should_writeCorrentFullJsonOfTemplateInstance() throws Exception {
		templateInstance.setId("emf:id");
		templateInstance.setForType("IDK");
		templateInstance.setContent("<br>T1</br>");
		templateInstance.setCorrespondingInstance("emf:templateId");
		templateInstance.getProperties().put(DefaultProperties.TITLE, "title");
		templateInstance.getProperties().put(DefaultProperties.PURPOSE, "primary");
		TemplateInstanceToJson.writeJson(generator, templateInstance);
		generator.flush();
		JsonObject object = Json.createReader(new ByteArrayInputStream(out.toByteArray())).readObject();
		assertEquals(object.values().size(), 5);
		assertEquals(object.getString(JsonKeys.ID), "emf:id");
		assertEquals(object.getString(JsonKeys.TEMPLATE_INSTANCE_ID), "emf:templateId");
		assertEquals(object.getString(TemplateProperties.FOR_TYPE), "IDK");
		assertEquals(object.getString(JsonKeys.CONTENT), "<br>T1</br>");
		assertEquals(object.getJsonObject(JsonKeys.PROPERTIES).entrySet().size(), 3);
		assertEquals(object.getJsonObject(JsonKeys.PROPERTIES).getString(TemplateProperties.PURPOSE), "primary");
		assertEquals(object.getJsonObject(JsonKeys.PROPERTIES).getString(DefaultProperties.TITLE), "title");
		assertFalse(object.getJsonObject(JsonKeys.PROPERTIES).getBoolean(TemplateProperties.PRIMARY));
	}

	@Test(expected = EmfRuntimeException.class)
	public void should_throwAnExceptionWhenIdIsEmpty() throws Exception {
		templateInstance.setId("");
		templateInstance.setForType("IDK");
		TemplateInstanceToJson.writeJson(generator, templateInstance);
	}

}
