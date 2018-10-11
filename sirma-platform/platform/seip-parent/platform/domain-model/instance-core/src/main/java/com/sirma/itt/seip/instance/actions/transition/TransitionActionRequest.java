package com.sirma.itt.seip.instance.actions.transition;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.actions.ActionRequest;

/**
 * When there is a request for transition operation execution, this class is used to contain the request information.
 * From the passed JSON is build object of this type. It is used for all transition operations. Extends
 * {@link ActionRequest}.
 *
 * @author A. Kunchev
 */
public class TransitionActionRequest extends ActionRequest {

	protected static final String OPERATION_NAME = "transition";

	private static final long serialVersionUID = 28222007700931031L;

	private Instance targetInstance;

	@Override
	public String getOperation() {
		return OPERATION_NAME;
	}

	/**
	 * Getter for the target instance. The instance is needed, id there are mandatory fields that should be filled,
	 * before the action execution.
	 *
	 * @return the targetInstance
	 */
	public Instance getTargetInstance() {
		return targetInstance;
	}

	/**
	 * Setter for the target instance. The instance is needed, id there are mandatory fields that should be filled,
	 * before the action execution.
	 *
	 * @param targetInstance
	 *            the targetInstance to set
	 */
	public void setTargetInstance(Instance targetInstance) {
		this.targetInstance = targetInstance;
	}

}
