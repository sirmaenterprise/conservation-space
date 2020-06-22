package com.sirma.itt.seip.eai.dam.service.communication.response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.mime.MimeType;
import org.docx4j.wml.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.eai.cs.model.CSItemRecord;
import com.sirma.itt.seip.eai.cs.model.CSItemRelations;
import com.sirma.itt.seip.eai.cs.model.internal.CSExternalInstanceId;
import com.sirma.itt.seip.eai.cs.service.communication.response.CSResponseReaderAdapter;
import com.sirma.itt.seip.eai.dam.configuration.DAMIntegrationConfigurationProvider;
import com.sirma.itt.seip.eai.dam.service.communication.DAMEAIServices;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.exception.EAIReportableException;
import com.sirma.itt.seip.eai.model.communication.RequestInfo;
import com.sirma.itt.seip.eai.model.communication.ResponseInfo;
import com.sirma.itt.seip.eai.model.error.ErrorBuilderProvider;
import com.sirma.itt.seip.eai.model.internal.ProcessedInstanceModel;
import com.sirma.itt.seip.eai.model.internal.RetrievedInstances;
import com.sirma.itt.seip.eai.model.response.StreamingResponse;
import com.sirma.itt.seip.eai.service.communication.response.EAIResponseReaderAdapter;
import com.sirma.itt.seip.eai.service.model.transform.EAIModelConverter;
import com.sirma.itt.seip.eai.service.model.transform.impl.DefaultModelConverter;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntry;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.tasks.TransactionMode;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentImport;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;

/**
 * DAM response adapter that handles specific requirements for DAM integrations
 *
 * @author bbanchev
 */
@ApplicationScoped
@Extension(target = EAIResponseReaderAdapter.PLUGIN_ID, order = 11)
public class DAMResponseReaderAdapter extends CSResponseReaderAdapter {

	private static final String KEY_FINGERPRINT = "fingerprint";
	private static final Long SCHEDULE_DELAY = Long.valueOf(300L);
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultModelConverter.class);
	@Inject
	private InstanceContentService instanceContentService;
	@Inject
	private SchedulerService schedulerService;
	@Inject
	private TempFileProvider tempFileProvider;
	@Inject
	private TransactionSupport transactionSupport;

	/**
	 * Added support for parsing {@link DAMEAIServices#CONTENT} responses
	 *
	 * @param response
	 *            is the response to parse
	 */
	@Override
	public <R extends ProcessedInstanceModel> R parseResponse(ResponseInfo response) throws EAIException {
		// on content import map a content to instance
		if (response.getServiceId() == DAMEAIServices.CONTENT) {
			return parseContentImport(response);
		}
		return super.parseResponse(response);
	}

	private void scheduleAsynchContentDownload(CSExternalInstanceId externalId, CSItemRecord record)
			throws EAIException {
		String imageId = externalId.getSourceSystemId() + "_" + externalId.getExternalId();
		try {

			LOGGER.trace("Scheduling download of image with id '{}'", imageId);
			// create the config
			SchedulerConfiguration configuration = schedulerService
					.buildEmptyConfiguration(SchedulerEntryType.TIMED)
						.setMaxRetryCount(10)
						.setRetryDelay(SCHEDULE_DELAY)
						.setScheduleTime(new Date(Calendar.getInstance().getTimeInMillis() + 6000))
						.setIncrementalDelay(true)
						.setPersistent(true)
						.setMaxActivePerGroup(EAIImageContentProcessorAction.NAME, 25)
						.setTransactionMode(TransactionMode.NOT_SUPPORTED)
						.setRemoveOnSuccess(true);
			configuration.setIdentifier(imageId);
			SchedulerContext context = new SchedulerContext();
			context.put(EAIImageContentProcessorAction.EAI_REQUEST_INFO,
					new RequestInfo(DAMIntegrationConfigurationProvider.SYSTEM_ID, DAMEAIServices.CONTENT,
							new ContentDownloadServiceRequest(externalId, record.getClassification())));
			SchedulerEntry scheduledDownload = schedulerService.schedule(EAIImageContentProcessorAction.NAME,
					configuration, context);
			LOGGER.trace("Scheduled download of image '{}' with entry {}", imageId, scheduledDownload.getId());
		} catch (Exception e) {
			throw new EAIException("Failed to schedule image download of " + imageId, e);
		}
	}

	@SuppressWarnings("unchecked")
	private <R extends ProcessedInstanceModel> R parseContentImport(ResponseInfo response) throws EAIException {
		ContentDownloadServiceRequest request = (ContentDownloadServiceRequest) response.getRequest();
		CSExternalInstanceId externalInstanceId = request.getInstanceId();
		RetrievedInstances<Instance> result = new RetrievedInstances<>();
		ErrorBuilderProvider errorProvider = new ErrorBuilderProvider();
		List<Instance> executeSearchByRecordInfo = executeSearchByRecordInfo(
				Collections.singletonMap(externalInstanceId, createDummyImageRecord(request)), errorProvider);
		if (errorProvider.hasErrors()) {
			throw new EAIReportableException(errorProvider.get().toString(), Objects.toString(externalInstanceId, ""));
		}
		if (executeSearchByRecordInfo.isEmpty()) {
			throw new EAIException(
					"Instance with id: " + externalInstanceId + " is not found! Could not attach image content to it."
							+ (errorProvider.hasErrors() ? errorProvider.get() : ""));
		}
		Instance image = executeSearchByRecordInfo.get(0);
		result.setInstances(Collections.singletonList(image));

		String imageFileName = null;
		File tempFile = null;
		try (StreamingResponse streamer = (StreamingResponse) response.getResponse()) {
			// mimetype and size should be always valid from streamer
			String mimetype = streamer.getContentType();
			// remove encoding
			if (mimetype.indexOf(';') > 1) {
				mimetype = mimetype.split(";")[0];
			}
			// extract filename from mimetype or if provided
			imageFileName = image.getString(DefaultProperties.NAME, buildImageFileName(externalInstanceId, mimetype));
			tempFile = downloadContent(streamer);
			Long fileSize = Long.valueOf(streamer.getContentLength());
			LOGGER.debug("Saving image with name {}, mimetype {} and size {} to {} ", imageFileName, mimetype, fileSize,
					image.getId());
			Content content = Content
					.createEmpty()
						.setContent(tempFile)
						.setPurpose(Content.PRIMARY_CONTENT)
						.setMimeType(mimetype)
						.setCharset(streamer.getContentEncoding())
						.setName(imageFileName);
			ContentInfo savedContent = transactionSupport.invokeInTx(() -> persistDownloadedContent(image, content));
			// refresh stale data object
			instanceService.refresh(image);
			// extract length and mimetype from the content itself
			fileSize = Long.valueOf(savedContent.getLength());
			mimetype = savedContent.getMimeType();
			// update both properties in the instance
			image.add(DefaultProperties.MIMETYPE, mimetype);
			image.add(DefaultProperties.CONTENT_LENGTH, fileSize);
			// set the image as uploaded
			image.add(DefaultProperties.PRIMARY_CONTENT_ID, savedContent.getContentId());
			LOGGER.debug("Saved image with name {}, mimetype {} and size {} to {} at {}", imageFileName, mimetype,
					fileSize, image.getId(), savedContent.getRemoteId());
		} catch (IOException e) {
			throw new EAIException("Failed to store image in server. Image file name: " + imageFileName, e);
		} finally {
			tempFileProvider.deleteFile(tempFile);
		}
		return (R) result;
	}

	private ContentInfo persistDownloadedContent(Instance image, Content content) {
		ContentInfo savedContent = instanceContentService.saveContent(image, content);
		// ---------following code is part of workaround
		ContentImport importedImage = ContentImport
				.copyFrom(savedContent)
					.setInstanceId(InstanceVersionService.buildVersionId(image));
		instanceContentService.importContent(importedImage);
		// ---------
		return savedContent;
	}

	private File downloadContent(StreamingResponse streamer) throws IOException {
		File tempFile = tempFileProvider.createTempFile("damTempContent", null);
		try (InputStream inputStream = streamer.getStream(); OutputStream output = new FileOutputStream(tempFile)) {
			long copied = IOUtils.copyLarge(inputStream, output);
			if (copied != streamer.getContentLength()) {
				LOGGER.warn("Downloaded content {} and the declared content {} size differ", copied,
						streamer.getContentLength());
			}
		}
		return tempFile;
	}

	private static String buildImageFileName(CSExternalInstanceId externalInstanceId, String mimetype) {
		String extension = "";
		try {
			TikaConfig config = TikaConfig.getDefaultConfig();
			MimeType mimeTypeValue = config.getMimeRepository().forName(mimetype);
			extension = mimeTypeValue.getExtension();
		} catch (Exception e) {// NOSONAR
			LOGGER.warn("Failed to extract extension for mimetype {} with error {}", mimetype, e.getMessage());
		}
		if (StringUtils.isBlank(extension)) {
			LOGGER.warn("Failed to extract extension for mimetype {}", mimetype);
		}
		StringBuilder fileNameBuilder = new StringBuilder();
		fileNameBuilder.append(externalInstanceId.getSourceSystemId()).append("_");
		fileNameBuilder.append(externalInstanceId.getExternalId()).append(extension);
		return fileNameBuilder.toString();
	}

	private CSItemRecord createDummyImageRecord(ContentDownloadServiceRequest request) {
		CSItemRecord record = new CSItemRecord();
		record.setNamespace(getDefaultNamespace());
		record.setClassification(request.getClassification());
		record.getProperties().put(buildNamespacePropertyId(getDefaultNamespace(), EXTERNAL_KEY_ID),
				request.getInstanceId().getExternalId());
		record.getProperties().put(buildNamespacePropertyId(getDefaultNamespace(), EXTERNAL_KEY_SOURCE),
				request.getInstanceId().getSourceSystemId());
		return record;
	}

	@Override
	protected void fillReceivedProperties(Instance createdInstance, CSItemRecord record) throws EAIException {
		EAIModelConverter modelConverter = modelService.provideModelConverterByNamespace(record.getNamespace());
		Map<String, Serializable> convertExternaltoSEIPProperties = modelConverter
				.convertExternaltoSEIPProperties(record.getProperties(), createdInstance);
		convertExternaltoSEIPProperties.remove(KEY_FINGERPRINT);
		createdInstance.getProperties().putAll(convertExternaltoSEIPProperties);
	}

	@Override
	protected void appendImportResult(RetrievedInstances<Instance> parsedInstances, Instance instance,
			boolean existingInstance, CSItemRecord record) throws EAIException {
		if (instance == null) {
			return;
		}
		// have same fingerptint
		Object fingerprint = record.get(buildNamespacePropertyId(getDefaultNamespace(), KEY_FINGERPRINT));
		if (fingerprint == null) {
			LOGGER.error("Missing fingeprint property for record {}", record);
		}
		if (existingInstance && instance.get(KEY_FINGERPRINT) != null
				&& EqualsHelper.nullSafeEquals(instance.get(KEY_FINGERPRINT), fingerprint)) {
			return;
		}
		// add the fingerprint again to be saved
		instance.add(KEY_FINGERPRINT, (Serializable) fingerprint);
		// needs to be updated, schedule image download
		parsedInstances.getInstances().add(instance);
		scheduleAsynchContentDownload(extractExternalId(record, null), record);
	}

	@Override
	protected Object loadRelationAsInformation(CSItemRecord sourceRecord, CSItemRelations nextRelation,
			Map<CSExternalInstanceId, Instance> existing, ErrorBuilderProvider errorBuilder) throws EAIException {
		return existing.get(extractExternalId(nextRelation.getRecord(), null));
	}

	@Override
	protected String getDefaultNamespace() {
		return DAMIntegrationConfigurationProvider.NAMESPACE;
	}

	@Override
	public String getName() {
		return DAMIntegrationConfigurationProvider.SYSTEM_ID;
	}

}
