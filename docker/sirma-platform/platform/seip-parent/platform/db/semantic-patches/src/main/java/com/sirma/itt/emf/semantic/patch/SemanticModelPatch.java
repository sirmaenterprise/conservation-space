package com.sirma.itt.emf.semantic.patch;

import com.sirma.itt.seip.plugin.Extension;

/**
 * Patch semantic repository with changes in the ontologies
 *
 * @author kirq4e
 */
@Extension(target = SemanticSchemaPatches.NAME, order = 1)
public class SemanticModelPatch implements SemanticSchemaPatches {

	@Override
	public String getPath() {
		return buildPathFromCurrentPackage("emf-semantic-changelog.xml");
	}

}
