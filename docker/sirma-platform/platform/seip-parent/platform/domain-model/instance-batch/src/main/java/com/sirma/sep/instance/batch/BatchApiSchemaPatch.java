package com.sirma.sep.instance.batch;

import com.sirma.itt.seip.db.patch.DbSchemaPatch;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Db schema patch plugin to include batch api schema changes
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 13/06/2017
 */
@Extension(target = DbSchemaPatch.TARGET_NAME, order = 146)
public class BatchApiSchemaPatch implements DbSchemaPatch {
	@Override
	public String getPath() {
		return buildPathFromCurrentPackage("batch-api-changelog.xml");
	}
}
