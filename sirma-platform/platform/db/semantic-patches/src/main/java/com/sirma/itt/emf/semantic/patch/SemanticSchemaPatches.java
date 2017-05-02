/**
 *
 */
package com.sirma.itt.emf.semantic.patch;

import com.sirma.itt.seip.db.patch.DbPatch;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * Semantic schema patches, separate from relational db patches.
 *
 * @author BBonev
 */
public interface SemanticSchemaPatches extends DbPatch, Plugin {
	/** The extension ID. */
	String NAME = "SemanticSchemaPatches";
}
