package com.sirma.itt.seip.tenant.step;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.cls.persister.SheetParser;
import com.sirma.itt.emf.cls.persister.SheetPersister;
import com.sirma.itt.emf.cls.validator.CodeValidator;
import com.sirma.itt.emf.cls.validator.SheetValidator;
import com.sirma.itt.emf.cls.validator.exception.CodeValidatorException;
import com.sirma.itt.emf.cls.validator.exception.SheetValidatorException;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.wizard.AbstractTenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.cls.parser.CodeListSheet;

import jxl.Sheet;

/**
 * Step, responsible for validating and uploading the codelists during tenant creation. The codelists file must be in
 * the archive/directory as the definitions and it must be named codelists.xls.
 *
 * @author nvelkov
 */
@Extension(target = TenantStep.CREATION_STEP_NAME, order = 21)
@Extension(target = TenantStep.DELETION_STEP_NAME, order = 1)
public class TenantCodelistStep extends AbstractTenantStep {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String WRONG_CODELIST_FORMAT_MSG = "The provided codelist file wasn't in the correct format!";

	@Inject
	private SheetPersister processor;

	@Inject
	private CodeValidator codeValidator;

	@Inject
	private SheetValidator sheetValidator;

	@Inject
	private SecurityContextManager securityContextManager;

	@Inject
	private SheetParser sheetParser;

	@Inject
	private TransactionSupport transactionSupport;

	@Override
	public boolean execute(TenantStepData data, TenantInitializationContext context) {
		persistCodelists(data, context);
		return true;
	}

	@Override
	public boolean delete(TenantStepData data, TenantInfo tenantInfo, boolean rollback) {
		// If the persist fails just truncate the db table.
		securityContextManager.executeAsTenant(tenantInfo.getTenantId()).executable(processor::delete);
		return true;
	}

	@Override
	public String getIdentifier() {
		return "DMSInitialization";
	}

	private void persistCodelists(TenantStepData data, TenantInitializationContext context) {
		List<File> models = data.getModels();
		Collection<CodeListSheet> sheets = models.stream()
				.map(model -> new File(model.getAbsolutePath() + "/codelists.xls"))
				.filter(File::exists)
				.map(this::getValidatedCodeListSheet)
				.map(this::convertToCodelistSheet)
				.collect(Collectors.toList());

		if (CollectionUtils.isNotEmpty(sheets)) {
			Consumer<CodeListSheet> persistSheet = securityContextManager
					.executeAsTenant(context.getTenantInfo().getTenantId())
					.toWrapper()
					.consumer(this::persistCodelists);
			sheets.forEach(persistSheet);
		}
	}

	private void persistCodelists(CodeListSheet mergedSheet) {
		transactionSupport.invokeInTx(() -> {
			try {
				codeValidator.validateCodeLists(mergedSheet.getCodeLists());
				processor.persist(mergedSheet);
				return null;
			} catch (CodeValidatorException e) {
				LOGGER.info("Codelist validation errors:");
				e.getErrors().forEach(LOGGER::warn);
				throw new TenantCreationException("The provided codelist file, failed to pass the validation phase."
						+ " Check the system log for more information.", e);
			} catch (IllegalArgumentException e) {
				throw new TenantCreationException("The provided codelist file couldn't be persisted!", e);
			}
		});
	}

	private Sheet getValidatedCodeListSheet(File file) {
		try (InputStream fileStream = new FileInputStream(file)) {
			return sheetValidator.getValidatedCodeListSheet(fileStream);
		} catch (SheetValidatorException e) {
			throw new TenantCreationException(WRONG_CODELIST_FORMAT_MSG, e);
		} catch (IOException e) {
			throw new TenantCreationException("The provided codelist file couldn't be opened!", e);
		}
	}

	private CodeListSheet convertToCodelistSheet(Sheet sheet) {
		try {
			return sheetParser.parseFromSheet(sheet);
		} catch (IllegalArgumentException e) {
			throw new TenantCreationException(WRONG_CODELIST_FORMAT_MSG, e);
		}
	}
}
