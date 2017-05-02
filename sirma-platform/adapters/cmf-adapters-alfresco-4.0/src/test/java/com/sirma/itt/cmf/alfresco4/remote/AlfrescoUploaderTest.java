package com.sirma.itt.cmf.alfresco4.remote;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.httpclient.methods.multipart.FilePartSource;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.cmf.services.adapter.descriptor.UploadWrapperDescriptor;
import com.sirma.itt.seip.GenericProxy;
import com.sirma.itt.seip.adapters.remote.StreamPartSource;
import com.sirma.itt.seip.content.descriptor.LocalFileDescriptor;
import com.sirma.itt.seip.content.descriptor.LocalProxyFileDescriptor;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.io.FileAndPropertiesDescriptor;
import com.sirma.itt.seip.io.FileDescriptor;

/**
 * Test for {@link AlfrescoUploader}.
 *
 * @author A. Kunchev
 */
public class AlfrescoUploaderTest {

	private AlfrescoUploader alfrescoUploader;

	@Before
	public void setup() {
		alfrescoUploader = new AlfrescoUploader();
	}

	@Test
	public void getPartSource_nullDescriptor() {
		assertNull(alfrescoUploader.getPartSource(null));
	}

	@Test
	public void getPartSource_StreamPartSource() {
		UploadWrapperDescriptor descriptor = new UploadWrapperDescriptor(mock(FileAndPropertiesDescriptor.class));
		when(descriptor.getDelegate().getInputStream()).thenReturn(mock(InputStream.class));
		PartSource partSource = alfrescoUploader.getPartSource(descriptor);
		assertNotNull(partSource);
		assertTrue(partSource instanceof StreamPartSource);
	}

	@Test
	public void getPartSource_LocalProxyFileDescriptor() throws IOException {
		LocalProxyFileDescriptor fileDescriptor = mock(LocalProxyFileDescriptor.class);
		Path path = Files.createTempFile("", "");
		when(fileDescriptor.getId()).thenReturn(path.toString());
		when(fileDescriptor.getProxiedId()).thenReturn(path.toString());
		CountingFileDescriptorMock descriptor = new CountingFileDescriptorMock(fileDescriptor);
		PartSource partSource = alfrescoUploader.getPartSource(descriptor);
		assertNotNull(partSource);
		assertTrue(partSource instanceof FilePartSource);
		path.toFile().delete();
	}

	@Test
	public void getPartSource_LocalFileDescriptor() throws IOException {
		LocalFileDescriptor fileDescriptor = mock(LocalFileDescriptor.class);
		Path path = Files.createTempFile("", "");
		when(fileDescriptor.getId()).thenReturn(path.toString());
		CountingFileDescriptorMock descriptor = new CountingFileDescriptorMock(fileDescriptor);
		PartSource partSource = alfrescoUploader.getPartSource(descriptor);
		assertNotNull(partSource);
		assertTrue(partSource instanceof FilePartSource);
		path.toFile().delete();
	}

	@Test(expected = EmfRuntimeException.class)
	public void getPartSource_exceptionNoFile() throws IOException {
		LocalFileDescriptor fileDescriptor = mock(LocalFileDescriptor.class);
		when(fileDescriptor.getId()).thenReturn("id");
		CountingFileDescriptorMock descriptor = new CountingFileDescriptorMock(fileDescriptor);
		alfrescoUploader.getPartSource(descriptor);
	}

	/**
	 * Mock of CountingFileDescriptor.
	 *
	 * @author A. Kunchev
	 */
	class CountingFileDescriptorMock implements FileDescriptor, GenericProxy<FileDescriptor> {

		private static final long serialVersionUID = 1L;
		private FileDescriptor descriptor;

		/**
		 * Constructor.
		 *
		 * @param descriptor
		 *            the file descriptor
		 */
		public CountingFileDescriptorMock(FileDescriptor descriptor) {
			this.descriptor = descriptor;
		}

		@Override
		public FileDescriptor getTarget() {
			return descriptor;
		}

		@Override
		public void setTarget(FileDescriptor target) {
			descriptor = target;
		}

		@Override
		public FileDescriptor cloneProxy() {
			return null;
		}

		@Override
		public FileDescriptor clone() {
			return null;
		}

		@Override
		public String getId() {
			return null;
		}

		@Override
		public String getContainerId() {
			return null;
		}

		@Override
		public InputStream getInputStream() {
			return null;
		}

		@Override
		public void close() {
			// nothing to clone
		}

	}

}
