package com.sirma.itt.cmf.services.mock;


import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.services.LinkProviderService;

/**
 * The LinkProviderService mock impl
 */
public class LinkProviderServiceMock implements LinkProviderService {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String buildLink(Instance instance) {
		return "/emf/entity/" + (instance != null ? instance.getId() : "NULL");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String buildLink(Instance instance, String tab) {
		return buildLink(instance) + "/" + tab;
	}

}
