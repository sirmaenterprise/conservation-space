/**
 *
 */
package com.sirma.itt.seip.content.type;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;

import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.plugin.Extension;

/**
 * Concrete implementation of {@link TikaMimeTypeResolver}. Implements the methods for resolving the mime type of the
 * stream and file using Apache Tika. This extension will be executed first.
 *
 * @author A. Kunchev
 */
@Extension(target = MimeTypeResolver.TARGET_NAME, enabled = true, order = 5)
public class TikaMimeTypeResolver implements MimeTypeResolver {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String OCTET_STREAM_MIMETYPE = "application/octet-stream";

	private final Tika tika = new Tika();

	@Override
	public String getMimeType(InputStream stream) {
		if (stream != null) {
			try (TikaInputStream tis = TikaInputStream.get(stream)) {
				return callExtractor(tis, tika::detect);
			} catch (IOException e) {
				LOGGER.error("Error occurred, while detecting the mime type", e);
			}
		}
		return null;
	}

	@Override
	public String getMimeType(File file) {
		if (file != null && file.exists() && file.isFile()) {
			return callExtractor(file, tika::detect);
		}
		return null;
	}

	@Override
	public String getMimeType(byte[] bytes) {
		if (bytes != null && bytes.length > 0) {
			return callExtractor(bytes, tika::detect);
		}
		return null;
	}

	@Override
	public String resolveFromName(String fileName) {
		if (StringUtils.isNotBlank(fileName) && fileName.contains(".")) {
			return callExtractor(fileName, tika::detect);
		}
		return null;
	}

	private static <T> String callExtractor(T toTest, Detector<T> tester) {
		try {
			String detected = tester.detect(toTest);
			// Tika is not good for detecting the type for non document types so something else should be tried
			if (nullSafeEquals(detected, OCTET_STREAM_MIMETYPE)) {
				return null;
			}
			return detected;
		} catch (IOException e) {
			LOGGER.debug("Error occurred, while detecting the mime type", e);
		}
		return null;
	}

	/**
	 * Internal functional interface used for detection of mimetypes
	 *
	 * @param <T>
	 * 		the generic type
	 */
	private interface Detector<T> {

		/**
		 * Detect.
		 *
		 * @param arg
		 * 		the arg
		 * @return the string
		 * @throws IOException
		 * 		Signals that an I/O exception has occurred.
		 */
		String detect(T arg) throws IOException;
	}

}
