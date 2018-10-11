package com.sirma.sep.export;

import java.io.File;
import java.util.Optional;

import com.sirma.itt.seip.Named;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * Defines means for executing export in different formats in unified way. The export format is determined by the
 * request type.
 *
 * @param <E>
 *            the type of the request
 * @author A. Kunchev
 */
public interface FileExporter<E extends ExportRequest> extends Named, Plugin {

	String PLUGIN_NAME = "fileExporter";

	/**
	 * Performs export process with the provided request. The process will produce file in specific format as a result.
	 *
	 * @param request
	 *            for execution. It type defines the format of the result file. Should contain all of the required data
	 *            for successful export
	 * @return file in specific file format
	 * @throws ContentExportException
	 *             when there is a problem with the export process
	 */
	Optional<File> export(E request) throws ContentExportException;

}
