package com.sirma.cmf.web.caseinstance.facet;

import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.emf.web.plugin.Plugable;

/**
 * Extension point for facets panel in case landing page.
 * 
 * @author svelikov
 */
@Documentation("Extension point for facets panel in case landing page.")
public interface CaseFacetsExtensionPoint extends Plugable {

	/** The Constant EXTENSION_POINT. */
	String EXTENSION_POINT = "case.facet";
}
