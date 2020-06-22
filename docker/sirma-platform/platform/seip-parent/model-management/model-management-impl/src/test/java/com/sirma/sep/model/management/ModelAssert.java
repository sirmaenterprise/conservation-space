package com.sirma.sep.model.management;

import static com.sirma.sep.model.management.ModelsFakeCreator.createStringMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import org.eclipse.rdf4j.model.IRI;

import com.sirma.sep.model.ModelNode;

/**
 * Utility methods for asserting {@link ModelAttribute} and {@link ModelNode}
 *
 * @author Mihail Radkov
 */
public class ModelAssert {

	public static void assertModelNodeLabels(ModelNode modelNode, String... expectedLabels) {
		Map<String, String> expectedMap = createStringMap(expectedLabels);
		int actualSize = modelNode.getLabels() != null ? modelNode.getLabels().size() : 0;

		assertEquals(expectedMap.size(), actualSize);
		expectedMap.forEach((lang, expectedDescription) -> assertEquals(expectedDescription, modelNode.getLabels().get(lang)));
	}

	public static void hasMultiLangStringAttribute(ModelNode modelNode, String attributeId, String... expectedMultiLangStrings) {
		Map<String, String> expectedMultiLanguageValues = createStringMap(expectedMultiLangStrings);
		Optional<ModelAttribute> attribute = modelNode.getAttribute(attributeId);
		assertTrue(attribute.isPresent());
		Map<String, String> multiLangValues = (Map<String, String>) attribute.get().getValue();
		expectedMultiLanguageValues.forEach((land, value) -> assertEquals(multiLangValues.get(land), value));
	}

	public static void hasAttribute(ModelNode model, String attributeName, String attributeType, Object attributeValue) {
		// Without hierarchy resolving
		Optional<ModelAttribute> attribute = model.getAttribute(attributeName);
		assertTrue(attribute.isPresent());
		assertAttribute(attribute.get(), attributeName, attributeType, attributeValue);
	}

	public static void hasAttribute(ModelNode model, IRI attribute, String attributeType, Serializable attributeValue) {
		hasAttribute(model, attribute.toString(), attributeType, attributeValue);
	}

	public static void hasInheritedAttribute(ModelNode model, String attributeName, String attributeType, Object attributeValue) {
		// WITH hierarchy resolving
		Optional<ModelAttribute> attribute = model.findAttribute(attributeName);
		assertTrue(attribute.isPresent());
		assertAttribute(attribute.get(), attributeName, attributeType, attributeValue);
	}

	private static void assertAttribute(ModelAttribute attribute, String attributeName, String attributeType, Object attributeValue) {
		assertEquals(attributeName, attribute.getName());
		assertEquals(attributeValue, attribute.getValue());
		assertEquals(attributeType, attribute.getDataType());
	}

	public static void assertAttributeNotPresent(ModelNode modelNode, String attributeId) {
		modelNode.getAttribute(attributeId).ifPresent(attribute -> fail());
	}
}
