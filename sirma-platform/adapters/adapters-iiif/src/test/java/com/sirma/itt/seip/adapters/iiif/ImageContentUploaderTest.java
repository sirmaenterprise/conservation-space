package com.sirma.itt.seip.adapters.iiif;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNull;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.io.FileAndPropertiesDescriptor;
import com.sirma.itt.seip.io.FileDescriptor;
import com.srima.itt.seip.adapters.mock.ImageServerConfigurationsMock;

/**
 * Tests the functionality of the image content uploader.
 *
 * @author Nikolay Ch
 */
@RunWith(MockitoJUnitRunner.class)
public class ImageContentUploaderTest {

	@InjectMocks
	private ImageContentUploader contentUploader;

	@Mock
	private ImageAdapterService imageService;

	@Spy
	private ImageServerConfigurationsMock configMock = new ImageServerConfigurationsMock();

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Verifies that the ImageService doesn't get called if the image server integration is not enabled.
	 *
	 * @throws Exception
	 *             if an error occurs
	 */
	@Test
	public void testsEnablement() throws Exception {
		configMock.setEnabled(false);
		Instance instance = new EmfInstance();
		instance.add(DefaultProperties.MIMETYPE, "image/jpeg");
		contentUploader.uploadContent(instance, mock(FileDescriptor.class));
		verify(imageService, never()).upload(any(Instance.class), any(FileDescriptor.class));
	}

	/**
	 * Tests the correct processing of the data.
	 *
	 * @throws DMSException
	 *             if an error occurs
	 */
	@Test
	public void testUploadContent() throws DMSException {
		Instance instance = new EmfInstance();
		FileDescriptor descriptor = new FileDescriptor() {
			private static final long serialVersionUID = 1L;

			@Override
			public InputStream getInputStream() {
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
			public void close() {
				// nothing to do
			}
		};
		FileAndPropertiesDescriptor fileDescriptor = new FileAndPropertiesDescriptor() {
			private static final long serialVersionUID = 1L;

			@Override
			public InputStream getInputStream() {
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
			public Map<String, Serializable> getProperties() {
				return null;
			}

			@Override
			public void close() {
				// nothing to do
			}
		};
		assertNull(contentUploader.uploadContent(instance, descriptor));
		instance.add(DefaultProperties.MIMETYPE, "image/jpeg");
		configMock.setEnabled(true);
		when(imageService.upload(any(EmfInstance.class), any(FileAndPropertiesDescriptor.class)))
				.thenReturn(fileDescriptor);
		assertEquals(contentUploader.uploadContent(instance, descriptor), fileDescriptor);
	}

}
