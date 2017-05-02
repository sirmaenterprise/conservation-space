package com.sirma.itt.seip.content.extensions;

import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentInfo;
import com.sirma.itt.seip.content.ContentLoader;
import com.sirma.itt.seip.content.ContentService;
import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.plugin.Extension;

/**
 * {@link ContentLoader} that integrates the new {@link InstanceContentService} into the old {@link ContentService}.
 * This is before all other providers in order to avoid errors in them for missing content.
 *
 * @author BBonev
 */
@Extension(target = ContentLoader.TARGET_NAME, order = 5)
public class DefaultContentLoader implements ContentLoader {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	@Inject
	private InstanceContentService instanceContentService;
	private static final Pattern TEXT_MIMETYPES = Pattern.compile("application/json|application/xml|text/.+$");

	@Override
	public boolean isApplicable(Object object) {
		return object instanceof Serializable;
	}

	@Override
	public String loadContent(Object object) {
		// this method should return the extracted content

		ContentInfo primaryView = instanceContentService.getContent((Serializable) object, Content.PRIMARY_VIEW);
		// for UI2 instance if there is no view then the instance is not managed by the service
		if (!primaryView.exists()) {
			return null;
		}
		ContentInfo primaryContent = instanceContentService.getContent((Serializable) object, Content.PRIMARY_CONTENT);
		// for now will only return the content for text representable mime types
		if (primaryContent.exists() && isTextMimeType(primaryContent.getMimeType())) {
			String previewData = getTextContent(primaryView);
			String contentData = getTextContent(primaryContent);
			return StringUtils.trimToNull(previewData + contentData);
		}
		return StringUtils.trimToNull(getTextContent(primaryView));
	}

	private static String getTextContent(ContentInfo primaryView) {
		try {
			return primaryView.asString();
		} catch (IOException e) {
			LOGGER.trace("Could not read view for instance", e);
			return "";
		}
	}

	private static boolean isTextMimeType(String mimeType) {
		return mimeType != null && TEXT_MIMETYPES.matcher(mimeType).matches();
	}

}
