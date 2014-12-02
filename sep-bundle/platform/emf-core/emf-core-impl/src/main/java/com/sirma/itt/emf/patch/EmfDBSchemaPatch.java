package com.sirma.itt.emf.patch;

import com.sirma.itt.emf.patch.DBSchemaPatch;
import com.sirma.itt.emf.plugin.Extension;

/**
 * DB patch for the EMF database.
 * 
 * @author BBonev
 */
@Extension(target = DBSchemaPatch.TARGET_NAME, order = 0)
public class EmfDBSchemaPatch implements DBSchemaPatch {
	@Override
	public String getPath() {
		return getClass().getPackage().getName().replace(".", "/") + "/emf-changelog.xml";
	}
}
