package com.sirma.sep.content.patch;

import com.sirma.itt.seip.db.patch.DbSchemaPatch;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Provides database schema patches for thumbnails module.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 09/11/2018
 */
@Extension(target = DbSchemaPatch.TARGET_NAME, order = 7)
public class ThumbnailSchemaPatch implements DbSchemaPatch {

	@Override
	public String getPath() {
		return buildPathFromCurrentPackage("thumbnails-changelog.xml");
	}
}
