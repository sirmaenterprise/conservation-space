package com.sirma.sep.export.pdf.action;

import java.io.File;

import javax.inject.Inject;

import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.export.ExportHelper;
import com.sirma.sep.export.ExportService;
import com.sirma.sep.export.SupportedExportFormats;

/**
 * Executes export to PDF action. The {@link #perform(ExportPDFRequest)} method builds link, with which the exported PDF
 * could be received.
 *
 * @author A. Kunchev
 */
@Extension(target = Action.TARGET_NAME, enabled = true, order = 120)
public class ExportPDFAction implements Action<ExportPDFRequest> {

	@Inject
	private ExportService exportService;

	@Inject
	private ExportHelper exportHelper;

	@Override
	public String getName() {
		return ExportPDFRequest.EXPORT_PDF;
	}

	@Override
	public String perform(ExportPDFRequest request) {
		try {
			File exported = exportService.export(request.toPDFExportRequest());
			return exportHelper.createDownloadableURL(exported, request.getFileName(), (String) request.getTargetId(),
					SupportedExportFormats.PDF.getMimeType(), "-export-pdf", request.getUserOperation());
		} catch (Exception e) {
			throw new RollbackedRuntimeException(e);
		}
	}

	@Override
	public boolean shouldLockInstanceBeforeAction(ExportPDFRequest request) {
		// read only operation
		return false;
	}
}
