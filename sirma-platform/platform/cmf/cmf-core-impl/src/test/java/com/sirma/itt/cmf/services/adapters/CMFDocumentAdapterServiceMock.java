package com.sirma.itt.cmf.services.adapters;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService;
import com.sirma.itt.cmf.services.adapter.descriptor.UploadWrapperDescriptor;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.seip.domain.DmsAware;
import com.sirma.itt.seip.domain.instance.DMSInstance;
import com.sirma.itt.seip.io.FileAndPropertiesDescriptor;
import com.sirma.itt.seip.io.FileDescriptor;

/**
 * The Class CMFDocumentAdapterServiceMock.
 */
@ApplicationScoped
public class CMFDocumentAdapterServiceMock implements CMFDocumentAdapterService {

	@Override
	public FileAndPropertiesDescriptor checkOut(DMSInstance document, String userId) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return null;
	}

	@Override
	public String deleteAttachment(DMSInstance documentInstance) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return null;
	}

	@Override
	public FileDescriptor getDocumentPreview(DmsAware documentInstance, String mimetype) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return null;
	}

	@Override
	public FileAndPropertiesDescriptor moveDocument(DMSInstance src, DMSInstance targetSection) throws DMSException {
		// Not used method
		return null;
	}

	@Override
	public FileAndPropertiesDescriptor uploadNewVersion(DMSInstance document, UploadWrapperDescriptor descriptor)
			throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return null;
	}

	@Override
	public FileAndPropertiesDescriptor uploadContent(final DMSInstance documentInstance,
			UploadWrapperDescriptor descriptor, Set<String> aspectsProp) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return null;
	}

	@Override
	public String getDocumentDirectAccessURI(DMSInstance instance) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return null;
	}

	@Override
	public String getLibraryDMSId() {
		RESTClientMock.checkAuthenticationInfo();
		return null;
	}

}
