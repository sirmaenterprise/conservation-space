package com.sirma.itt.seip.instance.actions;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.actions.ExecutableOperation;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Defines an operation for editing any {@link com.sirma.itt.seip.domain.instance.Instance}.
 *
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = ExecutableOperation.TARGET_NAME, order = 10)
public class EditDetailsExecutor extends BaseInstanceExecutor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getOperation() {
		return ActionTypeConstants.EDIT_DETAILS;
	}

}
