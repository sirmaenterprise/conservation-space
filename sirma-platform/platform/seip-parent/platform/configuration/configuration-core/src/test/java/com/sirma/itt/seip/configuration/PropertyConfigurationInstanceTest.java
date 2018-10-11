package com.sirma.itt.seip.configuration;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.lang.reflect.Field;

import org.testng.annotations.Test;

import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.TypeConverterContext;

@Test
public class PropertyConfigurationInstanceTest {

	@ConfigurationPropertyDefinition(system = true, shared = false, label = "some label", type = Number.class, converter = "converter", subSystem = "subSystem")
	static String CONFIG = "test";
	@ConfigurationPropertyDefinition(system = true, shared = false, type = Number.class, converter = "converter", alias = "alias")
	static String CONFIG_2 = "test";

	public void testConfigurationProperties_1() throws NoSuchFieldException, SecurityException {
		Field definedOn = PropertyConfigurationInstanceTest.class.getDeclaredField("CONFIG");
		ConfigurationPropertyDefinition propertyDefinition = definedOn
				.getAnnotation(ConfigurationPropertyDefinition.class);
		PropertyConfigurationInstance instance = new PropertyConfigurationInstance(propertyDefinition, definedOn);

		assertEquals(instance.getAnnotation(), propertyDefinition);
		assertEquals(instance.getLabel(), "some label");
		assertEquals(instance.getName(), CONFIG);
		assertEquals(instance.getType(), Number.class);
		assertEquals(instance.isComplex(), false);
		assertEquals(instance.isSharedConfiguration(), false);
		assertEquals(instance.isSystemConfiguration(), true);
		assertEquals(instance.getConverter(), "converter");
		assertEquals(instance.getAlias(), "subSystem.test");
		assertNotNull(instance.getDefinedOn());

		TypeConverterContext context = instance.createConverterContext(null, null, null);
		assertNotNull(context);
		context = instance.createConverterContext(null, null, null);
		assertNotNull(context);
	}

	public void testConfigurationProperties_2() throws NoSuchFieldException, SecurityException {
		Field definedOn = PropertyConfigurationInstanceTest.class.getDeclaredField("CONFIG_2");
		ConfigurationPropertyDefinition propertyDefinition = definedOn
				.getAnnotation(ConfigurationPropertyDefinition.class);
		PropertyConfigurationInstance instance = new PropertyConfigurationInstance(propertyDefinition, definedOn);

		assertEquals(instance.getAnnotation(), propertyDefinition);
		assertEquals(instance.getLabel(), CONFIG_2);
		assertEquals(instance.getName(), CONFIG_2);
		assertEquals(instance.getType(), Number.class);
		assertEquals(instance.isComplex(), false);
		assertEquals(instance.isSharedConfiguration(), false);
		assertEquals(instance.isSystemConfiguration(), true);
		assertEquals(instance.getConverter(), "converter");
		assertEquals(instance.getAlias(), "alias");
		assertNotNull(instance.getDefinedOn());

		TypeConverterContext context = instance.createConverterContext(null, null, null);
		assertNotNull(context);
		context = instance.createConverterContext(null, null, null);
		assertNotNull(context);
	}
}
