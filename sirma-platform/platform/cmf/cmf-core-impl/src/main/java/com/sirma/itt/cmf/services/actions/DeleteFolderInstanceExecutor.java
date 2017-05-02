package com.sirma.itt.cmf.services.actions;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.instance.actions.DeleteInstanceExecutor;
import com.sirma.itt.seip.instance.actions.OperationContext;
import com.sirma.itt.seip.instance.actions.OperationResponse;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Operation executor for folder/section deletion.
 *
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = DeleteFolderInstanceExecutor.TARGET_NAME, order = 127)
public class DeleteFolderInstanceExecutor extends DeleteInstanceExecutor {

	@Override
	public String getOperation() {
		return "deleteFolderInstance";
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
