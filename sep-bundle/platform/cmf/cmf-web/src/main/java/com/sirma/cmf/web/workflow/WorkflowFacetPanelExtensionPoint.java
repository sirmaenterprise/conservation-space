package com.sirma.cmf.web.workflow;

import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.emf.web.plugin.Plugable;

/**
 * Extension point for facets panel in workflow landing page.
 * 
 * @author svelikov
 */
@Documentation("Extension point for facets panel in workflow landing page.")
public interface WorkflowFacetPanelExtensionPoint extends Plugable {

	/** The Constant EXTENSION_POINT. */
	String EXTENSION_POINT = "workflow.facet";
}
