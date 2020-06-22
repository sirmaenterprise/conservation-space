package com.sirma.itt.seip.annotations.model;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.model.vocabulary.OA;

/**
 * Annotation constants class.
 *
 * @author tdossev
 */
public final class AnnotationProperties {

	public static final String REPLY_PROPERTY = "emf:replyTo";
	public static final String CREATE_ACTION = "create";
	public static final String INIT_STATUS = "INIT";
	public static final String ACTIONS_LABEL = "actions";

	public static final String HAS_TARGET = OA.PREFIX + ":" + OA.HAS_TARGET.getLocalName();

	public static final IRI ACTION = SimpleValueFactory.getInstance().createIRI(EMF.NAMESPACE, "action");

	private AnnotationProperties() {
		// Utility class constructor.
	}

}
