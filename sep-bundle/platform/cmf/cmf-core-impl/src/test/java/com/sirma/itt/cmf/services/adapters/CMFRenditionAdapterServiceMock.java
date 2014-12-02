package com.sirma.itt.cmf.services.adapters;

import java.io.OutputStream;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.emf.adapter.CMFRenditionAdapterService;
import com.sirma.itt.emf.adapter.DMSException;

/**
 * The CMFRenditionAdapterService mock.
 */
@ApplicationScoped
public class CMFRenditionAdapterServiceMock implements CMFRenditionAdapterService {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 6549116566793313880L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPrimaryThumbnailURI(String dmsId) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int downloadThumbnail(String dmsId, OutputStream buffer) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return 0;
	}

}
