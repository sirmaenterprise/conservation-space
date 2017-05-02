package com.sirma.itt.seip.template.rest.handlers.writers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Test;
import org.testng.Assert;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.template.TemplateInstance;
import com.sirma.itt.seip.template.TemplateProperties;
import com.sirma.itt.seip.template.TemplatePurposes;
import com.sirma.itt.seip.testutil.io.FileTestUtils;

import net.javacrumbs.jsonunit.JsonAssert;

public class TemplateInstanceMessageBodyWriterTest {

	private TemplateInstanceMessageBodyWriter writer = new TemplateInstanceMessageBodyWriter();

	@Test
	public void testIsWritable() {
		Assert.assertTrue(writer.isWriteable(TemplateInstance.class, null, null, null));
		Assert.assertFalse(writer.isWriteable(Instance.class, null, null, null));
	}

	@Test
	public void testFullWrite() throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		TemplateInstance instance = new TemplateInstance();
		instance.setId("1");
		instance.setCorrespondingInstance("emf:id");
		instance.setForType("definition-id");
		instance.setPrimary(Boolean.TRUE);
		instance.setPurpose(TemplatePurposes.CREATABLE);
		
		Map<String, Serializable> properties = instance.getProperties();
		properties.put(TemplateProperties.CONTENT, "test");
		properties.put(TemplateProperties.TITLE, "Test Template");

		writer.writeTo(instance, null, null, null, null, null, out);

		JsonObject actual = Json.createReader(new ByteArrayInputStream(out.toByteArray())).readObject();
		JsonObject expected = Json.createReader(FileTestUtils.getResourceAsStream("/json/full-template-no-owning-instance.json")).readObject();

		JsonAssert.assertJsonEquals(expected, actual);
	}
}
