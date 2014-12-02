package com.sirma.itt.cs.patch;

import com.sirma.itt.emf.patch.DBSchemaPatch;
import com.sirma.itt.emf.plugin.Extension;

/**
 *
 * @author kirq4e
 */
@Extension(target = DBSchemaPatch.TARGET_NAME, order = 3)
public class CSSemanticRepositoryUpdate implements DBSchemaPatch {


	@Override
	public String getPath() {
		return getClass().getPackage().getName().replace(".", "/") + "/cs-semantic-changelog.xml";
	}
}