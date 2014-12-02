package com.sirma.itt.emf.web.resources;


/**
 * Utility methods for web resources manipulation.
 * 
 * @author svelikov
 */
public class WebResourceUtil {

	/**
	 * Gets the icon url.
	 * 
	 * @param host
	 *            the host
	 * @param icon
	 *            the icon
	 * @return the icon url
	 */
	public static String getIconUrl(String host, String icon) {
		String imgSuffix = ".jsf?ln=images";
		String imgPreffix = "/javax.faces.resource/";
		StringBuilder stringURI = new StringBuilder(60);
		stringURI.append(host).append(imgPreffix).append(icon).append(imgSuffix);
		return stringURI.toString();
	}
}
