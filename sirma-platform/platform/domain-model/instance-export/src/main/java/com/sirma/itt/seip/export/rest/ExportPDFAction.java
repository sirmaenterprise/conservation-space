package com.sirma.itt.seip.export.rest;

import java.io.File;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;

import com.sirma.itt.seip.export.ExportHelper;
import com.sirma.itt.seip.export.PDFExporter;
import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.exceptions.ResourceException;
import com.sirma.itt.seip.rest.models.ErrorData;
import com.sirma.itt.seip.rest.utils.ErrorCode;

/**
 * Executes export to PDF action. The {@link #perform(ExportPDFRequest)} method builds link, with which the exported PDF
 * could be received.
 *
 * @author A. Kunchev
 */
@Extension(target = Action.TARGET_NAME, enabled = true, order = 120)
public class ExportPDFAction implements Action<ExportPDFRequest> {

	private static final String PDF_MIME_TYPE = "application/pdf";

	@Inject
	private PDFExporter exporter;

	@Inject
	private ExportHelper exportHelper;

	@Override
	public String getName() {
		return ExportPDFRequest.EXPORT_PDF;
	}

	@Override
	public String perform(ExportPDFRequest request) {
		File file;
		try {
			file = exporter.export(request.getUrl(), request.getCookies());
		} catch (TimeoutException e) {
			throw new ResourceException(Status.INTERNAL_SERVER_ERROR, new ErrorData(ErrorCode.TIMEOUT, e.getMessage()), e);
		}
		String targetId = (String) request.getTargetId();
		return exportHelper.createDownloadableURL(file, request.getFileName(), targetId, PDF_MIME_TYPE, "-export-pdf",
				request.getUserOperation());
	}
}
