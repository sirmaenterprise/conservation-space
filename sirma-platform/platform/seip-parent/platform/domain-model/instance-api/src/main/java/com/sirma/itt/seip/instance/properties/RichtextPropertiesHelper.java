package com.sirma.itt.seip.instance.properties;

/**
 * Contains common methods used for richtext properties.
 *
 * @author S.Djulgerova
 */
public final class RichtextPropertiesHelper {

	private RichtextPropertiesHelper() {
		// utility class
	}

	/**
	 * Strips html tags from string
	 * 
	 * @param html
	 *            html string
	 * @return stripped string
	 */
	public static String stripHTML(String html) {
		return html.replaceAll("<[^>]*>", "").replaceAll("&nbsp;", " ");
	}
}
