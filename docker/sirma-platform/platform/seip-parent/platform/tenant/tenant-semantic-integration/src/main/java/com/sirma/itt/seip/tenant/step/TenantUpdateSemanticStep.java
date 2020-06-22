/**
 *
 */
package com.sirma.itt.seip.tenant.step;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import com.sirma.itt.seip.patch.exception.PatchFailureException;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.semantic.patch.BackingPatchService;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.wizard.AbstractTenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantUpdateException;

/**
 * Tenant creation step that creates semantic repository.
 *
 * @author BBonev
 */
@Extension(target = TenantStep.UPDATE_STEP_NAME, order = 10)
public class TenantUpdateSemanticStep extends AbstractTenantStep {

	public static final String STEP_NAME = "SemanticDbUpdate";

	@Inject
	private SecurityContextManager securityContextManager;
	@Inject
	private BackingPatchService patchUtilService;

	@Override
	public String getIdentifier() {
		return STEP_NAME;
	}

	@Override
	public boolean execute(TenantStepData data, TenantInitializationContext context) {
		String tenantId = context.getTenantInfo().getTenantId();
		securityContextManager.initializeTenantContext(tenantId);
		try {
			List<File> models = data.getModels();
			for (File definitionFile : models) {
				patchUtilService.runPatchAndBackup(definitionFile, tenantId);
			}
		} catch (PatchFailureException e) {
			throw new TenantUpdateException("Cound not patch database due to ", e);
		} finally {
			securityContextManager.endContextExecution();
		}
		return true;
	}
}