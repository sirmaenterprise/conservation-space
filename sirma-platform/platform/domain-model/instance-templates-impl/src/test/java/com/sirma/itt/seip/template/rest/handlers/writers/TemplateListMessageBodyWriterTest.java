package com.sirma.itt.seip.template.rest.handlers.writers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;

import org.junit.Assert;
import org.junit.Test;

import com.sirma.itt.seip.template.TemplateInstance;

/**
 * Tests for {@link TemplateListMessageBodyWriter}.
 * 
 * @author yasko
 */
public class TemplateListMessageBodyWriterTest {

	private TemplateListMessageBodyWriter writer = new TemplateListMessageBodyWriter();
	
	/**
	 * Null list -> empty array
	 * 
	 * @throws Exception thrown from writer
	 */
	@Test
	public void testNullList() throws Exception {
		JsonArray array = invokeWriter(null);
		Assert.assertNotNull(array);
		Assert.assertEquals(0, array.size());
	}
	
	/**
	 * Empty list -> empty array
	 * 
	 * @throws Exception thrown from writer
	 */
	@Test
	public void testEmptyList() throws Exception {
		JsonArray array = invokeWriter(new LinkedList<>());
		Assert.assertNotNull(array);
		Assert.assertEquals(0, array.size());
	}
	
	/**
	 * List with items -> array with items
	 * 
	 * @throws Exception thrown from writer
	 */
	@Test
	public void testList() throws Exception {
		List<TemplateInstance> list = new LinkedList<>();
		TemplateInstance instance = new TemplateInstance();
		instance.setForType("1");
		instance.setId("emf:Id");
		instance.setCorrespondingInstance("InstanceID");
		list.add(instance);
		
		JsonArray array = invokeWriter(list);
		Assert.assertNotNull(array);
		Assert.assertEquals(1, array.size());
	}
	
	private JsonArray invokeWriter(List<TemplateInstance> list) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		writer.writeTo(list, null, null, null, null, null, out);
		return Json.createReader(new ByteArrayInputStream(out.toByteArray())).readArray();
	}
}
