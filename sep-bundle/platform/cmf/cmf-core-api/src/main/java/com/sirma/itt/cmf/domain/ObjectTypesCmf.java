package com.sirma.itt.cmf.domain;

import com.sirma.itt.emf.domain.ObjectTypes;

/**
 * The list of different implemented objects in the CMF module. These constants could be used for
 * qualifier selectors on beans that are responsible for the concrete classes.
 * 
 * @author BBonev
 */
public interface ObjectTypesCmf extends ObjectTypes {

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
