package com.sirma.sep.model.management.persistence;

import com.sirma.itt.seip.db.patch.DbSchemaPatch;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Extension for models schema patches
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 09/08/2018
 */
@Extension(target = DbSchemaPatch.TARGET_NAME, order = 70)
public class ModelsSchemaPatch implements DbSchemaPatch {

	@Override
	public String getPath() {
		return buildPathFromCurrentPackage("models-changelog.xml");
	}
}
