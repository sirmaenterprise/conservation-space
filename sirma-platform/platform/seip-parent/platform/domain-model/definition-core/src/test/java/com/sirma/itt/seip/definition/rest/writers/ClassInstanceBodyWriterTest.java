
package com.sirma.itt.seip.definition.rest.writers;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;
import org.junit.Test;

import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.json.JsonUtil;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * Tests the functionality of {@link ClassInstanceBodyWriter}.
 *
 * @author Vilizar Tsonev
 */
public class ClassInstanceBodyWriterTest {

	private ClassInstanceBodyWriter writer = new ClassInstanceBodyWriter();

	/**
	 * Tests the body writer when writing the full class info.
	 *
	 * @throws Exception
	 *             if an error occurs during the write process
	 */
	@Test
	public void testWriteFullInfo() throws Exception {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ClassInstance classInstance = new ClassInstance();
		classInstance.setId("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document");
		// In the semantic model and ClassInstance the property is defined as "creatEable" and that's why we
		// pass it that way here
		classInstance.getProperties().put("createable", true);
		classInstance.getProperties().put("uploadable", true);
		classInstance.getProperties().put("versionable", true);
		writer.writeTo(classInstance, null, null, null, null, null, stream);
		JSONObject expectedJson = new JSONObject();
		JsonUtil.addToJson(expectedJson, "id",
				"http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document");
		JsonUtil.addToJson(expectedJson, "creatable", true);
		JsonUtil.addToJson(expectedJson, "mailboxSupportable", false);
		JsonUtil.addToJson(expectedJson, "uploadable", true);
		JsonUtil.addToJson(expectedJson, "versionable", true);
		String expected = expectedJson.toString();
		String result = new String(stream.toByteArray(), StandardCharsets.UTF_8);
		JsonAssert.assertJsonEquals(expected, result);
	}

	/**
	 * Tests the body writer when the entity stream is not initialized.
	 *
	 * @throws Exception
	 *             if an error occurs during the write process
	 */
	@Test(expected = NullPointerException.class)
	public void testNullStream() throws Exception {
		ClassInstance classInstance = new ClassInstance();
		classInstance.setId("URI");
		classInstance.getProperties().put("createable", true);
		classInstance.getProperties().put("uploadable", true);
		classInstance.getProperties().put("versionable", true);
		writer.writeTo(classInstance, null, null, null, null, null, null);
	}
}
