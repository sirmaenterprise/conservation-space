package com.sirma.itt.emf.util;

import java.io.Serializable;
import java.util.HashMap;

import net.javacrumbs.jsonunit.JsonAssert;

import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.emf.domain.StringPair;
import com.sirma.itt.emf.instance.model.EmfInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.DefaultProperties;

/**
 * Tests for {@link JsonUtil}.
 * 
 * @author Adrian Mitev
 */
@Test
public class JsonUtilTest {

	/**
	 * Tests {@link JsonUtil#transformInstance(Instance, String...)} with correct data.
	 */
	public void testTransform() {
		JSONObject result = JsonUtil.transformInstance(constructTestInstance("emf:testId"),
				DefaultProperties.TITLE, DefaultProperties.NAME, DefaultProperties.TYPE);

		JsonAssert.assertJsonEquals(
				"{ \"id\":\"emf:testId\", \"title\" : \"test title\",\"name\" : \"test name\"}",
				result.toString());
	}

	/**
	 * Tests {@link JsonUtil#transformInstance(Instance, String...)} with correct data.
	 */
	public void testTransformWitouthId() {
		JSONObject result = JsonUtil.transformInstance(constructTestInstance(null),
				DefaultProperties.TITLE, DefaultProperties.NAME, DefaultProperties.TYPE);

		JsonAssert.assertJsonEquals("{\"title\" : \"test title\",\"name\" : \"test name\"}",
				result.toString());
	}

	/**
	 * Tests {@link JsonUtil#transformInstance(Instance, String...)} with first parameter as null.
	 */
	public void testTransformWithNullInstance() {
		Assert.assertNull(JsonUtil.transformInstance(null, DefaultProperties.NAME));
	}

	/**
	 * Tests {@link JsonUtil#transformInstance(Instance, String...)} with second parameter as null.
	 */
	public void testTransformWithNullProperties() {
		String[] properties = null;
		Assert.assertNull(JsonUtil.transformInstance(constructTestInstance(null), properties));
	}

	/**
	 * Tests {@link JsonUtil#transformInstance(Instance, StringPair...)} with correct data.
	 */
	public void testTransformWithPairedProperties() {
		JSONObject result = JsonUtil.transformInstance(constructTestInstance("emf:testId"),
				new StringPair[] { new StringPair("custom-title", DefaultProperties.TITLE),
						new StringPair("custom-name", DefaultProperties.NAME) });

		JsonAssert
				.assertJsonEquals(
						"{ \"id\":\"emf:testId\", \"custom-title\" : \"test title\",\"custom-name\" : \"test name\"}",
						result.toString());
	}

	/**
	 * Tests {@link JsonUtil#transformInstance(Instance, StringPair...)} with correct data.
	 */
	public void testTransformWithPairedPropertiesWithoutId() {
		JSONObject result = JsonUtil.transformInstance(constructTestInstance(null), new StringPair[] {
				new StringPair("custom-title", DefaultProperties.TITLE),
				new StringPair("custom-name", DefaultProperties.NAME) });

		JsonAssert.assertJsonEquals(
				"{ \"custom-title\" : \"test title\",\"custom-name\" : \"test name\"}",
				result.toString());
	}

	/**
	 * Tests {@link JsonUtil#transformInstance(Instance, String...)} with second parameter as null.
	 */
	public void testTransformForStringPairWithNullInstance() {
		StringPair[] parameters = new StringPair[0];
		Assert.assertNull(JsonUtil.transformInstance(null, parameters));
	}

	/**
	 * Tests {@link JsonUtil#transformInstance(Instance, String...)} with second parameter as null.
	 */
	public void testTransformForStringPairWithNullProperties() {
		StringPair[] parameters = null;
		Assert.assertNull(JsonUtil.transformInstance(constructTestInstance(null), parameters));
	}

	/**
	 * Constructs an {@link Instance} for testing purposes.
	 * 
	 * @param id
	 *            id to set.
	 * @return constructed instance.
	 */
	private Instance constructTestInstance(String id) {
		EmfInstance instance = new EmfInstance();
		instance.setId(id);

		instance.setProperties(new HashMap<String, Serializable>());
		instance.getProperties().put(DefaultProperties.TITLE, "test title");
		instance.getProperties().put(DefaultProperties.NAME, "test name");
		instance.getProperties().put(DefaultProperties.TYPE, null);
		return instance;
	}

}
