package com.sirma.itt.emf.util.sanitize;

/**
 * Sanitizes HTML content
 * 
 * @author BBonev
 */
public interface ContentSanitizer {

	/**
	 * Sanitizes document html. 
	 * 
	 * @param content
	 *            html content to sanitize.
	 * @return sanitized html
	 */
	String sanitize(String content);

	/**
	 * Sanitizes document html.
	 * 
	 * @param content
	 *            html content to sanitize.
	 * @param origin
	 *            the origin to use for link sanitizing
	 * @return sanitized html
	 */
	String sanitize(String content, String origin);

	/**
	 * Sanitizes template html. Just like document sanitize but also removes "id" attribute from
	 * elements and "value" attribute from all widgets.
	 * 
	 * @param content
	 *            html content to sanitize.
	 * @param origin
	 *            the origin to use for link sanitizing
	 * @return sanitized html
	 */
	String sanitizeTemplate(String content, String origin);

	/**
	 * Sanitizes the document html just before it is served as a result of a document/object clone
	 * operation. In addition to doing the standard document sanitation it also removes the id
	 * attributes from the elements. This is especially important for heading elements, it insures
	 * that comments in the original are not shown in the clone.
	 * 
	 * @param content
	 *            html content to sanitize.
	 * @param origin
	 *            the origin to use for link sanitizing
	 * @return sanitized html
	 */
	String sanitizeBeforeClone(String content, String origin);

}