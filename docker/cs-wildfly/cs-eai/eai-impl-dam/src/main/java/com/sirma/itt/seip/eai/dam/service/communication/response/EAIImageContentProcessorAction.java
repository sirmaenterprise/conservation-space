package com.sirma.itt.seip.eai.dam.service.communication.response;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.exception.EAIReportableException;
import com.sirma.itt.seip.eai.model.communication.RequestInfo;
import com.sirma.itt.seip.eai.model.communication.ResponseInfo;
import com.sirma.itt.seip.eai.model.internal.RetrievedInstances;
import com.sirma.itt.seip.eai.service.communication.BaseEAIServices;
import com.sirma.itt.seip.eai.service.communication.EAICommunicationService;
import com.sirma.itt.seip.eai.service.communication.request.EAIRequestProvider;
import com.sirma.itt.seip.eai.service.communication.response.EAIResponseReader;
import com.sirma.itt.seip.exception.EmfException;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.tasks.SchedulerActionAdapter;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.content.rendition.ThumbnailService;

/**
 * Invoke content import on existing item. Action handler makes use of the correct EAI services to invoke and parse
 * response. As a final step the processed instance is persisted with the last modifications and thumbnails are
 * registered
 * 
 * @author bbanchev
 */
@ApplicationScoped
@Named(EAIImageContentProcessorAction.NAME)
public class EAIImageContentProcessorAction extends SchedulerActionAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	/** Action name. */
	public static final String NAME = "EAIImageContentProcessorAction";
	/** Full information for the required request */
	public static final String EAI_REQUEST_INFO = "requestInfo";

	private static final List<Pair<String, Class<?>>> PARAM_VALIDATION = Collections
			.singletonList(new Pair<>(EAI_REQUEST_INFO, RequestInfo.class));

	@Inject
	private InstanceService instanceService;
	@Inject
	private EAICommunicationService communicationService;
	@Inject
	private EAIResponseReader responseReader;
	@Inject
	private EAIRequestProvider requestProvider;
	@Inject
	private ThumbnailService thumbnailService;
	@Inject
	private LinkService linkService;
	@Inject
	private TransactionSupport transactionSupport;

	@Override
	public void execute(SchedulerContext context) throws Exception {

		// get the content
		RequestInfo request = context.getIfSameType(EAI_REQUEST_INFO, RequestInfo.class);
		LOGGER.debug("Executing request {} ", request);
		try {
			ResponseInfo response = communicationService.invoke(request);
			// import the response to the instance
			RetrievedInstances<Instance> result = responseReader.parseResponse(response);
			// need to save again the object to update mimetype and filesize
			transactionSupport.invokeConsumerInNewTx(this::savedParsedResults, result);
		} catch (EAIReportableException e) {
			try {
				RequestInfo error = requestProvider.provideRequest(request.getSystemId(), BaseEAIServices.LOGGING, e);
				communicationService.invoke(error);
			} catch (EAIException eai) {// NOSONAR
				throw new EmfRuntimeException(
						"Failed to execute image download for " + request + " and subsequent notification error: "
								+ eai.getLocalizedMessage() + "! Original cause: " + e.getLocalizedMessage(),
						e);
			}
			throw new EmfRuntimeException("Failed to execute image download for " + request + "! Original cause: "
					+ e.getLocalizedMessage() + "! System is notified!", e);

		} catch (Exception e) {
			throw new EmfException("Failed to execute image download request :  " + request, e);
		}
	}

	private void savedParsedResults(RetrievedInstances<Instance> result) {
		for (Instance instance : result.getInstances()) {
			instanceService.save(instance, new Operation(ActionTypeConstants.EDIT_DETAILS));
			registerImageAsThumbnail(instance);
		}
	}

	/**
	 * Register the given image source as thumbnail of all the related Objects.
	 * 
	 * @param source
	 *            the source image we use for thumbnail.
	 */
	private void registerImageAsThumbnail(Instance source) {
		thumbnailService.register(source);
		List<LinkReference> linksTo = linkService.getLinks(source.toReference(), LinkConstants.IS_THUMBNAIL_OF);
		for (LinkReference targetReference : linksTo) {
			Instance target = targetReference.getTo().toInstance();
			thumbnailService.register(target, source);
		}
	}

	@Override
	protected List<Pair<String, Class<?>>> validateInput() {
		return PARAM_VALIDATION;
	}

}
