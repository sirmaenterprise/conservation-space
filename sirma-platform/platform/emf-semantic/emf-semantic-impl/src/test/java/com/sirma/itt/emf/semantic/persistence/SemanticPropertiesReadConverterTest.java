package com.sirma.itt.emf.semantic.persistence;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;

import com.sirma.itt.seip.definition.model.FieldDefinitionImpl;
import com.sirma.itt.seip.domain.semantic.persistence.MultiLanguageValue;
import com.sirma.itt.seip.testutil.mocks.DefinitionMock;

/**
 * Tests for {@link SemanticPropertiesReadConverter}.
 *
 * @author nvelkov
 */
public class SemanticPropertiesReadConverterTest {

	private SemanticPropertiesReadConverter converter = new SemanticPropertiesReadConverter();

	/**
	 * Test the conversion of the semantic properties to internal model.
	 */
	@Test
	public void testConvertModelProperties() {
		FieldDefinitionImpl field = new FieldDefinitionImpl();
		field.setName("emf:title");
		field.setMultiValued(false);
		field.setIdentifier("emf:title");

		DefinitionMock model = new DefinitionMock();
		model.setFields(Arrays.asList(field));

		Map<String, Set<Value>> instanceProperties = new HashMap<>();
		Value value = new LiteralImpl("someTitle");
		instanceProperties.put("emf:title", new HashSet<>(Arrays.asList(value)));
		Map<String, Serializable> properties = new HashMap<>();
		converter.convertPropertiesFromSemanticToInternalModel(model, instanceProperties, properties);

		String title = (String) properties.get("emf:title");
		Assert.assertEquals("someTitle", title);
	}

	/**
	 * Test the conversion of the semantic multi-language properties to internal model.
	 */
	@Test
	public void testConvertModelPropertiesMultiLanguage() {
		FieldDefinitionImpl field = new FieldDefinitionImpl();
		field.setName("emf:title");
		field.setMultiValued(false);
		field.setIdentifier("emf:title");

		DefinitionMock model = new DefinitionMock();
		model.setFields(Arrays.asList(field));

		Map<String, Set<Value>> instanceProperties = new HashMap<>();
		Value nullLanguageValue = new LiteralImpl("someTitle");
		Value enValue = new LiteralImpl("someTitleEn", "en");
		Value bgValue = new LiteralImpl("someTitleBg", "bg");
		instanceProperties.put("emf:title", new HashSet<>(Arrays.asList(nullLanguageValue, enValue, bgValue)));
		Map<String, Serializable> properties = new HashMap<>();
		converter.convertPropertiesFromSemanticToInternalModel(model, instanceProperties, properties);

		MultiLanguageValue multiLanguageValue = (MultiLanguageValue) properties.get("emf:title");
		Assert.assertEquals("someTitleEn", multiLanguageValue.getValues("en"));
		Assert.assertEquals("someTitleBg", multiLanguageValue.getValues("bg"));
		// This language doesn't exist so we will just return the first one.
		Assert.assertEquals("someTitleEn", multiLanguageValue.getValues("ger"));
	}
}
