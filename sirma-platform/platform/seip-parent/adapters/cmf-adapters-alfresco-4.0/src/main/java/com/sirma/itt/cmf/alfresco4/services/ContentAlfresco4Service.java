package com.sirma.itt.cmf.alfresco4.services;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.sirma.itt.cmf.alfresco4.descriptor.AlfrescoFileDescriptor;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.seip.adapters.AdaptersConfiguration;
import com.sirma.itt.seip.adapters.remote.RESTClient;
import com.sirma.itt.seip.content.ContentAdapterService;
import com.sirma.itt.seip.domain.instance.DMSInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.io.FileDescriptor;

/**
 * Adapter implementation for content service.<br>
 * REVIEW: should provide more non restrictive method for fetching content from DMS
 *
 * @author hackyou
 */
@ApplicationScoped
public class ContentAlfresco4Service implements ContentAdapterService {

	/** The rest client. */
	@Inject
	private Instance<RESTClient> restClient;

	@Inject
	private AdaptersConfiguration adaptersConfiguration;

	@Override
	public FileDescriptor getContentDescriptor(DMSInstance doc) {
		// higher priority get the working location
		Serializable temporaryId = doc.get(DocumentProperties.WORKING_COPY_LOCATION);
		if (temporaryId == null) {
			if (doc.getDmsId() == null) {
				temporaryId = doc.get(DocumentProperties.CLONED_DMS_ID);
			} else {
				temporaryId = doc.getDmsId();
			}
		}
		if (temporaryId == null) {
			temporaryId = doc.get(DefaultProperties.ATTACHMENT_LOCATION);
		}
		if (temporaryId == null) {
			return null;
		}
		return getContentDescriptor(temporaryId.toString(), adaptersConfiguration.getDmsContainerId().get());
	}

	@Override
	public FileDescriptor getContentDescriptor(String dmsId) {
		return getContentDescriptor(dmsId, adaptersConfiguration.getDmsContainerId().get());
	}

	@Override
	public FileDescriptor getContentDescriptor(String dmsId, String containerId) {
		return new AlfrescoFileDescriptor(dmsId, containerId, null, restClient.get());
	}
}
