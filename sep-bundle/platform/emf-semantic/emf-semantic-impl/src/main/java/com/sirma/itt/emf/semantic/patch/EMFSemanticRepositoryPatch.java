package com.sirma.itt.emf.semantic.patch;

import com.sirma.itt.emf.patch.DBSchemaPatch;
import com.sirma.itt.emf.plugin.Extension;

/**
 * Patch semantic repository with changes in the ontologies
 *
 * @author kirq4e
 */
@Extension(target = DBSchemaPatch.TARGET_NAME, order = 1)
public class EMFSemanticRepositoryPatch implements DBSchemaPatch {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPath() {
		return getClass().getPackage().getName().replace(".", "/") + "/emf-semantic-changelog.xml";
	}

}
