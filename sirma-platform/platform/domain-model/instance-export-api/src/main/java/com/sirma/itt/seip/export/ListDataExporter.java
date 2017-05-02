package com.sirma.itt.seip.export;

import java.io.File;


/**
 * Defines method for exporting list data (e.g. datatable widget) to file.
 *
 * @author gshefkedov
 */
public interface ListDataExporter {

	/**
	 * Exports list data (e.g. datatable widget) to a file.
	 *
	 * @param request
	 *            list data request for export
	 * @return generated file
	 */
	File export(ExportListDataXlsx request);
}
