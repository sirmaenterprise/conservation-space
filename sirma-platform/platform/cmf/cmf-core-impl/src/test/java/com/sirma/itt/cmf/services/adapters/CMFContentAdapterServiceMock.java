package com.sirma.itt.cmf.services.adapters;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.seip.content.ContentAdapterService;
import com.sirma.itt.seip.domain.instance.DMSInstance;
import com.sirma.itt.seip.io.FileDescriptor;

/**
 * The Class CMFContentAdapterServiceMock.
 */
@ApplicationScoped
public class CMFContentAdapterServiceMock implements ContentAdapterService {

	@Override
	public FileDescriptor getContentDescriptor(DMSInstance instance) {
		return null;
	}

	@Override
	public FileDescriptor getContentDescriptor(String dmsId) {
		return null;
	}

	@Override
	public FileDescriptor getContentDescriptor(String dmsId, String containerId) {
		return null;
	}

}
