package com.sirma.sep.email.patch;

import com.sirma.itt.emf.semantic.patch.SemanticSchemaPatches;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Patch semantic with changes needed for email integration
 *
 * @author S.Djulgerova
 */
@Extension(target = SemanticSchemaPatches.NAME, order = 70)
public class EmailIntegrationSemanticPatch implements SemanticSchemaPatches {

	@Override
	public String getPath() {
		return buildPathFromCurrentPackage("email-integration-semantic-changelog.xml");
	}

}