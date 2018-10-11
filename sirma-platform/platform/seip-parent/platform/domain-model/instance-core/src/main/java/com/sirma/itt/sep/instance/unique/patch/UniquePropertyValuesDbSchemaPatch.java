package com.sirma.itt.sep.instance.unique.patch;

import com.sirma.itt.seip.db.patch.DbSchemaPatch;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Extension to add the schema patch file for the unique property value management persistence model.
 *
 * @author Boyan Tonchev.
 */
@Extension(target = DbSchemaPatch.TARGET_NAME, order = 334)
public class UniquePropertyValuesDbSchemaPatch implements DbSchemaPatch {

    @Override
    public String getPath() {
        return buildPathFromCurrentPackage("unique-fields-changelog.xml");
    }
}
