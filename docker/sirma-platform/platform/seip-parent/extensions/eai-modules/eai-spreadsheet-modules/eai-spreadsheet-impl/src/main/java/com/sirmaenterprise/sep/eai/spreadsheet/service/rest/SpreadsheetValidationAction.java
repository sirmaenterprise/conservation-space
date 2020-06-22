package com.sirmaenterprise.sep.eai.spreadsheet.service.rest;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.CONTENT_LENGTH;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.MIMETYPE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.NAME;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.PRIMARY_CONTENT_ID;

import java.lang.invoke.MethodHandles;
import java.util.Calendar;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.eai.exception.EAIReportableException;
import com.sirma.itt.seip.eai.exception.EAIRuntimeException;
import com.sirma.itt.seip.eai.model.communication.RequestInfo;
import com.sirma.itt.seip.eai.model.communication.ResponseInfo;
import com.sirma.itt.seip.eai.service.communication.EAICommunicationService;
import com.sirma.itt.seip.eai.service.communication.request.EAIRequestProvider;
import com.sirma.itt.seip.eai.service.communication.response.EAIResponseReader;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.time.ISO8601DateFormat;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirmaenterprise.sep.eai.spreadsheet.configuration.SpreadsheetIntegrationConfiguration;
import com.sirmaenterprise.sep.eai.spreadsheet.configuration.SpreadsheetIntegrationConfigurationProvider;
import com.sirmaenterprise.sep.eai.spreadsheet.model.SpreadsheetEAIServices;
import com.sirmaenterprise.sep.eai.spreadsheet.model.error.SpreadsheetValidationReport;
import com.sirmaenterprise.sep.eai.spreadsheet.model.internal.SpreadsheetResultInstances;
import com.sirmaenterprise.sep.eai.spreadsheet.model.request.arg.ReadRequestArgument;

/**
 * Action handler for spreadsheet validation. Contains method invocation to service adapters for service
 * {@link SpreadsheetEAIServices#PREPARE}. The main method {@link #read(SpreadSheetReadRequest)} generates as a result
 * {@link SpreadsheetOperationReport}.
 * 
 * @author bbanchev
 */
@Extension(target = Action.TARGET_NAME, order = 660)
@ApplicationScoped
public class SpreadsheetValidationAction implements Action<SpreadSheetReadRequest> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	@Inject
	private SpreadsheetIntegrationConfiguration eaiConfiguration;
	@Inject
	private EAICommunicationService communicationService;
	@Inject
	private EAIRequestProvider requestProvider;
	@Inject
	private EAIResponseReader responseReader;
	@Inject
	private DomainInstanceService domainInstanceService;
	@Inject
	private InstanceContentService instanceContentService;
	@Inject
	private SecurityContextManager securityContextManager;
	@Inject
	private TransactionSupport transactionSupport;

	@Override
	public String getName() {
		return SpreadSheetReadRequest.OPERATION_NAME;
	}

	@Override
	public Object perform(SpreadSheetReadRequest request) {
		return read(request);
	}

	/**
	 * Reads and validates and instance with spreadsheet content. {@link SpreadsheetOperationReport} holds the error in
	 * case if content is not valid or there is any other reportable error
	 * 
	 * @param readRequest
	 *            is the provided request
	 * @return the generated {@link SpreadsheetOperationReport} containing the report instance and the parsed instances
	 */
	public SpreadsheetOperationReport read(SpreadSheetReadRequest readRequest) {
		String spreadsheetSystemId = SpreadsheetIntegrationConfigurationProvider.SYSTEM_ID;
		SpreadsheetValidationReport validationReport = new SpreadsheetValidationReport();
		SpreadsheetOperationReport result = new SpreadsheetOperationReport();
		try {
			// throws EAIReportableException on wrong format
			RequestInfo request = requestProvider.provideRequest(spreadsheetSystemId, SpreadsheetEAIServices.PREPARE,
					new ReadRequestArgument(readRequest.getTargetReference(), readRequest.getContext()));
			// now parse sheet - should not throw exception
			ResponseInfo response = communicationService.invoke(request);
			// throws validation errors
			result.setResult(responseReader.parseResponse(response));
		} catch (EAIReportableException e) {
			logError(validationReport, e);
		} catch (Exception e) {
			logError(validationReport, "Failed to read data integration instance " + readRequest.getTargetId()
					+ ". Check log for more details!");
			generateResult(readRequest.getTargetReference(), validationReport, result);
			LOGGER.error("Error during read of spreadsheet model!", e);
			throw new EAIRuntimeException("Failed to read data integration instance " + readRequest.getTargetId(), e);
		}
		return generateResult(readRequest.getTargetReference(), validationReport, result);
	}

	private SpreadsheetOperationReport generateResult(InstanceReference spreadsheet,
			SpreadsheetValidationReport validationReport, SpreadsheetOperationReport result) {
		SpreadsheetResultInstances resultModel = (SpreadsheetResultInstances) result.getResult();
		if (resultModel != null && resultModel.getError() != null) {
			validationReport.append(resultModel.getError().getMessage());
		}
		if (!validationReport.hasErrors()) {
			// spreadsheet system checked successfully the file
			validationReport.append(SpreadsheetValidationReport.MSG_SUCCESSFUL_VALIDATION);
		}
		Executable executable = securityContextManager.executeAsSystem().toWrapper().executable(() -> {
			Instance report = domainInstanceService.createInstance(eaiConfiguration.getReportDefinitionId().get(),
					spreadsheet.getId());
			String title = "FileImportValidation_"
					+ ISO8601DateFormat.format(Calendar.getInstance()).replaceAll(":", "_") + ".txt";
			report.add(DefaultProperties.TITLE, title);
			Content reportContent = Content
					.createEmpty()
					.setContent(validationReport.toString(), (String) null)
					.setMimeType("text/plain")
					.setName(title)
					.setPurpose(Content.PRIMARY_CONTENT);
			ContentInfo savedContent = instanceContentService.saveContent(report, reportContent);
			report.add(PRIMARY_CONTENT_ID, savedContent.getContentId());
			report.add(CONTENT_LENGTH, savedContent.getLength());
			report.add(NAME, savedContent.getName());
			report.add(MIMETYPE, savedContent.getMimeType());
			domainInstanceService.save(InstanceSaveContext.create(report, new Operation(ActionTypeConstants.CREATE)));
			result.setReport(report);
		});

		transactionSupport.invokeInNewTx(executable);

		return result;
	}

	private static void logError(SpreadsheetValidationReport validationReport, Object error) {
		if (error instanceof Throwable) {
			validationReport.append(((Throwable) error).getMessage());
		} else {
			validationReport.append(error);
		}
	}
}
