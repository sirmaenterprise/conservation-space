package com.sirma.itt.emf.definition.model;

import java.util.List;

import com.sirma.itt.emf.domain.model.TopLevelDefinition;

/**
 * Common interface for loading definition templates. The class is used when loading a xml file with
 * multiple definitions in it. These definitions are considered as template definitions.
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
}
