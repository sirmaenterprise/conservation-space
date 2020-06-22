package com.sirma.itt.seip.domain;

/**
 * Defines identifiers for the working objects in the application. The constants are used also for identifiers in
 * qualifiers and definitions.
 *
 * @author BBonev
 */
public interface ObjectTypes {

	/** The instance. */
	String INSTANCE = "CommonInstance";
	/** The default. */
	String DEFAULT = "default";
	/** The link. */
	String LINK = "link";
	/**
	 * Class category for link references and link instances
	 */
	String LINK_REFERENCE = "linkreference";
	/** The resource. */
	String RESOURCE = "resource";
	/** The annotation instance. */
	String ANNOTATION = "annotation";
	/** The topic instance. */
	String TOPIC = "topic";
	/** The user. */
	String USER = "user";
	/** The group. */
	String GROUP = "group";
	/** The image annotation. */
	String IMAGE_ANNOTATION = "imageannotation";

	/** The type for deleted instances */
	String ARCHIVED = "archived";

	/** The type for rules. */
	String RULE = "rule";

	/** The object. */
	String OBJECT = "object";
	/** The class. */
	String CLASS = "class";

	/** The case qualifier. */
	String CASE = "case";
	/** The folder qualifier. */
	String FOLDER = "folder";
	/** The document qualifier. */
	String DOCUMENT = "document";
	/** The workflow qualifier. */
	String WORKFLOW = "workflow";
	/** The task. */
	String WORKFLOW_TASK = "workflowTask";
	/** The standalone task. */
	String STANDALONE_TASK = "task";
	/** The section. */
	String SECTION = "section";
	/** The template. */
	String TEMPLATE = "template";
	/** The generic. */
	String GENERIC = "generic";

}
