package com.sirma.itt.pm.testutil;

import com.sirma.itt.cmf.testutil.ModulesInfo;
import com.sirma.itt.emf.domain.Pair;

/**
 * Extends {@link ModulesInfo} and provides information about all pm modules needed
 */
public interface PmModulesInfo extends ModulesInfo {

	/** The semantic impl. */
	Pair<String, String> SEMANTIC_IMPL = new Pair<String, String>("emf-semantic-impl", "");

	/** The semantic api. */
	Pair<String, String> SEMANTIC_API = new Pair<String, String>("emf-semantic-api", "");

	/** The semantic model. */
	Pair<String, String> SEMANTIC_MODEL = new Pair<String, String>("emf-semantic-model", "");

	/** The emf comments. */
	Pair<String, String> EMF_COMMENTS = new Pair<String, String>("emf-comments", "");

	/** The schedule core. */
	Pair<String, String> PM_SCHEDULE_IMPL = new Pair<String, String>("pm-schedule-core", "");
	/** The schedule core. */
	Pair<String, String> PM_SCHEDULE_IMPL_TEST = new Pair<String, String>("pm-schedule-core",
			"tests");
}
