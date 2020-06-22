package com.sirmaenterprise.sep.eai.spreadsheet.service.communication;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.model.ServiceResponse;
import com.sirma.itt.seip.eai.model.communication.RequestInfo;
import com.sirma.itt.seip.eai.service.communication.BaseEAIServices;
import com.sirma.itt.seip.eai.service.communication.EAICommunicationServiceAdapter;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirmaenterprise.sep.eai.spreadsheet.configuration.SpreadsheetIntegrationConfigurationProvider;
import com.sirmaenterprise.sep.eai.spreadsheet.exception.EAIUnsupportedContentException;
import com.sirmaenterprise.sep.eai.spreadsheet.model.SpreadsheetEAIServices;
import com.sirmaenterprise.sep.eai.spreadsheet.model.request.SpreadsheetIntegrationServiceRequest;
import com.sirmaenterprise.sep.eai.spreadsheet.model.request.SpreadsheetReadServiceRequest;
import com.sirmaenterprise.sep.eai.spreadsheet.model.response.SpreadsheetSheet;
import com.sirmaenterprise.sep.eai.spreadsheet.service.SpreadsheetParser;

/**
 * The communication adapter accepts {@link SpreadsheetReadServiceRequest} and
 * {@link SpreadsheetIntegrationServiceRequest} and request parsing to {@link SpreadsheetParser} service. The result
 * model is instance of {@link SpreadsheetSheet}
 * 
 * @author bbanchev
 */
@Singleton
@Extension(target = EAICommunicationServiceAdapter.PLUGIN_ID, order = 5)
public class ContentInfoCommunicationServiceAdapter implements EAICommunicationServiceAdapter {
	@Inject
	private InstanceContentService instanceContentService;

	@Inject
	private SpreadsheetParser spreadsheetParser;

	@Override
	public ServiceResponse invoke(RequestInfo requestInfo) throws EAIException {
		if (SpreadsheetEAIServices.PREPARE.equals(requestInfo.getServiceId())) {
			SpreadsheetReadServiceRequest request = (SpreadsheetReadServiceRequest) requestInfo.getRequest();
			return spreadsheetParser.parseEntries(getContentInfo(request), null);
		} else if (SpreadsheetEAIServices.RETRIEVE.equals(requestInfo.getServiceId())) {
			SpreadsheetIntegrationServiceRequest request = (SpreadsheetIntegrationServiceRequest) requestInfo
					.getRequest();
			return spreadsheetParser.parseEntries(getContentInfo(request), request.getRequestedEntries());
		} else if (BaseEAIServices.LOGGING.equals(requestInfo.getServiceId())) {
			//
		}
		return null;
	}

	private ContentInfo getContentInfo(SpreadsheetReadServiceRequest sourceArgument) throws EAIException {
		InstanceReference source = sourceArgument.getSource();
		if (source == null) {
			throw new EAIUnsupportedContentException("Missing content information to extract spreadsheet data from!");
		}
		ContentInfo contentInfo = instanceContentService.getContent(source.getId(), Content.PRIMARY_CONTENT);
		if (contentInfo != null) {
			if (isSupported(contentInfo)) {
				return contentInfo;
			}
			throw new EAIUnsupportedContentException(
					"Content of type: " + contentInfo.getMimeType() + " is not supported!");

		}
		throw new EAIException(
				"Unsupported type of spreadsheet integration source value. Expected 'Instance' or 'Content', recieved: "
						+ sourceArgument);
	}

	private boolean isSupported(ContentInfo content) {
		return spreadsheetParser.isSupported(content);
	}

	@Override
	public String getName() {
		return SpreadsheetIntegrationConfigurationProvider.SYSTEM_ID;
	}

}
