package com.sirma.itt.emf.semantic.persistence;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Assert;
import org.junit.Test;

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
		Value value = SimpleValueFactory.getInstance().createLiteral("someTitle");
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
		model.setFields(Collections.singletonList(field));

		Map<String, Set<Value>> instanceProperties = new HashMap<>();
		SimpleValueFactory factory = SimpleValueFactory.getInstance();
		Value nullLanguageValue = factory.createLiteral("someTitle");
		Value enValue = factory.createLiteral("someTitleEn", "en");
		Value bgValue = factory.createLiteral("someTitleBg", "bg");
		instanceProperties.put("emf:title", new HashSet<>(Arrays.asList(nullLanguageValue, enValue, bgValue)));
		Map<String, Serializable> properties = new HashMap<>();
		converter.convertPropertiesFromSemanticToInternalModel(model, instanceProperties, properties);

		MultiLanguageValue multiLanguageValue = (MultiLanguageValue) properties.get("emf:title");
		Assert.assertEquals("someTitleEn", multiLanguageValue.getValues("en").findFirst().orElse(null));
		Assert.assertEquals("someTitleBg", multiLanguageValue.getValues("bg").findFirst().orElse(null));
		// This language doesn't exist so we will just return empty stream
		Assert.assertNull(multiLanguageValue.getValues("ger").findFirst().orElse(null));
	}
}
