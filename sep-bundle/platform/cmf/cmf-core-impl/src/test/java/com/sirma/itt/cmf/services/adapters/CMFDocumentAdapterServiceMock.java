package com.sirma.itt.cmf.services.adapters;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.services.adapter.CMFDocumentAdapterService;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.FileAndPropertiesDescriptor;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.instance.model.DMSInstance;

/**
 * The Class CMFDocumentAdapterServiceMock.
 */
@ApplicationScoped
public class CMFDocumentAdapterServiceMock implements CMFDocumentAdapterService {

	@Override
	public Map<DocumentInfoOperation, Serializable> performDocumentInfoOperation(
			DocumentInstance document, Set<DocumentInfoOperation> operations) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return null;
	}

	@Override
	public FileAndPropertiesDescriptor performDocumentOperation(DocumentInstance document,
			DocumentOperation operation) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return null;
	}

	@Override
	public FileAndPropertiesDescriptor performDocumentOperation(DocumentInstance document,
			String userId, DocumentOperation operation) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return null;
	}

	@Override
	public FileAndPropertiesDescriptor checkOut(DocumentInstance document, String userId)
			throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return null;
	}

	@Override
	public String deleteAttachment(DocumentInstance documentInstance) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return null;
	}

	@Override
	public FileDescriptor getDocumentPreview(DocumentInstance documentInstance, String mimetype)
			throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return null;
	}

	@Override
	public DocumentInstance getDocumentVersion(DocumentInstance instance, String version)
			throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return null;
	}

	@Override
	public DocumentInstance revertVersion(DocumentInstance instance, String version)
			throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return null;
	}

	@Override
	public FileAndPropertiesDescriptor moveDocument(DocumentInstance src,
			DMSInstance targetSection) throws DMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileAndPropertiesDescriptor copyDocument(DocumentInstance src,
			DMSInstance targetSection, String newDocumentName) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return null;
	}

	@Override
	public FileAndPropertiesDescriptor uploadNewVersion(DocumentInstance document,
			FileAndPropertiesDescriptor descriptor) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return null;
	}

	@Override
	public FileAndPropertiesDescriptor uploadContent(final DocumentInstance documentInstance,
			FileAndPropertiesDescriptor descriptor, Set<String> aspectsProp) throws DMSException {
		RESTClientMock.checkAuthenticationInfo();
		return null;
	}

	@Override
	public String getDocumentDirectAccessURI(DocumentInstance instance) throws DMSException {
		return null;
	}

}
