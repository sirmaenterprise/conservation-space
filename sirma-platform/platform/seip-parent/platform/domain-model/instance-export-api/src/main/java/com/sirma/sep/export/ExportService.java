package com.sirma.sep.export;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;

/**
 * Provides base functionality for exporting contents in different file formats. The type of the result file is
 * determined from the type of the input request.
 *
 * @author A. Kunchev
 * @see SupportedExportFormats
 */
@ApplicationScoped
public class ExportService {

	@Inject
	@ExtensionPoint(FileExporter.PLUGIN_NAME)
	private Plugins<FileExporter<ExportRequest>> exporters;

	/**
	 * Exports specific instance content to file. The type of the result file will be determined by the input request.
	 *
	 * @param request
	 *            should contain all of information, needed to complete the specific file export
	 * @return file in specific format, based on the executed export
	 * @see SupportedExportFormats
	 * @throws NullPointerException
	 *             when the request object is <code>null</code>
	 * @throws ExportFailedException
	 *             when cannot find suitable exporter for the requested format, when the exporter fails to export file
	 *             or when there is no result file from the export process
	 */
	public File export(ExportRequest request) {
		Objects.requireNonNull(request, "Request should not be null.");
		Optional<FileExporter<ExportRequest>> exporter = exporters.get(request.getName());
		if (!exporter.isPresent()) {
			throw new ExportFailedException("Failed to find suitable exporter for the requested format.");
		}

		Optional<File> exported;
		try {
			exported = exporter.get().export(request);
		} catch (ContentExportException e) {
			throw new ExportFailedException(e.getMessage(), e);
		}

		return exported.orElseThrow(() -> new ExportFailedException("There is no result file from the export."));
	}

}
