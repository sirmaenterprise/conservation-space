package com.sirma.cmf.web.search.facet.event;

import javax.enterprise.util.AnnotationLiteral;

/**
 * The Class FacetEventTypeBinding.
 * 
 * @author svelikov
 */
public class FacetEventTypeBinding extends AnnotationLiteral<FacetEventType> implements
		FacetEventType {

	private static final long serialVersionUID = 3604340714090781954L;

	private final String instanceName;

	/**
	 * Instantiates a new facet event type binding.
	 * 
	 * @param instanceName
	 *            the instance name
	 */
	public FacetEventTypeBinding(String instanceName) {
		this.instanceName = instanceName;
	}

	@Override
	public String value() {
		return instanceName;
	}

}
