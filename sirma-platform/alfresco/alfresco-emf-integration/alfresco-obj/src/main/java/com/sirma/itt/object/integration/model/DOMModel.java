package com.sirma.itt.object.integration.model;

import org.alfresco.service.namespace.QName;

/**
 * The Interface ObjectsModel is the java representation of domain object model.
 */
public interface DOMModel {
	/** PM Model URI. */
	public static String DOM_MODEL_1_0_URI = "http://www.sirmaitt.com/model/dom/1.0";
	/** The pm model prefix. */
	public static String DOM_MODEL_PREFIX = "dom";
	/** The type cmf object definition holder. */
	QName TYPE_DOM_OBJECT_DEF_SPACE = QName.createQName(DOM_MODEL_1_0_URI, "objectdefinitionspace");
	/** The type cmf object instance space. */
	QName TYPE_DOM_OBJECT_INSTANCES_SPACE = QName.createQName(DOM_MODEL_1_0_URI,
			"objectinstancesspace");
	/** The type cmf object space. */
	QName TYPE_DOM_OBJECT_SPACE = QName.createQName(DOM_MODEL_1_0_URI, "objectspace");
	/** The aspect cmf object. */
	QName ASPECT_DOM_OBJECT_INSTANCE = QName.createQName(DOM_MODEL_1_0_URI, "object");
	/** The aspect cmf object definition. */
	QName ASPECT_DOM_OBJECT_DEFINITION = QName.createQName(DOM_MODEL_1_0_URI, "objectDefinition");
}
