package com.sirma.itt.emf.definition.compile;

import java.util.List;

import com.sirma.itt.emf.definition.load.DefinitionCompilerCallback;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;

/**
 * Defines definition compilation algorithm.
 * 
 * @author BBonev
 */
public interface DefinitionCompilerAlgorithm {

	/**
	 * The method is called before any compilations. The implementation should reset any internal
	 * state and be prepared for next steps. If any pre-loading should be done it can be done here.
	 * 
	 * @param callback
	 *            the callback
	 */
	void prepare(DefinitionCompilerCallback<TopLevelDefinition> callback);

	/**
	 * Loads the files that are provided by the given callback
	 * 
	 * @param callback
	 *            the callback
	 * @return the list of loaded definitions.
	 */
	List<TopLevelDefinition> loadFiles(DefinitionCompilerCallback<TopLevelDefinition> callback);

	/**
	 * The actual compilation. The method should compile the given list of definitions. Note that
	 * the given list may be not the same returned from the method
	 * {@link #loadFiles(DefinitionCompilerCallback)}.
	 * 
	 * @param definitions
	 *            the definitions to compile
	 * @param callback
	 *            the callback
	 * @param persist
	 *            the persist
	 * @return the list
	 */
	List<TopLevelDefinition> compile(List<TopLevelDefinition> definitions,
			DefinitionCompilerCallback<TopLevelDefinition> callback, boolean persist);
}
