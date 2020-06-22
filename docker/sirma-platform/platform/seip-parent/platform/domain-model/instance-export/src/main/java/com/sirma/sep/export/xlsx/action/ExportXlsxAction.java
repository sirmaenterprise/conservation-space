package com.sirma.sep.export.xlsx.action;

import java.io.File;

import javax.inject.Inject;

import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.export.ExportHelper;
import com.sirma.sep.export.ExportService;
import com.sirma.sep.export.SupportedExportFormats;

/**
 * Executes export of list data (e.g. data table widget) action. The {@link #perform(ExportXlsxRequest)} method builds
 * link, with which the exported file could be retrieved.
 *
 * @author gshefkedov
 * @author A. Kunchev
 */
@Extension(target = Action.TARGET_NAME, enabled = true, order = 140)
public class ExportXlsxAction implements Action<ExportXlsxRequest> {

	@Inject
	private ExportService exportService;

	@Inject
	private ExportHelper exportHelper;

	@Override
	public String getName() {
		return ExportXlsxRequest.EXPORT_XLSX;
	}

	@Override
	public Object perform(ExportXlsxRequest request) {
		File file = exportService.export(request.toXlsxExportRequest());
		String targetId = (String) request.getTargetId();
		return exportHelper.createDownloadableURL(file, request.getFileName(), targetId,
				SupportedExportFormats.XLS.getMimeType(), "-export-xlsx", request.getUserOperation());
	}

	@Override
	public boolean shouldLockInstanceBeforeAction(ExportXlsxRequest request) {
		// read only operation
		return false;
	}
}
