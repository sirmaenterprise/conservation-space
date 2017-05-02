package com.sirma.itt.seip.tenant.step;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.sirma.itt.emf.cls.entity.CodeListSheet;
import com.sirma.itt.emf.cls.persister.PersisterException;
import com.sirma.itt.emf.cls.persister.SheetParser;
import com.sirma.itt.emf.cls.persister.XLSProcessor;
import com.sirma.itt.emf.cls.validator.XLSValidator;
import com.sirma.itt.emf.cls.validator.XLSValidatorException;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.wizard.AbstractTenantCreationStep;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStep;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;

import jxl.Sheet;

/**
 * Step, responsible for validating and uploading the codelists during tenant creation. The codelists file must be in
 * the archive/directory as the definitions and it must be named codelists.xls.
 *
 * @author nvelkov
 */
@Extension(target = TenantStep.CREATION_STEP_NAME, order = 21)
public class TenantCreationCodelistStep extends AbstractTenantCreationStep {

	@Inject
	private XLSProcessor processor;
	@Inject
	private XLSValidator validator;
	@Inject
	private SecurityContextManager securityContextManager;
	@Inject
	private SheetParser sheetParser;

	@Override
	public boolean execute(TenantStepData data, TenantInitializationContext context) {
		persistCodelists(data, context);
		return true;
	}

	@Override
	public boolean rollback(TenantStepData data, TenantInitializationContext context) {
		// If the persist fails just truncate the db table.
		processor.deleteCodeLists();
		return true;
	}

	@Override
	public String getIdentifier() {
		return "DMSInitialization";
	}

	private void persistCodelists(TenantStepData data, TenantInitializationContext context) {
		List<File> models = data.getModels();
		Collection<CodeListSheet> sheets = models
				.stream()
					.map(model -> new File(model.getAbsolutePath() + "/codelists.xls"))
					.filter(File::exists)
					.map(this::getValidatedCodeListSheet)
					.map(this::convertToCodelistSheet)
					.collect(Collectors.toList());

		if (CollectionUtils.isNotEmpty(sheets)) {
			CodeListSheet mergedSheet = processor.mergeSheets(sheets);
			securityContextManager.initializeTenantContext(context.getTenantInfo().getTenantId());
			try {
				processor.persistSheet(mergedSheet);
			} catch (PersisterException e) {
				throw new TenantCreationException(
						"The provided codelist xls file in the DMSInitialization couldn't be persisted!", e);
			}
			securityContextManager.endContextExecution();
		}
	}

	private Sheet getValidatedCodeListSheet(File file) {
		try (InputStream fileStream = new FileInputStream(file)) {
			return validator.getValidatedCodeListSheet(fileStream);
		} catch (XLSValidatorException e) {
			throw new TenantCreationException(
					"The provided codelist xls file in the DMSInitialization wasn't in the correct format!", e);
		} catch (IOException e) {
			throw new TenantCreationException(
					"The provided codelist xls file in the DMSInitialization couldn't be opened!", e);
		}
	}

	private CodeListSheet convertToCodelistSheet(Sheet sheet) {
		try {
			return sheetParser.parseXLS(sheet);
		} catch (PersisterException e) {
			throw new TenantCreationException(
					"The provided codelist xls file in the DMSInitialization wasn't in the correct format!", e);
		}
	}
}
