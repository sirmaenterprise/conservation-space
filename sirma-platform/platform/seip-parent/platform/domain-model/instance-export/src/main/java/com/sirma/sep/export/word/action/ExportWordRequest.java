package com.sirma.sep.export.word.action;

import com.sirma.itt.seip.instance.actions.ActionRequest;
import com.sirma.sep.export.word.WordExportRequest;
import com.sirma.sep.export.word.WordExportRequest.WordExportRequestBuilder;

/**
 * Used to store the information needed to execute correctly export to word action.
 *
 * @author Stella D
 */
public class ExportWordRequest extends ActionRequest {

	private static final long serialVersionUID = 2724834482811562890L;
	/** The name for operation. */
	public static final String EXPORT_WORD = "exportWord";

	private String url;
	private String fileName;
	private String tabId;

	@Override
	public String getOperation() {
		return EXPORT_WORD;
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
	 * Getter for the page URL.
	 *
	 * @return the url
	 */
	public String getUrl() {
		return url;
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
	 * Gets the tab id.
	 *
	 * @return the tab id
	 */
	public String getTabId() {
		return tabId;
	}

	/**
	 * Sets the tab id.
	 *
	 * @param tabId
	 *            the new tab id
	 */
	public void setTabId(String tabId) {
		this.tabId = tabId;
	}

	/**
	 * Converts jax-rs mapped request to service specific export request
	 *
	 * @return {@link WordExportRequest} to be used by the services for exporting
	 */
	public WordExportRequest toWordExporterRequest() {
		return new WordExportRequestBuilder()
				.setInstanceId(getTargetId())
					.setFileName(getFileName())
					.setTabId(getTabId())
					.buildRequest();
	}

}
