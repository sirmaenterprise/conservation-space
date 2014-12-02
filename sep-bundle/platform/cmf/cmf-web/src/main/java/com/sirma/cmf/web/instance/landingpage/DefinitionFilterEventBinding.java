package com.sirma.cmf.web.instance.landingpage;

import javax.enterprise.util.AnnotationLiteral;

import com.sirma.itt.emf.instance.dao.InstanceType;

/**
 * The Class DefinitionFilterEventBinding.
 * 
 * @author svelikov
 */
public class DefinitionFilterEventBinding extends AnnotationLiteral<InstanceType> implements
		InstanceType {

	/** The type. */
	private final String type;

	/**
	 * Instantiates a new definition filter event binding.
	 * 
	 * @param type
	 *            the type
	 */
	public DefinitionFilterEventBinding(String type) {
		this.type = type;
	}

	@Override
	public String type() {
		return type;
	}

}
