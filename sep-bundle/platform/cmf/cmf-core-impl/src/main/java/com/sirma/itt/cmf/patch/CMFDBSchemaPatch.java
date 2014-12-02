package com.sirma.itt.cmf.patch;

import com.sirma.itt.emf.patch.DBSchemaPatch;
import com.sirma.itt.emf.plugin.Extension;

/**
 * DB patch for the CMF database.
 * 
 * @author Adrian Mitev
 */
@Extension(target = DBSchemaPatch.TARGET_NAME, order = 10)
public class CMFDBSchemaPatch implements DBSchemaPatch {

	@Override
	public String getPath() {
		return getClass().getPackage().getName().replace(".", "/") + "/cmf-changelog.xml";
	}

}
