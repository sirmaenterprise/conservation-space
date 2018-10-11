package com.sirma.itt.pm.integration.model;

import org.alfresco.service.namespace.QName;

/**
 * The Interface PMModel is the java representation of pm model.
 */
public interface PMModel {
	/** PM Model URI. */
	public static String PM_MODEL_1_0_URI = "http://www.sirmaitt.com/model/pm/1.0";
	/** The pm model prefix. */
	public static String PM_MODEL_PREFIX = "pm";
	/** The type cmf project definition holder. */
	QName TYPE_PM_PROJECT_DEF_SPACE = QName.createQName(PM_MODEL_1_0_URI, "projectdefinitionspace");
	/** The type cmf project instance space. */
	QName TYPE_PM_PROJECT_INSTANCES_SPACE = QName.createQName(PM_MODEL_1_0_URI, "projectinstancesspace");
	/** The type cmf project space. */
	QName TYPE_PM_INSTANCE_SPACE = QName.createQName(PM_MODEL_1_0_URI, "projectspace");
	/** The aspect cmf project. */
	QName ASPECT_PM_PROJECT_INSTANCE = QName.createQName(PM_MODEL_1_0_URI, "project");
	/** The aspect cmf case. */
	QName ASPECT_PM_PROJECT_DEFINITION = QName.createQName(PM_MODEL_1_0_URI, "projectDefinition");
}
