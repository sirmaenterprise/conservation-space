package com.sirma.sep.export.pdf;

import java.io.Serializable;
import java.net.URI;
import java.util.Objects;

import com.sirma.sep.export.ExportRequest;
import com.sirma.sep.export.SupportedExportFormats;

/**
 * Request object used to execute export, which will generate file in PDF format. It holds the required information for
 * successful execution of the export process.
 *
 * @author A. Kunchev
 */
public class PDFExportRequest extends ExportRequest {

	/**
	 * Full URI to the instance page, used to view the data and the content for that instance. It should include
	 * authorization token.
	 */
	private final URI instanceURI;

	/**
	 * Instantiates new PDF export request.
	 *
	 * @param instanceId
	 *            the id of the instance that will be exported
	 * @param fileName
	 *            the name for the result file
	 * @param instanceURI
	 *            full url to the instance, which should be exported. It is used to load the page that should be
	 *            exported as PDF
	 */
	protected PDFExportRequest(final Serializable instanceId, final String fileName, final URI instanceURI) {
		this.instanceId = instanceId;
		this.fileName = fileName;
		this.instanceURI = instanceURI;
	}

	@Override
	public String getName() {
		return SupportedExportFormats.PDF.getFormat();
	}

	public URI getInstanceURI() {
		return instanceURI;
	}

	/**
	 * Builder for the {@link PDFExportRequest}. It provides validation of the required data before building and
	 * returning the actual request.
	 *
	 * @author A. Kunchev
	 */
	public static class PDFExportRequestBuilder {

		private Serializable id;
		private String name;
		private URI uri;

		/**
		 * Instantiates new PDF export request builder.
		 */
		public PDFExportRequestBuilder() {
			// empty
		}

		/**
		 * Sets the id of the instance that should be exported.
		 *
		 * @param id
		 *            of the instance that should be exported
		 * @return current builder to allow methods chaining
		 */
		public PDFExportRequestBuilder setInstanceId(Serializable id) {
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
		public PDFExportRequestBuilder setFileName(String name) {
			this.name = name;
			return this;
		}

		/**
		 * Sets the {@link URI} to the instance that should be exported. It is used to load the page to the instance.
		 *
		 * @param uri
		 *            full url to the instance that should be exported. It is used to retrieved the html for the page so
		 *            that it could be used in the conversion to PDF
		 * @return current builder to allow methods chaining
		 */
		public PDFExportRequestBuilder setInstanceURI(URI uri) {
			this.uri = uri;
			return this;
		}

		/**
		 * Performs validation for required data and builds new PDF export request.
		 *
		 * @return new {@link PDFExportRequest}
		 */
		public PDFExportRequest buildRequest() {
			Objects.requireNonNull(uri, "InstanceURI is required argument for PDF export.");
			return new PDFExportRequest(id, name, uri);
		}
	}

}
