package com.sirma.itt.cmf.testutil;

import com.sirma.itt.emf.domain.Pair;

/**
 * The Interface ModulesInfo holds constants for modules related to tests
 */
public interface ModulesInfo {

	/** The emf web. */
	Pair<String, String> EMF_WEB = new Pair<String, String>("emf-core-web", "");

	/** The cmf web. */
	Pair<String, String> CMF_WEB = new Pair<String, String>("cmf-web", "");

	/** The cmf core impl. */
	Pair<String, String> CMF_CORE_IMPL = new Pair<String, String>("cmf-core-impl", "");

	/** The cmf adapters alfresco. */
	Pair<String, String> CMF_ADAPTERS_ALFRESCO = new Pair<String, String>(
			"cmf-adapters-alfresco-4.0", "");
}
