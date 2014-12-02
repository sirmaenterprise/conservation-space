package com.sirma.cmf.web.workflow;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

/**
 * Registered facets in workflow facet panel.
 * 
 * @author svelikov
 */
@Named
@ApplicationScoped
public class WorkflowFacetPanel implements WorkflowFacetPanelExtensionPoint {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getExtensionPoint() {
		return EXTENSION_POINT;
	}

}
