package com.sirma.itt.pm.patch;

import com.sirma.itt.emf.patch.DBSchemaPatch;
import com.sirma.itt.emf.plugin.Extension;

/**
 * Base database patch for PM module
 * 
 * @author BBonev
 */
@Extension(target = DBSchemaPatch.TARGET_NAME, order = 20)
public class PmDbPatch implements DBSchemaPatch {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPath() {
		return getClass().getPackage().getName().replace(".", "/") + "/pm-changelog.xml";
	}

}
