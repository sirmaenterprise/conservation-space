package com.sirma.itt.seip.export.rest;

import java.io.File;

import javax.inject.Inject;

import com.sirma.itt.seip.export.ExportHelper;
import com.sirma.itt.seip.export.ExportListDataXlsx;
import com.sirma.itt.seip.export.ListDataXlsxExporter;
import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Executes export of list data (e.g. widget) action. The {@link #perform(ExportListDataXlsxRequest)} method builds
 * link, with which the exported file could be retrieved.
 *
 * @author gshefkedov
 */
@Extension(target = Action.TARGET_NAME, enabled = true, order = 140)
public class ExportListDataXlsxAction implements Action<ExportListDataXlsxRequest> {

	private static final String XLSX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	@Inject
	private ListDataXlsxExporter entityList;

	@Inject
	private ExportHelper exportHelper;

	@Override
	public String getName() {
		return ExportListDataXlsxRequest.EXPORT_XLSX;
	}

	@Override
	public Object perform(ExportListDataXlsxRequest request) {
		ExportListDataXlsx dataXlsxRequest = new ExportListDataXlsx();
		dataXlsxRequest.setFileName(request.getFileName());
		dataXlsxRequest.setHeaderType(request.getHeaderType());
		dataXlsxRequest.setSelectedInstances(request.getSelectedInstances());
		dataXlsxRequest.setSelectedProperties(request.getSelectedProperties());

		File file = entityList.export(dataXlsxRequest);
		String targetId = (String) request.getTargetId();
		return exportHelper.createDownloadableURL(file, null, targetId, XLSX_MIME_TYPE, "-export-xlsx", null);
	}

}
