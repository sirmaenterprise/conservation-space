package com.sirma.itt.seip.export.rest;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.instance.actions.ActionRequest;

/**
 * Used to store the information needed to execute correctly export to PDF action.
 *
 * @author A. Kunchev
 */
public class ExportPDFRequest extends ActionRequest {

	public static final String EXPORT_PDF = "exportPDF";

	private static final long serialVersionUID = 8432930153536410371L;

	private StringPair[] cookies;

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
	 * @throws IllegalArgumentException
	 *             when the input argument is blank
	 */
	public void setUrl(String url) {
		if (StringUtils.isBlank(url)) {
			throw new IllegalArgumentException("URL parameter shouldn't be blank.");
		}
		this.url = url;
	}

	/**
	 * Getter for the request cookies.
	 *
	 * @return the cookies
	 */
	public StringPair[] getCookies() {
		return cookies;
	}

	/**
	 * Setter for the request cookies.
	 *
	 * @param cookies
	 *            the cookies to set
	 */
	public void setCookies(StringPair[] cookies) {
		this.cookies = cookies;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
