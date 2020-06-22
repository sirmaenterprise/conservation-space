package com.sirma.itt.seip.instance.content.share;

import static com.sirma.sep.export.SupportedExportFormats.getSupportedFormat;

import java.io.File;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerRetryException;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.export.ExportFailedException;
import com.sirma.sep.export.ExportRequest;
import com.sirma.sep.export.ExportService;
import com.sirma.sep.export.ExportURIBuilder;
import com.sirma.sep.export.SupportedExportFormats;
import com.sirma.sep.export.pdf.PDFExportRequest.PDFExportRequestBuilder;
import com.sirma.sep.export.word.WordExportRequest.WordExportRequestBuilder;

/**
 * Schedules tasks for creating publicly sharable content for documents that are created. Created documents do not have
 * a primary content (binary content) which means that before sharing them we need to execute export.
 * <br />
 * We use our export functionality (PDF or word are supported so far) to create a new file from the content of the
 * instance. The new file is a content that has purpose specifically used for sharing. This way the content will become
 * unique per share URL. If for some reason export fails, we will retry it until it has reached the maximal number of
 * retries.
 *
 * @author A. Kunchev
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 */
@ApplicationScoped
@Named(ShareContentCreatedInstanceAction.ACTION_NAME)
public class ShareContentCreatedInstanceAction extends BaseShareInstanceContentAction {

	public static final String ACTION_NAME = "shareContentCreatedInstanceAction";
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String GENERATED_TITLE_PREFIX = "Generated-title-";

	@Inject
	private ExportURIBuilder uriBuilder;
	@Inject
	private ExportService exportService;
	@Inject
	private InstanceContentService instanceContentService;

	@Override
	public void execute(SchedulerContext context) throws Exception {
		ContentShareData data = context.getIfSameType(DATA, ContentShareData.class);

		SupportedExportFormats supportedFormat = getSupportedFormat(data.getFormat());

		ExportRequest request = getExportRequest(supportedFormat, data.getInstanceId(), data.getTitle(),
												 data.getToken());
		// if the request cannot be build for some reason then we skip it, because otherwise it will be scheduled 10
		// times before finally dropping it.
		if (request == null) {
			LOGGER.error("Could not construct request for instance {} with format {}. Skipping it.",
						 data.getInstanceId(), supportedFormat.getFormat());
			return;
		}
		export(request, supportedFormat, data.getContentId());
	}

	private ExportRequest getExportRequest(SupportedExportFormats format, Serializable instanceId, String name,
			String token) {
		switch (format) {
		case PDF:
			return preparePDFRequest(instanceId, name, token);
		case WORD:
			return prepareWordRequest(instanceId, name);
		default:
			return null;
		}
	}

	private ExportRequest preparePDFRequest(Serializable instanceId, String name, String token) {
		return new PDFExportRequestBuilder().setInstanceURI(uriBuilder.generateURI((String) instanceId, token))
				.setInstanceId(instanceId)
				.setFileName(getValidName(name, instanceId))
				.buildRequest();
	}

	private static ExportRequest prepareWordRequest(Serializable instanceId, String name) {
		return new WordExportRequestBuilder().setInstanceId(instanceId)
				.setFileName(getValidName(name, instanceId))
				.buildRequest();
	}

	private static String getValidName(String name, Serializable instanceId) {
		if (StringUtils.isBlank(name)) {
			return instanceId.toString().replaceAll("emf:", GENERATED_TITLE_PREFIX);
		}

		return name.replaceAll("[ //,.:]+", "-");
	}

	private void export(ExportRequest request, SupportedExportFormats format, String contentId) {
		Serializable id = request.getInstanceId();
		File result;
		try {
			result = exportService.export(request);
		} catch (ExportFailedException e) {
			LOGGER.trace(e.getMessage(), e);
			LOGGER.warn("Failed to export content for sharing for instance - {}", id);
			throw new SchedulerRetryException("Could not export content in order to share it, cause : ", e);
		}

		Content content = buildShareContent(result, format, contentId);
		ContentInfo savedContent = instanceContentService.saveContent(new EmfInstance(id), content);
		if (!savedContent.exists()) {
			LOGGER.warn("Failed to save content for sharing for instance - {}", id);
			result.delete();
			throw new SchedulerRetryException("Could not save exported content in order to share it.");
		}
	}

	private static Content buildShareContent(File content, SupportedExportFormats format, String contentId) {
		return Content.createEmpty()
				.setContent(content)
				.setContentId(contentId)
				.setCharset(StandardCharsets.UTF_8.name())
				.setMimeType(format.getMimeType())
				.setPurpose(SHARED_CONTENT_PURPOSE_PREFIX + RandomStringUtils.randomAlphanumeric(4))
				.setIndexable(false)
				.setView(false)
				.setName(content.getName())
				.setVersionable(false)
				.setDetectedMimeTypeFromContent(false);
	}
}