package com.sirma.itt.seip.definition;

import java.util.List;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * The AllowedAuthorityDefinitionsExtension is extension for definition filtering that depends on current user
 * permissions.
 *
 * @author bbanchev
 */
@Documentation("The Interface AllowedAuthorityDefinitionsExtension is extension for definition filtering that depends on current user permissions.")
public interface AllowedAuthorityDefinitionsExtension extends Plugin {

	/** The target name. */
	String TARGET_NAME = "allowedUserDefinitionsExtension";

	/**
	 * Checks if plugin supports this models - list of definition that later would be passed to
	 * {@link #getAllowedDefinitions(List)}.
	 *
	 * @param <D>
	 *            the generic type
	 * @param model
	 *            the model to be checked
	 * @return true, if is supported
	 */
	<D extends DefinitionModel> boolean isSupported(List<D> model);

	/**
	 * Gets the allowed definitions (new list of filtered out definitions). The result might be the same as model, if
	 * user has permissions to all instances or no filtering is applied
	 *
	 * @param <D>
	 *            the generic type
	 * @param model
	 *            the model to be definitions
	 * @return the allowed definitions for current user
	 */
	<D extends DefinitionModel> List<D> getAllowedDefinitions(List<D> model);

}
