/**
 *
 */
package com.sirma.itt.seip.content.type;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.mime.MimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.type.MimeTypeResolver;

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

	/**
	 * Resolves the mime type of the given input stream. Note that if the passed input stream must support
	 * {@link InputStream#mark(int)} and {@link InputStream#reset()} methods. The method does not close the stream.
	 * <br/>
	 * Method use {@link Tika#detect(InputStream)}
	 * There is tika-mimetype.xml in tika jar. Where mimetypes are described.
	 *
	 * Tika use two steps to detect mimetype:
	 * 1. First step will detect from <code>stream</code> mimetype.
	 * 2. Second step will detect from filename.
	 *
	 * after this steps it will use "tika-mimetype.xml" for more precise detection.
	 *
	 * For example "tika-mimetype.xml" contains below description of video/webm
	 * <pre>
	 * {@code <mime-type type="video/webm">
	 *     <sub-class-of type="application/x-matroska"/>
	 *     <glob pattern="*.webm" />
	 *   </mime-type>
	 * }
	 * </pre>
	 * On first step tika will detect that mimetype is "application/x-matroska"
	 * On second step tika will detect that mimeype is "video/webm"
	 *
	 * after that will check if mimetype from second step is subtype of mimetype of first step
	 * if yes second will be returned otherwise first.
	 * {@link org.apache.tika.mime.MimeTypes#applyHint(List, MimeType)}
	 *
	 * @param stream
	 *         the stream, which mime type is searched
	 * @param fileName
	 *         - the filename of document from which <code>stream</code> is taken.
	 * @return - return detected mimetype or null if exception occured.
	 */
	@Override
	public String getMimeType(InputStream stream, String fileName) {
		if (stream != null) {
			try (TikaInputStream tis = TikaInputStream.get(stream)) {
				return detectOrGetNull(tika.detect(tis, fileName));
			} catch (IOException e) {
				LOGGER.error("Error occurred, while detecting the mime type", e);
			}
		}
		return null;
	}

	@Override
	public String getMimeType(File file) {
		if (file != null && file.exists() && file.isFile()) {
			try {
				return detectOrGetNull(tika.detect(file));
			} catch (IOException e) {
				LOGGER.error("Error occurred, while detecting the mime type", e);
			}
		}
		return null;
	}

	@Override
	public String getMimeType(byte[] bytes, String fileName) {
		if (bytes != null && bytes.length > 0) {
			return detectOrGetNull(tika.detect(bytes, fileName));
		}
		return null;
	}

	@Override
	public String resolveFromName(String fileName) {
		if (StringUtils.isNotBlank(fileName) && fileName.contains(".")) {
			return detectOrGetNull(tika.detect(fileName));
		}
		return null;
	}

	/**
	 * Check if detected mimetype is "application/octet-stream". If yes null will be returned.
	 * Tika implementation of detection return "application/octet-stream" if can't detect mimetype so we have to check
	 * it and return null.
	 * @param type - the type to be checked
	 * @return detected mimetype or null.
	 */
	private String detectOrGetNull(String type) {
		if (nullSafeEquals(type, OCTET_STREAM_MIMETYPE)) {
			return null;
		}
		return type;
	}
}