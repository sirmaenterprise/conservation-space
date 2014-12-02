package com.sirma.itt.emf.cls.patch;

import com.sirma.itt.emf.patch.DBSchemaPatch;
import com.sirma.itt.emf.plugin.Extension;

/**
 * Class pointing to the CLS DB schema patch file.
 * 
 * @author Mihail Radkov
 */
@Extension(target = DBSchemaPatch.TARGET_NAME, order = 66)
public class ClsDBSchemaPatch implements DBSchemaPatch {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPath() {
		return getClass().getPackage().getName().replace(".", "/") + "/cls-changelog.xml";
	}

}
