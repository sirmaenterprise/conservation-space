package com.sirma.cmf.web.caseinstance.facet;

import javax.inject.Named;

/**
 * Registered case facet panels for CaseFacetsExtensionPoint.
 * 
 * @author svelikov
 */
@Named
public class CaseFacetPanels implements CaseFacetsExtensionPoint {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getExtensionPoint() {
		return EXTENSION_POINT;
	}

}
