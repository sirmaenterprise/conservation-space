package com.sirma.sep.content;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;

import org.junit.Test;

import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.testutil.io.FailingInputStream;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.DefaultContentDigestProvider;

/**
 * Test for {@link DefaultContentDigestProvider}
 *
 * @author BBonev
 */
@SuppressWarnings("static-method")
public class DefaultContentDigestProviderTest {

	private static final String DATA = "sdfhsdhfsflhskfhs";

	@Test
	public void computeDigest() throws Exception {
		Content content = Content.create("test.txt",
				FileDescriptor.create(() -> new ByteArrayInputStream(DATA.getBytes()), DATA.length()));
		DefaultContentDigestProvider digestProvider = new DefaultContentDigestProvider();
		assertNotNull(digestProvider.digest(content));
	}

	@Test()
	public void onInvalidContent() throws Exception {
		Content content = Content.create("test.txt", FileDescriptor.create(() -> new FailingInputStream(), 0));
		DefaultContentDigestProvider digestProvider = new DefaultContentDigestProvider();
		assertNull(digestProvider.digest(content));
	}
}
