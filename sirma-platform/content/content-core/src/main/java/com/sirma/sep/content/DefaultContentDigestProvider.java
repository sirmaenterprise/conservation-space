package com.sirma.sep.content;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.security.DigestInputStream;
import java.security.MessageDigest;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentDigestProvider;

/**
 * Default implementation of {@link ContentDigestProvider} that computes MD5 digest of the entire content.
 *
 * @author BBonev
 */
@Extension(target = ContentDigestProvider.EXTENSION_NAME, order = Double.MAX_VALUE)
public class DefaultContentDigestProvider implements ContentDigestProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	@Override
	public boolean accept(Content content) {
		// accept all that reach here
		return true;
	}

	@Override
	public String digest(Content content) {
		LOGGER.debug("Digesting content {} with size {}", content.getName(), content.getContentLength());
		// we need fresh digest each time
		// as message digest is stateful and not thread safe
		MessageDigest digest = DigestUtils.getMd5Digest();
		try (DigestInputStream stream = new DigestInputStream(content.getContent().getInputStream(), digest)) {
			// consume the stream so that the digest is computed
			IOUtils.copy(stream, VoidOutputStream.INSTANCE);
		} catch (IOException e) {
			LOGGER.warn("Failed to consume input stream when calculating digest for ", content.getName(), e);
			return null;
		}
		return Hex.encodeHexString(digest.digest());
	}

	/**
	 * Output stream that does nothing with the content
	 *
	 * @author BBonev
	 */
	private static class VoidOutputStream extends OutputStream {

		static final VoidOutputStream INSTANCE = new VoidOutputStream();

		@Override
		public void write(int b) throws IOException {
			// do nothing with it as we just want to read from the input but we do not care for the data
		}
	}
}
