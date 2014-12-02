package com.sirma.itt.emf.label;

import java.util.ResourceBundle;

/**
 * Session scoped provider for fetching labels from database and from web configured bundles.
 * <p>
 * There is an extension point to add more bundles via
 * {@link com.sirma.itt.emf.label.LabelBundleProvider} interface.
 * 
 * @author BBonev
 */
public interface LabelProvider {

	public static final String NO_LABEL = "(No label)";

	/**
	 * Gets a label for the language (locale) of the current user.
	 * 
	 * @param labelId
	 *            the label id
	 * @return the label
	 */
	String getLabel(String labelId);

	/**
	 * Gets a label for the given language.
	 * 
	 * @param labelId
	 *            the label id
	 * @param language
	 *            language of the current user.
	 * @return the label
	 */
	String getLabel(String labelId, String language);

	/**
	 * Gets a label defined in resource bundle defined in the faces-config.xml of the web fragment.
	 * The bundles are added via extension point
	 * 
	 * @param key
	 *            The property key to search for.
	 * @return Label value.
	 * @see com.sirma.itt.emf.label.LabelBundleProvider
	 */
	String getValue(String key);

	/**
	 * Getter for resource bundle.
	 * 
	 * @param language
	 *            language of the current user.
	 * @return A resource bundle.
	 */
	Iterable<ResourceBundle> getBundles(String language);

	/**
	 * Getter for resource bundle.
	 * 
	 * @return A resource bundle.
	 */
	Iterable<ResourceBundle> getBundles();

}
