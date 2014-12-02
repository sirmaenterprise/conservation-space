/**
 *
 */
package com.sirma.itt.cmf.alfresco4.services;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.sirma.itt.cmf.alfresco4.descriptor.AlfrescoFileDescriptor;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.services.adapter.CMFContentAdapterService;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.instance.model.DMSInstance;
import com.sirma.itt.emf.instance.model.TenantAware;
import com.sirma.itt.emf.properties.model.PropertyModel;
import com.sirma.itt.emf.remote.RESTClient;

/**
 * Adapter implementation for content service.<br>
 * REVIEW: should provide more non restrictive method for fetching content from DMS
 *
 * @author hackyou
 */
@ApplicationScoped
public class ContentAlfresco4Service implements CMFContentAdapterService {

	/** The rest client. */
	@Inject
	private Instance<RESTClient> restClient;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileDescriptor getContentDescriptor(DMSInstance doc) throws DMSException {
		// REVIEW ot tazi proverka izliza che hich ne poddyrjame documenti bez content v tqh, a ima
		// momenti, v koito ima document, no toi vse oshte nqma kachen content. Spored men kogato
		// dmsId e null, togava da se vryshta null za descriptora, kato tova se opishe i v doc-a.
		// Trqbva da se fixne i ContentServiceImpl
		if (doc == null) {
			throw new DMSException("Provided instance is null!");
		}
		Serializable temporaryId = null;
		if (doc instanceof PropertyModel) {
			// higher priority get the working location
			temporaryId = ((PropertyModel) doc).getProperties().get(
					DocumentProperties.WORKING_COPY_LOCATION);
			if (temporaryId == null) {
				if (doc.getDmsId() == null) {
					temporaryId = ((PropertyModel) doc).getProperties().get(
							DocumentProperties.CLONED_DMS_ID);
				} else {
					temporaryId = doc.getDmsId();
				}
			}
		} else if (doc.getDmsId() != null) {
			temporaryId = doc.getDmsId();
		}
		if (temporaryId == null) {
			throw new DMSException("Did not found any location descriptor for: " + doc);
		}
		String containerId = null;
		if (doc instanceof TenantAware) {
			containerId = ((TenantAware) doc).getContainer();
		}
		return getContentDescriptor(temporaryId.toString(), containerId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileDescriptor getContentDescriptor(String dmsId) throws DMSException {
		return getContentDescriptor(dmsId, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FileDescriptor getContentDescriptor(String dmsId, String containerId)
			throws DMSException {
		return new AlfrescoFileDescriptor(dmsId, containerId, restClient.get());
	}
}
