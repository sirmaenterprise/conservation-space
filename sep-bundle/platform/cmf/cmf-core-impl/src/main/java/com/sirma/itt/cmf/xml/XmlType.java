package com.sirma.itt.cmf.xml;

import com.sirma.itt.cmf.xml.schema.CmfSchemaBuilder;
import com.sirma.itt.emf.xml.XmlSchemaProvider;

/**
 * XmlType enum that defines the default XSD providers.
 *
 * @author BBonev
 */
public enum XmlType implements XmlSchemaProvider {

	/** The document definitions. */
	DOCUMENT_DEFINITIONS("document", "case.xsd", "common.xsd"),
	/** The case definition. */
	CASE_DEFINITION("case", "case.xsd", "common.xsd"),
	/** The types definition. */
	TYPES_DEFINITION("types", "types.xsd"),
	/** The task definitions. */
	TASK_DEFINITIONS("task", "workflowDefinition.xsd", "common.xsd"),
	/** The workflow definition. */
	WORKFLOW_DEFINITION("workflow", "workflowDefinition.xsd", "common.xsd"),
	/** The template definition. */
	TEMPLATE_DEFINITION("template", "templates.xsd", "common.xsd"),
	/** The generic definition. */
	GENERIC_DEFINITION("generic", "generic.xsd", "common.xsd");
	private String id;
	private String mainSchema;
	private String[] additional;

	/**
	 * Instantiates a new xml type.
	 *
	 * @param id
	 *            the id
	 * @param mainSchema
	 *            the main schema
	 * @param additional
	 *            the additional
	 */
	private XmlType(String id, String mainSchema, String... additional) {
		this.id = id;
		this.mainSchema = mainSchema;
		this.additional = additional;
	}

	@Override
	public Class<?> baseLoaderClass() {
		return CmfSchemaBuilder.class;
	}

	@Override
	public String getIdentifier() {
		return id;
	}

	@Override
	public String getMainSchemaFileLocation() {
		return mainSchema;
	}

	@Override
	public String[] getAdditionalSchemaLocations() {
		return additional;
	}
}
