package com.sirma.itt.seip.domain.definition;

import java.util.List;

/**
 * Common interface for loading definition templates. The class is used when loading a xml file with multiple
 * definitions in it. These definitions are considered as template definitions.
 *
 * @param <E>
 *            the template type
 * @author BBonev
 */
public interface DefinitionTemplateHolder<E extends TopLevelDefinition> extends TopLevelDefinition {

	/**
	 * Gets the templates.
	 *
	 * @return the templates
	 */
	List<E> getTemplates();

	/**
	 * Sets the templates.
	 *
	 * @param templates
	 *            the new templates
	 */
	void setTemplates(List<E> templates);

	@Override
	default String getType() {
		return null;
	}
}
