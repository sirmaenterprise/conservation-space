package com.sirma.itt.seip.instance.actions.publish;

import java.util.Set;

import com.sirma.itt.seip.instance.actions.ActionRequest;

/**
 * Request object to execute the publish operation action
 *
 * @author BBonev
 */
public class PublishActionRequest extends ActionRequest {

	private static final long serialVersionUID = -7196248185133377777L;
	protected static final String OPERATION_NAME = "publish";

	private String relationType;
	private Set<String> relatedInstances;

	/**
	 * Gets the operation.
	 *
	 * @return the operation
	 */
	@Override
	public String getOperation() {
		return OPERATION_NAME;
	}

	/**
	 * @return the relationType
	 */
	public String getRelationType() {
		return relationType;
	}

	/**
	 * @return the relatedInstances
	 */
	public Set<String> getRelatedInstances() {
		return relatedInstances;
	}
}
