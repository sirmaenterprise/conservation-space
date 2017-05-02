package com.sirma.itt.seip.eai.model.internal;

import java.util.List;

/**
 * The internal model representation used as an abstraction layer between invocation service and imported objects.
 * Represents a list of items and optional relations for each item.
 *
 * @author bbanchev
 * @param <E>
 *            the element type for result
 */
public class RetrievedInstances<E> extends BatchProcessedInstancesModel<E> {

	private List<RelationInformation> relations;

	/**
	 * Gets the relations. It contains information about:
	 * <ul>
	 * <li>the source instance for the relation</li>
	 * <li>the target instance or {@link ResolvableInstance} with the resolving data</li>
	 * <li>the relation short uri</li>
	 * </ul>
	 * 
	 * @return the relations list
	 */
	public List<RelationInformation> getRelations() {
		return relations;
	}

	/**
	 * Sets the relations model.
	 *
	 * @param relations
	 *            the relations
	 */
	public void setRelations(List<RelationInformation> relations) {
		this.relations = relations;
	}
}
