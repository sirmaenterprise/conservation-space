package com.sirma.itt.seip.definition.label;

import java.util.List;

/**
 * Label service that provides a way for persisting label definition to DB
 *
 * @author BBonev
 */
public interface LabelService {

	/**
	 * Gets a label definition by name.
	 *
	 * @param name
	 *            the name
	 * @return the label definition
	 */
	LabelDefinition getLabel(String name);

	/**
	 * Save label definition.
	 *
	 * @param labelDefinition
	 *            the label definition
	 * @return true, if successful
	 */
	boolean saveLabel(LabelDefinition labelDefinition);

	/**
	 * Saves all labels.
	 *
	 * @param definitions
	 *            the label definitions to save
	 * @return true, if successful
	 */
	boolean saveLabels(List<LabelDefinition> definitions);

	/**
	 * Clear internal cache.
	 */
	void clearCache();
}
