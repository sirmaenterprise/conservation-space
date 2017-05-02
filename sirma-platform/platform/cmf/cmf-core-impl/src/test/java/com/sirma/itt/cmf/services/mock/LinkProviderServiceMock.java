package com.sirma.itt.cmf.services.mock;

import java.io.Serializable;

import com.sirma.itt.seip.instance.util.LinkProviderService;

/**
 * The LinkProviderService mock impl
 */
public class LinkProviderServiceMock implements LinkProviderService {

	@Override
	public String buildLink(Serializable instanceId) {
		return "/emf/entity/" + instanceId;
	}

}
