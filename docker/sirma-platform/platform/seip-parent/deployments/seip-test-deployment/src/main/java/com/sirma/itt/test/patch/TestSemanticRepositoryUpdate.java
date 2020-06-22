package com.sirma.itt.test.patch;

import com.sirma.itt.emf.semantic.patch.SemanticSchemaPatches;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Updates all ontologies in the semantic repository
 *
 * @author kirq4e
 */
@Extension(target = SemanticSchemaPatches.NAME, order = 5)
public class TestSemanticRepositoryUpdate implements SemanticSchemaPatches {

	@Override
	public String getPath() {
		return buildPathFromCurrentPackage("test-semantic-changelog.xml");
	}

}
