package com.sirma.itt.seip.export.rest;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.instance.actions.ActionRequest;

/**
 * Used to store the information needed to execute correctly export to word action.
 *
 * @author Stella D
 */
public class ExportWordRequest extends ActionRequest {

	private static final long serialVersionUID = 2724834482811562890L;

	public static final String EXPORT_WORD = "exportWord";

	private StringPair[] cookies;

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
	 * Getter for the page URL.
	 *
	 * @return the url
	 */
	public String getUrl() {
		return url;
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

	/**
	 * Getter for the request cookies.
	 *
	 * @return the cookies
	 */
	public StringPair[] getCookies() {
		return cookies;
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

}
