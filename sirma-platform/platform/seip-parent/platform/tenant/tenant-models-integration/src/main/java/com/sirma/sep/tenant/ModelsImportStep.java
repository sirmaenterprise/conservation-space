package com.sirma.sep.tenant;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.wizard.AbstractTenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.util.file.ArchiveUtil;
import com.sirma.sep.definition.DefinitionImportService;
import com.sirma.sep.model.ModelImportService;

@Extension(target = TenantStep.CREATION_STEP_NAME, order = 23)
public class ModelsImportStep extends AbstractTenantStep {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private ModelImportService modelImportService;

	@Inject
	private SecurityContextManager securityContextManager;

	@Inject
	private DefinitionImportService definitionImportService;

	@Inject
	private TransactionSupport transactionSupport;

	@Override
	public boolean execute(TenantStepData data, TenantInitializationContext context) {
		Map<String, InputStream> fileStreams = new HashMap<>();

		securityContextManager.initializeTenantContext(context.getTenantInfo().getTenantId());

		File modelsZip = null;

		try {
			for (File file : data.getModels()) {
				if (file.isDirectory()) {
					// create zip containing the models and put it in the same directory
					modelsZip = new File(file.getParentFile().getAbsolutePath() + File.separator + file.getName() + ".zip");
					ArchiveUtil.zipFile(file, modelsZip);

					fileStreams.put(modelsZip.getName(), FileUtils.openInputStream(modelsZip));
				} else {
					fileStreams.put(file.getName(), FileUtils.openInputStream(file));
				}
			}

			// The data types have to be initialized before the definition import process
			transactionSupport.invokeInNewTx(definitionImportService::initializeDataTypes);

			// The transaction includes large operations and needs more time to complete
			List<String> errors = transactionSupport.invokeInNewTx(() ->  modelImportService.importModel(fileStreams), 10, TimeUnit.MINUTES);

			if (!errors.isEmpty()) {
				LOGGER.warn("Models import failed. Errors:");
				errors.forEach(LOGGER::warn);
				throw new TenantCreationException("Validation errors found during models import");
			}

			return true;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} finally {
			securityContextManager.endContextExecution();
			fileStreams.values().forEach(IOUtils::closeQuietly);

			if (modelsZip != null) {
				FileUtils.deleteQuietly(modelsZip);
			}
		}
	}

	@Override
	public String getIdentifier() {
		return "DMSInitialization";
	}

	@Override
	public boolean delete(TenantStepData data, TenantInfo tenantInfo, boolean arg2) {
		return false;
	}

}
