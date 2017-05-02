package com.sirma.itt.seip.definition.compile;

import java.util.List;

import com.sirma.itt.seip.domain.definition.TopLevelDefinition;

/**
 * Defines definition compiler that executes a compilation algorithms based on the provided callback implementation.
 *
 * @author BBonev
 */
public interface DefinitionCompiler {

	/**
	 * Compile definitions provided by the given callback.
	 *
	 * @param <T>
	 *            the top level definition type
	 * @param callback
	 *            the callback to use for definition compilation.
	 * @param persist
	 *            if the compiler should persist the compiled definitions or only to return them.
	 * @return the list of the successfully compiled definitions
	 */
	<T extends TopLevelDefinition> List<T> compileDefinitions(DefinitionCompilerCallback<T> callback, boolean persist);
}
