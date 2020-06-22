/**
 *
 */
package com.sirma.itt.emf.semantic.definitions;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.rdf4j.model.Model;

import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.semantic.definitions.SemanticDefinitionsModelProvider;

/**
 * Used to collect all SemanticDefinitionsModelProvider implementations registered as extensions and to call their
 * methods. This implementation is not included in the method calls, because it is not an extension.
 *
 * @author A. Kunchev
 */
@ApplicationScoped
public class ChainingSemanticDefinitionsModelProvider implements SemanticDefinitionsModelProvider {

	@Inject
	@ExtensionPoint(SemanticDefinitionsModelProvider.TARGET_NAME)
	private Iterable<SemanticDefinitionsModelProvider> providers;

	/**
	 * Executes this method, in the different implementations of the interface, which are registered as extensions. The
	 * extensions methods are called one by one.
	 */
	@Override
	public void provideModelStatements(DefinitionModel definition, Model model) {
		providers.forEach(provider -> provider.provideModelStatements(definition, model));
	}

}
