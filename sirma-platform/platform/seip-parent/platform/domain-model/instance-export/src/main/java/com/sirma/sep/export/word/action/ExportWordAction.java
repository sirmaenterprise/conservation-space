package com.sirma.sep.export.word.action;

import java.io.File;

import javax.inject.Inject;

import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.export.ExportHelper;
import com.sirma.sep.export.ExportService;
import com.sirma.sep.export.SupportedExportFormats;

/**
 * Executes export to Word action. The {@link #perform(ExportWordRequest)} method builds link, with which the exported
 * Word could be received.
 *
 * @author Stella D
 */
@Extension(target = Action.TARGET_NAME, enabled = true, order = 130)
public class ExportWordAction implements Action<ExportWordRequest> {

	@Inject
	private ExportService exportService;

	@Inject
	private ExportHelper exportHelper;

	@Override
	public String getName() {
		return ExportWordRequest.EXPORT_WORD;
	}

	@Override
	public String perform(ExportWordRequest request) {
		File file = exportService.export(request.toWordExporterRequest());
		String targetId = (String) request.getTargetId();
		return exportHelper.createDownloadableURL(file, request.getFileName(), targetId,
				SupportedExportFormats.WORD.getMimeType(), "-export-word", request.getUserOperation());
	}

	@Override
	public boolean shouldLockInstanceBeforeAction(ExportWordRequest request) {
		// read only operation
		return false;
	}
}
