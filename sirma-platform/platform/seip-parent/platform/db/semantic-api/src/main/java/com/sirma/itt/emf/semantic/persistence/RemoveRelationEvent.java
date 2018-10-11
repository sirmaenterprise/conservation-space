package com.sirma.itt.emf.semantic.persistence;

import java.util.function.Function;

import org.eclipse.rdf4j.model.IRI;

import com.sirma.itt.seip.domain.instance.event.ObjectPropertyRemoveEvent;

/**
 * Low level event that is fired before adding new relation statement. The event observer could provide additional
 * statements to be added to the database if needed.
 *
 * @author BBonev
 */
public class RemoveRelationEvent extends BaseRelationChangeEvent implements ObjectPropertyRemoveEvent {

	/**
	 * Instantiates a new removes the relation event.
	 *
	 * @param statementTrigger
	 *            the statement trigger
	 * @param shortUriConverter
	 *            the short uri converter
	 */
	public RemoveRelationEvent(LocalStatement statementTrigger, Function<IRI, String> shortUriConverter) {
		super(statementTrigger, shortUriConverter);
	}
}
