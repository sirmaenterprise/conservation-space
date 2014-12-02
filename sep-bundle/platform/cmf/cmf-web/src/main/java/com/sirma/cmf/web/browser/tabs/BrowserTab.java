package com.sirma.cmf.web.browser.tabs;

/**
 * This class will holds browser tab information, like icon and title. The data will be filled based
 * on <b>URL path</b> or <b>context instance</b>.
 * 
 * @see ApplicationTabHeaderProvider
 * @author cdimitrov
 */
public class BrowserTab {

	/** The browser tab title. */
	private String browserTabTitle;

	/** The browser tab icon. */
	private String browserTabIcon;

	/**
	 * Getter for browser tab title.
	 * 
	 * @return browser tab title
	 */
	public String getBrowserTabTitle() {
		return browserTabTitle;
	}

	/**
	 * Setter for browser tab title.
	 * 
	 * @param browserTabTitle
	 *            current browser tab title
	 */
	public void setBrowserTabTitle(String browserTabTitle) {
		this.browserTabTitle = browserTabTitle;
	}

	/**
	 * Getter for browser tab icon.
	 * 
	 * @return browser tab icon
	 */
	public String getBrowserTabIcon() {
		return browserTabIcon;
	}

	/**
	 * Setter for browser tab icon.
	 * 
	 * @param browserTabIcon
	 *            current browser tab icon.
	 */
	public void setBrowserTabIcon(String browserTabIcon) {
		this.browserTabIcon = browserTabIcon;
	}

}
