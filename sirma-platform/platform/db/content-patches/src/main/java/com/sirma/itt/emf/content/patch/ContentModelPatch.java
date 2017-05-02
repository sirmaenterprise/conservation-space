package com.sirma.itt.emf.content.patch;

import com.sirma.itt.seip.db.patch.DbDataPatch;
import com.sirma.itt.seip.db.patch.DbSchemaPatch;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Extension for providing the path to the content patches change log file.
 *
 * Those patches depends on the database to be fully initialized and should be executed on a later stage from all
 * patches implementing {@link DbSchemaPatch}.
 *
 * @author Mihail Radkov
 */
@Extension(target = DbDataPatch.TARGET_NAME, order = 1000)
public class ContentModelPatch implements DbDataPatch {

    @Override
    public String getPath() {
        return buildPathFromCurrentPackage("content-changelog.xml");
    }

}
