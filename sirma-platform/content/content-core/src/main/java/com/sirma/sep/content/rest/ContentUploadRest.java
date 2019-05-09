package com.sirma.sep.content.rest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.monitor.annotations.MetricDefinition;
import com.sirma.itt.seip.monitor.annotations.MetricDefinition.Type;
import com.sirma.itt.seip.monitor.annotations.Monitored;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.upload.ContentUploader;
import com.sirma.sep.content.upload.UploadRequest;

/**
 * Rest service that handle post requests for new content upload and upload new version.
 *
 * @author BBonev
 * @author Vilizar Tsonev
 */
@Path("/content")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class ContentUploadRest {

	private static final String METRIC_CONTENT_UPLOAD_SIZE_BYTES = "content_upload_size_bytes";

	@Inject
	private ContentUploader contentUploader;

	@Inject
	private Statistics stats;

	/**
	 * Upload single file from multi part form data
	 *
	 * @param uploadRequest
	 *            the upload request
	 * @param purpose
	 *            the purpose
	 * @return a content info containing information for the uploaded file
	 */
	@POST
	@Transactional
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Monitored({
		@MetricDefinition(name = "content_upload_duration_seconds", type = Type.TIMER, descr = "Content upload duration in seconds."),
		@MetricDefinition(name = METRIC_CONTENT_UPLOAD_SIZE_BYTES, type = Type.HISTOGRAM, descr = "Content upload size in bytes.")
	})
	public ContentInfo addContent(UploadRequest uploadRequest,
			@DefaultValue(Content.PRIMARY_CONTENT) @QueryParam("purpose") String purpose) {

		ContentInfo info = contentUploader.uploadWithoutInstance(uploadRequest, purpose);
		setLenghtStatValue(info.length());
		return info;
	}

	private void setLenghtStatValue(long len) {
		if (len > -1) {
			stats.value(METRIC_CONTENT_UPLOAD_SIZE_BYTES, len);
		}
	}
}
