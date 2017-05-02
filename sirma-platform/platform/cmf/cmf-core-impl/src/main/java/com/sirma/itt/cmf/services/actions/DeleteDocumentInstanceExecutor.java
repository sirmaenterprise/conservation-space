package com.sirma.itt.cmf.services.actions;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.instance.actions.DeleteInstanceExecutor;
import com.sirma.itt.seip.instance.actions.ExecutableOperation;
import com.sirma.itt.seip.instance.actions.OperationContext;
import com.sirma.itt.seip.instance.actions.OperationResponse;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Operation executor for document deletion.
 *
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = ExecutableOperation.TARGET_NAME, order = 125)
public class DeleteDocumentInstanceExecutor extends DeleteInstanceExecutor {

	@Override
	public String getOperation() {
		return "deleteDocumentInstance";
	}

	@Override
	public OperationResponse execute(OperationContext context) {
		Options.DO_NOT_CALL_DMS.enable();
		try {
			return super.execute(context);
		} finally {
			Options.DO_NOT_CALL_DMS.disable();
		}
	}
}
