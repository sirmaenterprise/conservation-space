package com.sirma.itt.seip.instance.actions.change.type;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.actions.ActionRequest;

/**
 * Request object to trigger actual instance type change.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 13/02/2019
 */
public class ChangeTypeRequest extends ActionRequest {

	protected static final String OPERATION_NAME = "changeType";

	private Instance instance;

	@Override
	public String getOperation() {
		return OPERATION_NAME;
	}

	public Instance getInstance() {
		return instance;
	}

	/**
	 * Set instance that has it's properties migrated and with final user changes.
	 *
	 * @param instance the instance to save
	 */
	public void setInstance(Instance instance) {
		this.instance = instance;
	}
}
