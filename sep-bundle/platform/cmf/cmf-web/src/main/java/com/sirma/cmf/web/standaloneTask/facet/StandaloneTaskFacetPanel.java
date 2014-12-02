/*
 * 
 */
package com.sirma.cmf.web.standaloneTask.facet;

import javax.inject.Named;

import com.sirma.itt.emf.web.plugin.Plugable;

/**
 * The Class StandaloneTaskFacetPanel.
 * 
 * @author svelikov
 */
@Named
public class StandaloneTaskFacetPanel implements Plugable {

	/** The Constant EXTENSION_POINT. */
	public static final String EXTENSION_POINT = "standaloneTaskFacet";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getExtensionPoint() {
		return EXTENSION_POINT;
	}
}
