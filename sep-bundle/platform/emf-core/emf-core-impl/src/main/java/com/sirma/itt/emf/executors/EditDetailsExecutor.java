package com.sirma.itt.emf.executors;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.emf.executors.ExecutableOperation;
import com.sirma.itt.emf.instance.actions.BaseInstanceExecutor;
import com.sirma.itt.emf.plugin.Extension;


/**
 * Defines an operation for editing any {@link com.sirma.itt.emf.instance.model.Instance}.
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
		return "edit_details";
	}

}
