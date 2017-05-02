package com.sirma.itt.seip.adapters.iiif;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.httpclient.URI;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.adapters.remote.RESTClient;
import com.sirma.itt.seip.content.ContentAccessProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Plugin for accessing the content of an uploaded image.
 *
 * @author Nikolay Ch
 */
@ApplicationScoped
@Extension(target = ContentAccessProvider.TARGET_NAME, order = 10)
public class ImageContentAccessProvider implements ContentAccessProvider {

	private static final String IMAGE = "image";

	@Inject
	private ImageAdapterService imageAdapter;

	@Inject
	private javax.enterprise.inject.Instance<RESTClient> restClient;

	@Override
	public String getContentURI(Instance instance) {
		if (!instance.getString(DefaultProperties.MIMETYPE, "").contains(IMAGE)) {
			return null;
		}

		return "/share/content/" + instance.getId();
	}

	@Override
	public FileDescriptor getDescriptor(Instance instance) {
		if (!instance.getString(DefaultProperties.MIMETYPE, "").contains(IMAGE)) {
			return null;
		}

		String contentUrl = imageAdapter.getContentUrl(instance);
		if (StringUtils.isNullOrEmpty(contentUrl)) {
			return null;
		}

		URI imageAddress;
		try {
			imageAddress = new URI(contentUrl, false);
		} catch (Exception e) {
			throw new EmfRuntimeException(e);
		}
		return new ImageFileDescriptor(imageAddress, null, restClient.get());
	}

}
