package com.sirma.itt.cmf.alfresco4.content;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.DMSInstanceAdapterService;
import com.sirma.itt.seip.content.ContentUploader;
import com.sirma.itt.seip.domain.instance.DMSInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.OwnedModel;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.io.FileAndPropertiesDescriptor;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.plugin.Extension;

/**
 * @author Nikolay Ch
 */
@ApplicationScoped
@Extension(target = ContentUploader.TARGET_NAME, order = 30)
public class DocumentContentUploader implements ContentUploader {

	@Inject
	private DMSInstanceAdapterService genericAdapterService;

	@Override
	public FileAndPropertiesDescriptor uploadContent(Instance instance, FileDescriptor descriptor) {
		try {
			if (((OwnedModel) instance).getOwningReference() == null
					|| ((OwnedModel) instance).getOwningInstance() == null) {
				return genericAdapterService.attachDocumenToLibrary((DMSInstance) instance, descriptor, null);
			}
			return genericAdapterService.attachDocumentToInstance((DMSInstance) instance, descriptor, null);
		} catch (DMSException e) {
			throw new RollbackedRuntimeException(e);
		}
	}

}
