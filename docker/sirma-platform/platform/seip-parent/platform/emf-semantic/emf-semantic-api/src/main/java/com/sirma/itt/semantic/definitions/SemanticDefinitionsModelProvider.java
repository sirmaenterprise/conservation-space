/**
 *
 */
package com.sirma.itt.semantic.definitions;

import org.eclipse.rdf4j.model.Model;

import com.sirma.itt.seip.definition.event.DefinitionsChangedEvent;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * Plugin providing the way to extract information from definitions, prepare semantic statements and add them to the
 * model, which will be saved in the semantic. It is executed, when the definitions are loaded or reloaded (on
 * {@link DefinitionsChangedEvent} event). This guarantees that we work with latest definitions.
 *
 * @author A. Kunchev
 */
public interface SemanticDefinitionsModelProvider extends Plugin {

	String TARGET_NAME = "semanticDefinitionsModelProvider";

	/**
	 * Populates the passed model with the statements, created from the definitions.
	 *
	 * @param definition
	 *            the definition from which will be created statements and populated the passed model
	 * @param model
	 *            the model in which will be stored the created statements before their execution
	 */
	void provideModelStatements(DefinitionModel definition, Model model);

}
