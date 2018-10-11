package com.sirma.itt.seip.content;

import java.io.Serializable;
import java.util.Base64;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;

/**
 * Implementation of the {@link ContentResourceService}.
 *
 * @author Nikolay Ch
 */
@Singleton
public class ContentResourceManagerServiceImpl implements ContentResourceManagerService {

	@Inject
	private InstanceContentService instanceContentService;

	@Override
	public void uploadContent(Serializable instanceId, Map<Serializable, String> contentMapping) {
		EmfInstance instance = new EmfInstance();
		instance.setId(instanceId);
		for (Entry<Serializable, String> contentEntry : contentMapping.entrySet()) {
			Content content = Content.createEmpty().setPurpose(contentEntry.getKey().toString())
					.setName("content-" + contentEntry.getKey())
					.setContent(extractAndDecodeBase64(contentEntry.getValue()))
					.setVersionable(true)
					.setMimeType(extractMimetypeBase64(contentEntry.getValue()));
			instanceContentService.saveContent(instance, content);
		}
	}

	@Override
	public ContentInfo getContent(Serializable instanceId, String purpose) {
		return instanceContentService.getContent(instanceId, purpose);
	}

	/**
	 * It obtains the base64 format of an image which is consisted of a prefix and the real image representation. So it
	 * removes the prefix and decodes the image to a byte array.
	 *
	 * @param fullBase64Encoding
	 *            the full encoding of the image
	 * @return the decoded image
	 */
	private static byte[] extractAndDecodeBase64(String fullBase64Encoding) {
		String base64Encoding = fullBase64Encoding.split(",")[1];
		return Base64.getDecoder().decode(base64Encoding);
	}
	/**
	 * Extracts the mimetype of a base64 encoded string.
	 * @param fullBase64Encoding the full base64 string
	 */
	private static String extractMimetypeBase64(String fullBase64Encoding){
		String [] base64EncodingParts = fullBase64Encoding.split(":|;");
		return base64EncodingParts[1];
	}

}
