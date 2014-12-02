/*
 * 
 */
package com.sirma.cmf.web.workflow.task.facet;

import javax.inject.Named;

import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.web.plugin.PageFragment;
import com.sirma.itt.emf.web.plugin.Plugable;

/**
 * The Class WorkflowTaskFacetPanel.
 * 
 * @author svelikov
 */
@Named
public class WorkflowTaskFacetPanel implements Plugable {

	/** The Constant EXTENSION_POINT. */
	public static final String EXTENSION_POINT = "workflowTaskFacet";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getExtensionPoint() {
		return EXTENSION_POINT;
	}

	/**
	 * The Class WorkflowTaskPropertiesFacet.
	 */
	@Extension(target = EXTENSION_POINT, enabled = true, order = 20, priority = 1)
	public static class WorkflowTaskPropertiesFacet implements PageFragment {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getPath() {
			return "/task/workflow/includes/facet-task-properties.xhtml";
		}
	}

}
