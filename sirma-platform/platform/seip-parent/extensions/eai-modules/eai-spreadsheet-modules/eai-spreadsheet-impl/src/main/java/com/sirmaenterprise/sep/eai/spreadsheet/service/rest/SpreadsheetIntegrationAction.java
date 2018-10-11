package com.sirmaenterprise.sep.eai.spreadsheet.service.rest;

import java.util.ArrayList;
import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.eai.service.IntegrateExternalObjectsService;
import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.plugin.Extension;
import com.sirmaenterprise.sep.eai.spreadsheet.model.internal.ParsedInstance;

/**
 * Action to delegate import request to {@link IntegrateExternalObjectsService}. Service does not support tx and
 * delegates tx orchestration to integration service.
 * 
 * @author bbanchev
 */
@Extension(target = Action.TARGET_NAME, order = 661)
@ApplicationScoped
public class SpreadsheetIntegrationAction implements Action<SpreadsheetDataIntegrataionRequest> {

	@Inject
	private IntegrateExternalObjectsService integrateExternalObjectsService;

	@Override
	public String getName() {
		return SpreadsheetDataIntegrataionRequest.OPERATION_NAME;
	}

	@Override
	public SpreadsheetOperationReport perform(SpreadsheetDataIntegrataionRequest importRequest) {
		SpreadsheetOperationReport result = new SpreadsheetOperationReport();
		result.setReport(importRequest.getReport().toInstance());
		// each instance is imported in separate transaction
		Collection<ParsedInstance> importedInstances = integrateExternalObjectsService.importInstances(importRequest);
		result.getResult().setInstances(new ArrayList<>(importedInstances));
		return result;
	}

}
