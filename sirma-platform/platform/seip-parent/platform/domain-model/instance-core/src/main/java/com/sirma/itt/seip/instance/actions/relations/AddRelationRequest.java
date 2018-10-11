package com.sirma.itt.seip.instance.actions.relations;

import java.util.Map;
import java.util.Set;

import com.sirma.itt.seip.instance.actions.ActionRequest;

/**
 * Request object for adding a relation to an instance.
 *
 * @author BBonev
 */
public class AddRelationRequest extends ActionRequest {

	private static final long serialVersionUID = -2106372192452160218L;

	protected static final String OPERATION_NAME = "addRelation";

	private Map<String, Set<String>> relations;

	private boolean removeExisting;

	@Override
	public String getOperation() {
		return OPERATION_NAME;
	}

	/**
	 * Gets the relations that need to be added.
	 *
	 * @return the relations
	 */
	public Map<String, Set<String>> getRelations() {
		return relations;
	}

	/**
	 * Sets the relations to add. The key is the name of the relation and the values are the id of the instances that
	 * need to linked to
	 *
	 * @param relations
	 *            the relations to set
	 */
	public void setRelations(Map<String, Set<String>> relations) {
		this.relations = relations;
	}

	/**
	 * Shows if existing relations should be removed.
	 *
	 * @return the removeExisting
	 */
	public boolean isRemoveExisting() {
		return removeExisting;
	}

	/**
	 * Sets if existing relations should be removed.
	 *
	 * @param removeExisting
	 *            the removeExisting to set
	 */
	public void setRemoveExisting(boolean removeExisting) {
		this.removeExisting = removeExisting;
	}
}
