package com.sirma.itt.seip.domain.definition.label;

import java.util.ResourceBundle;
import java.util.function.Function;

/**
 * Session scoped provider for fetching labels from database and from web configured bundles.
 * <p>
 * There is an extension point to add more bundles via {@link LabelBundleProvider} interface.
 *
 * @author BBonev
 */
public interface LabelProvider {

	String NO_LABEL = "(No label)";

	/**
	 * Gets a label for the language (locale) of the current user.
	 *
	 * @param labelId
	 *            the label id
	 * @return the label
	 */
	String getLabel(String labelId);

	/**
	 * Gets a function that gets the label for the language of the current user.
	 *
	 * @return the function
	 */
	default Function<String, String> getLabelProvider() {
		return this::getLabel;
	}

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
	 * Gets a label defined in resource bundle defined in the faces-config.xml of the web fragment. The bundles are
	 * added via extension point. If the label is not found in the bundles, it will be retrieved from the cache.
	 * <b>Cannot be called from the system tenant.</b>
	 *
	 * @param key
	 *            The property key to search for.
	 * @return Label value.
	 * @see LabelBundleProvider
	 */
	String getValue(String key);

	/**
	 * Gets a label defined in resource bundle defined in the faces-config.xml of the web fragment. The bundles are
	 * added via extension point. If the label is not found in the bundles, it will be retrieved from the cache.
	 * <b>Shouldn't be called from the system tenant.</b>
	 *
	 * @param key
	 *            The property key to search for.
	 * @return Label value.
	 * @see LabelBundleProvider
	 */
	String getBundleValue(String key);

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
