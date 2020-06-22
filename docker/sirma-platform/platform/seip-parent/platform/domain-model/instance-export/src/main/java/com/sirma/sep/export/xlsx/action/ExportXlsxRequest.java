package com.sirma.sep.export.xlsx.action;

import javax.json.JsonObject;

import com.sirma.itt.seip.instance.actions.ActionRequest;
import com.sirma.sep.export.xlsx.XlsxExportRequest;
import com.sirma.sep.export.xlsx.XlsxExportRequest.JsonXlsxExportRequestBuilder;

/**
 * Request model for exporting data (e.g. data table widget) to excel file format.
 *
 * @author gshefkedov
 */
public class ExportXlsxRequest extends ActionRequest {

	public static final String EXPORT_XLSX = "exportXlsx";

	private static final long serialVersionUID = -7162319605784246665L;

	private String filename;

	private JsonObject requestJson;

	@Override
	public String getOperation() {
		return EXPORT_XLSX;
	}

	/**
	 * Getter method for filename.
	 *
	 * @return title
	 */
	public String getFileName() {
		return filename;
	}

	/**
	 * Setter method for filename.
	 *
	 * @param filename
	 *            the filename to set
	 */
	public void setFileName(String filename) {
		this.filename = filename;
	}

	public JsonObject getRequestJson() {
		return requestJson;
	}

	public void setRequestJson(JsonObject requestJson) {
		this.requestJson = requestJson;
	}

	/**
	 * Builds new {@link XlsxExportRequest} from the request json.
	 *
	 * @return request object that is used to export in excel
	 */
	public XlsxExportRequest toXlsxExportRequest() {
		return new JsonXlsxExportRequestBuilder(requestJson).buildRequest();
	}

}
