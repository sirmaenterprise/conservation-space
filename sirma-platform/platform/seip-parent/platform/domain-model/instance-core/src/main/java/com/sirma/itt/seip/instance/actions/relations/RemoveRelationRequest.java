package com.sirma.itt.seip.instance.actions.relations;

import java.util.Map;
import java.util.Set;

import com.sirma.itt.seip.instance.actions.ActionRequest;

/**
 * Request object for removing a relation from an instance.
 *
 * @author BBonev
 */
public class RemoveRelationRequest extends ActionRequest {

	private static final long serialVersionUID = 2038649630591646557L;

	protected static final String OPERATION_NAME = "removeRelation";

	private Map<String, Set<String>> relations;

	@Override
	public String getOperation() {
		return OPERATION_NAME;
	}

	/**
	 * Gets the relations that need to be removed.
	 *
	 * @return the relations
	 */
	public Map<String, Set<String>> getRelations() {
		return relations;
	}

	/**
	 * Sets the relations to remove. The key is the name of the relation and the values are the id of the instances that
	 * need to be unlinked from
	 *
	 * @param relations
	 *            the relations to set
	 */
	public void setRelations(Map<String, Set<String>> relations) {
		this.relations = relations;
	}
}
