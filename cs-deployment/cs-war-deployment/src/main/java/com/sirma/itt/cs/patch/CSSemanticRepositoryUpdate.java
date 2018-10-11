package com.sirma.itt.cs.patch;

import com.sirma.itt.emf.semantic.patch.SemanticSchemaPatches;
import com.sirma.itt.seip.plugin.Extension;

/**
 * @author kirq4e
 */
@Extension(target = SemanticSchemaPatches.NAME, order = 3)
public class CSSemanticRepositoryUpdate implements SemanticSchemaPatches {

	@Override
	public String getPath() {
		return getClass().getPackage().getName().replace(".", "/") + "/cs-semantic-changelog.xml";
	}
}