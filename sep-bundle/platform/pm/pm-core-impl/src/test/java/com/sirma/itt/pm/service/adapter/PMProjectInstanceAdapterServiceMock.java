package com.sirma.itt.pm.service.adapter;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.cmf.services.adapters.RESTClientMock;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.services.adapter.CMFProjectInstanceAdapterService;

/**
 * The mock up for {@link CMFProjectInstanceAdapterService}.
 */
@ApplicationScoped
public class PMProjectInstanceAdapterServiceMock implements CMFProjectInstanceAdapterService {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -6780681855859625409L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String createProjectInstance(ProjectInstance projectInstance) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return UUID.randomUUID().toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String updateProjectInstance(ProjectInstance projectInstance) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return projectInstance.getDmsId().toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String deleteProjectInstance(ProjectInstance instance, boolean permanent)
			throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return instance.getDmsId().toString();
	}

}
