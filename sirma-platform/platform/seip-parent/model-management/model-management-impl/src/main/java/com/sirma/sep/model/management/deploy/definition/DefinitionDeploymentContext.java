package com.sirma.sep.model.management.deploy.definition;

/**
 * Context that should be used when performing {@link com.sirma.sep.model.management.ModelDefinition} deployment.
 * <p>
 * Contains meta information about some specific events that may have occurred during deployment.
 *
 * @author Mihail Radkov
 */
public class DefinitionDeploymentContext {

	private boolean hasDeployedCodelists;

	public void codelistsUpdated() {
		hasDeployedCodelists = true;
	}

	public boolean hasUpdatedCodelists() {
		return hasDeployedCodelists;
	}
}
