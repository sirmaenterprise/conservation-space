package com.sirma.itt.seip.domain.definition.label;

import java.util.function.Function;

import com.sirma.itt.seip.domain.definition.PropertyDefinition;

/**
 * Session scoped provider for fetching labels from database and from web configured bundles.
 * <p>
 * There is an extension point to add more bundles via {@link LabelResolverProvider} interface.
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
	 * @see LabelResolverProvider
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
	 * @see LabelResolverProvider
	 */
	String getBundleValue(String key);

	/**
	 * Resolve label for a property defined by the given property definition. The property label is resolved by the
	 * following algorithm: <ol>
	 * <li>Check if the given property label has value for the current language</li>
	 * <li>Check if the given property uri id has value for the current language</li>
	 * <li>Check if the given property label has value for the default language</li>
	 * <li>Check if the given property uri id has value for the default language</li>
	 * </ol>
	 *
	 * @param propertyDefinition the property definition to get it's label
	 * @return the label for the property defined by the given definition
	 */
	String getPropertyLabel(PropertyDefinition propertyDefinition);

	/**
	 * Resolve tooltip for a property defined by the given property definition. The property tooltip is resolved by the
	 * following algorithm: <ol>
	 * <li>Check if the given property label has value for the current language</li>
	 * <li>Check if the given property uri id has value for the current language</li>
	 * <li>Check if the given property label has value for the default language</li>
	 * <li>Check if the given property uri id has value for the default language</li>
	 * </ol>
	 *
	 * @param propertyDefinition the property definition to get it's tooltip
	 * @return the tooltip for the property defined by the given definition
	 */
	String getPropertyTooltip(PropertyDefinition propertyDefinition);

	/**
	 * Builds an identifier for the given URI that can be used for resolving a property label.
	 *
	 * @param uri the property uri
	 * @return the label identified based on the given uri
	 */
	static String buildUriLabelId(String uri) {
		return uri + ".label";
	}

	/**
	 * Builds an identifier for the given URI that can be used for resolving a property tooltip.
	 *
	 * @param uri the property uri
	 * @return the tooltip identified based on the given uri
	 */
	static String buildUriTooltipId(String uri) {
		return uri + ".tooltip";
	}
}
