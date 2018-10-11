package com.sirma.sep.export.word;

import java.io.Serializable;
import java.util.Objects;

import com.sirma.sep.export.ExportRequest;
import com.sirma.sep.export.SupportedExportFormats;

/**
 * Request object used to execute export, which will generate file in MS Word format. It holds the required information
 * for successful execution of the export process.
 *
 * @author A. Kunchev
 */
public class WordExportRequest extends ExportRequest {

	/** Used when there should be exported specific tab. */
	private final String tabId;

	/**
	 * Instantiates new word export request.
	 *
	 * @param instanceId
	 *            the id of the instance that will be exported
	 * @param fileName
	 *            the name for the result file
	 * @param tabId
	 *            the id of the specific tab that should be exported
	 */
	protected WordExportRequest(final Serializable instanceId, final String fileName, final String tabId) {
		this.instanceId = instanceId;
		this.fileName = fileName;
		this.tabId = tabId;
	}

	@Override
	public String getName() {
		return SupportedExportFormats.WORD.getFormat();
	}

	public String getTabId() {
		return tabId;
	}

	/**
	 * Builder for the {@link WordExportRequest}. It provides validation of the required data before building and
	 * returning the actual request.
	 *
	 * @author A. Kunchev
	 */
	public static class WordExportRequestBuilder {

		private Serializable id;
		private String name;
		private String tab;

		/**
		 * Instantiates new word export request builder.
		 */
		public WordExportRequestBuilder() {
			// empty
		}

		/**
		 * Sets the id of the instance that should be exported.
		 *
		 * @param id
		 *            of the instance that should be exported
		 * @return current builder to allow methods chaining
		 */
		public WordExportRequestBuilder setInstanceId(Serializable id) {
			this.id = id;
			return this;
		}

		/**
		 * Sets the name that should be assigned to the result PDF file, when exporting the instance.
		 *
		 * @param name
		 *            of the result PDF file
		 * @return current builder to allow methods chaining
		 */
		public WordExportRequestBuilder setFileName(String name) {
			this.name = name;
			return this;
		}

		/**
		 * Sets the id of the specific tab that should be exported. If not set the whole instance will be exported.
		 *
		 * @param tab
		 *            the id of the tab that should be exported. Not required, if missing all of the tabs will be
		 *            exported
		 * @return current builder to allow methods chaining
		 */
		public WordExportRequestBuilder setTabId(String tab) {
			this.tab = tab;
			return this;
		}

		/**
		 * Performs validation for required data and builds new word export request.
		 *
		 * @return new {@link WordExportRequest}
		 */
		public WordExportRequest buildRequest() {
			Objects.requireNonNull(id, "Instance id is required argument for PDF export.");
			return new WordExportRequest(id, name, tab);
		}
	}

}
