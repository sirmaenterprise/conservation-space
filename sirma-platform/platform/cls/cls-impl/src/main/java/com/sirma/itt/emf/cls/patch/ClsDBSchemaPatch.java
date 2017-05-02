package com.sirma.itt.emf.cls.patch;

import com.sirma.itt.seip.db.patch.DbSchemaPatch;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Class pointing to the CLS DB schema patch file.
 *
 * @author Mihail Radkov
 */
@Extension(target = DbSchemaPatch.TARGET_NAME, order = 66)
public class ClsDBSchemaPatch implements DbSchemaPatch {

	@Override
	public String getPath() {
		return buildPathFromCurrentPackage("cls-changelog.xml");
	}

}
