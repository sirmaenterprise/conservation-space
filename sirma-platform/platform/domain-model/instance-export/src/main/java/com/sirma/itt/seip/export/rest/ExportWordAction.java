package com.sirma.itt.seip.export.rest;

import java.io.File;
import java.util.Objects;

import javax.inject.Inject;

import com.sirma.itt.seip.export.ExportHelper;
import com.sirma.itt.seip.export.ExportWord;
import com.sirma.itt.seip.export.WordExporter;
import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Executes export to Word action. The {@link #perform(ExportWordRequest)} method builds link, with which the exported
 * Word could be received.
 *
 * @author Stella D
 */
@Extension(target = Action.TARGET_NAME, enabled = true, order = 130)
public class ExportWordAction implements Action<ExportWordRequest> {

	private static final String DOCX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

	@Inject
	private ExportHelper exportHelper;

	@Inject
	private WordExporter exporter;

	@Override
	public String getName() {
		return ExportWordRequest.EXPORT_WORD;
	}

	@Override
	public String perform(ExportWordRequest request) {
		ExportWord exportWord = new ExportWord();
		exportWord.setCookies(request.getCookies());
		exportWord.setFileName(request.getFileName());
		exportWord.setTabId(request.getTabId());
		exportWord.setUrl(request.getUrl());
		exportWord.setTargetId(Objects.toString(request.getTargetId(), null));

		File file = exporter.export(exportWord);
		String targetId = (String) request.getTargetId();
		return exportHelper.createDownloadableURL(file, request.getFileName(), targetId, DOCX_MIME_TYPE, "-export-word",
				request.getUserOperation());
	}

}