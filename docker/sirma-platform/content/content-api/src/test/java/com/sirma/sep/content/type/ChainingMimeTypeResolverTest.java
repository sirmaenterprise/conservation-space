/**
 *
 */
package com.sirma.sep.content.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.sep.content.type.ChainingMimeTypeResolver;
import com.sirma.sep.content.type.MimeTypeResolver;

/**
 * Test for ChainingMimeTypeResolver.
 *
 * @author A. Kunchev
 */
public class ChainingMimeTypeResolverTest {

	private static final String STREAM_MIME_TYPE = "application/octet-stream";

	@InjectMocks
	private ChainingMimeTypeResolver resolver = new ChainingMimeTypeResolver();

	@Mock
	private MimeTypeResolver mockResolver;

	List<MimeTypeResolver> resolvers = new ArrayList<>();
	@Spy
	Plugins<MimeTypeResolver> resolverPlugins = new Plugins<>("", resolvers);

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		resolvers.clear();
		resolvers.add(mockResolver);
	}

	@Test
	public void getMimeTypeBufferedInputStream_nullStream_nullResult() {
		BufferedInputStream stream = null;
		String mimeType = resolver.getMimeType(stream, null);
		assertNull(mimeType);
		verify(mockResolver).getMimeType(any(BufferedInputStream.class), Matchers.eq(null));
	}

	@Test
	public void getMimeTypeBufferedInputStream_notNullStream_notNullResult() {
		String fileName = "test.txt";
		when(mockResolver.getMimeType(any(BufferedInputStream.class), Matchers.eq(fileName))).thenReturn(STREAM_MIME_TYPE);
		String mimeType = resolver.getMimeType(Mockito.mock(BufferedInputStream.class), fileName);
		assertEquals(STREAM_MIME_TYPE, mimeType);
	}

	@Test
	public void getMimeTypeFile_nullFile_nullResult() {
		File file = null;
		String mimeType = resolver.getMimeType(file);
		assertNull(mimeType);
		verify(mockResolver).getMimeType(any(File.class));
	}

	@Test
	public void getMimeTypeFile_notNullFile_notNullResult() {
		File file = Mockito.mock(File.class);
		Mockito.when(mockResolver.getMimeType(Matchers.any(File.class))).thenReturn("application/pdf");
		String mimeType = resolver.getMimeType(file);
		assertEquals("application/pdf", mimeType);
	}

	@Test
	public void getMimeTypeBytes_nullBytes_nullResult() {
		byte[] bytes = null;
		String mimeType = resolver.getMimeType(bytes, null);
		assertNull(mimeType);
	}

	@Test
	public void getMimeTypeBytes_notNullBytes_notNullResult() {
		byte[] bytes = "bytes".getBytes();
		String fileName = "test.pdf";
		Mockito.when(mockResolver.getMimeType(Matchers.any(byte[].class), Matchers.eq(fileName))).thenReturn("application/pdf");
		String mimeType = resolver.getMimeType(bytes, fileName);
		assertNotNull(mimeType);
	}

	@Test
	public void resolveForName() {
		Mockito.when(mockResolver.resolveFromName("test.txt")).thenReturn("plain/text");
		String mimeType = resolver.resolveFromName("test.txt");
		assertEquals("plain/text", mimeType);
	}

}
