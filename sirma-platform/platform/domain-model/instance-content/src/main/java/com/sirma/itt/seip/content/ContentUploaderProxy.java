/**
 *
 */
package com.sirma.itt.seip.content;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.io.FileAndPropertiesDescriptor;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.plugin.ExtensionPoint;

/**
 * Chaining proxy implementation of {@link ContentUploader}. The implementation iterates all plugins and calls them
 * until non null result is returned indicating successful upload.
 *
 * @author BBonev
 */
@ApplicationScoped
public class ContentUploaderProxy implements ContentUploader {

	@Inject
	@ExtensionPoint(ContentUploader.TARGET_NAME)
	private Iterable<ContentUploader> contentUploaders;

	@Override
	public FileAndPropertiesDescriptor uploadContent(Instance instance, FileDescriptor descriptor) {
		for (ContentUploader contentUploader : contentUploaders) {
			FileAndPropertiesDescriptor result = contentUploader.uploadContent(instance, descriptor);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

}
