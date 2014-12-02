package com.sirma.itt.objects.patch;

import com.sirma.itt.emf.patch.DBSchemaPatch;
import com.sirma.itt.emf.plugin.Extension;

/**
 * Base database patch for Objects module
 * 
 * @author BBonev
 */
@Extension(target = DBSchemaPatch.TARGET_NAME, order = 40)
public class ObjectsDbPatch implements DBSchemaPatch {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPath() {
		return getClass().getPackage().getName().replace(".", "/") + "/objects-changelog.xml";
	}

}
