package com.sirma.itt.emf.definition.load;

import java.util.List;

import com.sirma.itt.emf.definition.model.DefinitionTemplateHolder;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;

/**
 * Callback interface needed when compiling concrete template definitions.
 *
 * @param <T>
 *            the type of the template
 * @param <V>
 *            the type of the template holder object
 * @author BBonev
 */
public interface TemplateDefinitionCompilerCallback<T extends TopLevelDefinition, V extends DefinitionTemplateHolder<T>>
		extends DefinitionCompilerCallback<T> {

	/**
	 * Gets the template holder class.
	 *
	 * @return the template class
	 */
	Class<V> getTemplateClass();

	/**
	 * Checks if is hybrid definitions supported by the given template definition callback. The
	 * method should return <code>true</code> if the current implementation for template definition
	 * defines a top level definitions and template definition at the same time.
	 * 
	 * @return true, if is hybrid definitions supported
	 */
	boolean isHybridDefinitionsSupported();

	/**
	 * Separates the template and standalone definitions. TIf the method
	 * {@link #isHybridDefinitionsSupported()} returns <code>true</code> then this method should
	 * filter out the template definitions from the given list of loaded definitions to the returned
	 * list should contain definitions that are top level definitions and need to be processed by
	 * the other compiler algorithm. The first argument of the pair are the template definitions and
	 * the second are standalone definitions.
	 * 
	 * @param loadedDefinitions
	 *            the loaded definitions
	 * @return the list of top level definitions
	 */
	Pair<List<T>, List<T>> filerStandaloneDefinitions(List<T> loadedDefinitions);

	/**
	 * If the method {@link #isHybridDefinitionsSupported()} returns <code>true</code> then this
	 * method should return a callback that to be used for processing the other definitions
	 * 
	 * @return the other callback
	 */
	DefinitionCompilerCallback<TopLevelDefinition> getOtherCallback();

}
