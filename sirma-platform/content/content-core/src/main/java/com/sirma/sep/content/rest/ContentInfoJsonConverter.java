package com.sirma.sep.content.rest;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.CONTENT_LENGTH;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.MIMETYPE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.NAME;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.PRIMARY_CONTENT_ID;

import javax.json.stream.JsonGenerator;

import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.sep.content.ContentInfo;

/**
 * Class for converting a content info to a json.
 * 
 * @author Nikolay Ch
 */
public class ContentInfoJsonConverter {

	/**
	 * Private constructor to override the default one.
	 */
	private ContentInfoJsonConverter() {
	}

	/**
	 * Convert the content info to json and writes it to a stream generator.
	 * 
	 * @param generator
	 *            the stream generator
	 * @param info
	 *            the content info to be written
	 */
	public static void convertAndWriteToGenerator(JsonGenerator generator, ContentInfo info) {
		generator
				.write(PRIMARY_CONTENT_ID, info.getContentId())
					.write(NAME, info.getName())
					.write(MIMETYPE, info.getMimeType())
					.write(CONTENT_LENGTH, info.getLength());

		// If null property is written an error is thrown.
		JSON.addIfNotNull(generator, JsonKeys.CONTENT_CHECKSUM, info.getChecksum());
	}
}
