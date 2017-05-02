package com.sirma.itt.seip.adapters.iiif;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.seip.content.ContentUploader;
import com.sirma.itt.seip.content.StoreException;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.io.FileAndPropertiesDescriptor;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Base plugin for uploading images.
 *
 * @author Nikolay Ch
 */
@ApplicationScoped
@Extension(target = ContentUploader.TARGET_NAME, order = 10)
public class ImageContentUploader implements ContentUploader {

	@Inject
	private ImageAdapterService imageAdapter;
	@Inject
	private ImageServerConfigurations imageServerConfigurations;

	@Override
	public FileAndPropertiesDescriptor uploadContent(Instance instance, FileDescriptor descriptor) {
		if (!imageServerConfigurations.isImageServerEnabled().get().booleanValue()) {
			return null;
		}
		if (instance.getString(DefaultProperties.MIMETYPE, "").contains("image")) {
			try {
				return imageAdapter.upload(instance, descriptor);
			} catch (DMSException e) {
				throw new StoreException(e);
			}
		}
		return null;
	}

}
