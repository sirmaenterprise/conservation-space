package com.sirma.sep.export.pdf.action;

import java.net.URI;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.instance.actions.ActionRequest;
import com.sirma.sep.export.pdf.PDFExportRequest;
import com.sirma.sep.export.pdf.PDFExportRequest.PDFExportRequestBuilder;

/**
 * Used to store the information needed to execute correctly export to PDF action.
 *
 * @author A. Kunchev
 */
public class ExportPDFRequest extends ActionRequest {

	/** The name for operation. */
	public static final String EXPORT_PDF = "exportPDF";

	private static final long serialVersionUID = 8432930153536410371L;

	private String url;
	private String fileName;

	@Override
	public String getOperation() {
		return EXPORT_PDF;
	}

	/**
	 * Getter for the page URL.
	 *
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Setter for the page URL.
	 *
	 * @param url
	 *            the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Gets the file name.
	 *
	 * @return the file name
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Sets the file name.
	 *
	 * @param fileName
	 *            the new file name
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Converts jax-rs mapped request to service specific export request
	 *
	 * @return {@link PDFExportRequest} to be used by the services for exporting
	 * @throws IllegalArgumentException
	 *             when the url or filename arguments are blank
	 */
	public PDFExportRequest toPDFExportRequest() {
		if (StringUtils.isBlank(getUrl())) {
			throw new IllegalArgumentException("URL parameter shouldn't be blank.");
		}

		if (StringUtils.isBlank(getFileName())) {
			throw new IllegalArgumentException("Filename parameter shouldn't be blank.");
		}

		return new PDFExportRequestBuilder()
				.setInstanceId(getTargetId())
					.setInstanceURI(URI.create(getUrl()))
					.setFileName(getFileName())
					.buildRequest();
	}

}
