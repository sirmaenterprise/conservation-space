package com.sirma.itt.seip.adapters.iiif;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.CONTENT_LENGTH;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.seip.adapters.remote.FTPConfiguration;
import com.sirma.itt.seip.adapters.remote.FtpClient;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.io.FileAndPropertiesDescriptor;
import com.sirma.itt.seip.io.FileDescriptor;
import com.srima.itt.seip.adapters.mock.ImageServerConfigurationsMock;

/**
 * Tests {@link ImageAdapterServiceImpl}.
 *
 * @author BBonev
 */
public class ImageAdapterServiceImplTest {

	@InjectMocks
	private ImageAdapterServiceImpl adapterService;

	@Spy
	private ImageServerConfigurationsMock ftpConfig = new ImageServerConfigurationsMock();

	@Mock
	private FtpClient ftpClient;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		ftpConfig.setAsyncThreshold(0L);
	}

	@Test
	public void testUpload() throws Exception {
		ftpConfig.setEnabled(true);

		FileDescriptor fileDescriptor = mock(FileDescriptor.class);
		Instance instance = new EmfInstance();
		instance.setId("emf:instance");
		instance.add(DefaultProperties.MIMETYPE, "image/jpg");
		ftpConfig.setImageFTPServerConfig(new FTPConfiguration());
		FileAndPropertiesDescriptor uploaded = adapterService.upload(instance, fileDescriptor);
		assertNotNull(uploaded);

		verify(ftpClient).transfer(any(InputStream.class), anyString(), any(FTPConfiguration.class));
	}

	@Test(expected = DMSException.class)
	public void testUpload_failTransfer() throws Exception {
		ftpConfig.setEnabled(true);

		FileDescriptor fileDescriptor = mock(FileDescriptor.class);
		Instance instance = new EmfInstance();
		instance.setId("emf:instance");
		instance.add(DefaultProperties.MIMETYPE, "image/jpg");
		ftpConfig.setImageFTPServerConfig(new FTPConfiguration());
		doThrow(IOException.class).when(ftpClient).transfer(any(), anyString(), any());

		adapterService.upload(instance, fileDescriptor);
	}

	@Test
	public void testUpload_async() throws Exception {
		ftpConfig.setEnabled(true);
		ftpConfig.setAsyncThreshold(1L);
		FileDescriptor fileDescriptor = mock(FileDescriptor.class);
		Instance instance = new EmfInstance();
		instance.setId("emf:instance");
		instance.add(DefaultProperties.MIMETYPE, "image/jpg");
		instance.add(CONTENT_LENGTH, 10L);
		ftpConfig.setImageFTPServerConfig(new FTPConfiguration());
		FileAndPropertiesDescriptor uploaded = adapterService.upload(instance, fileDescriptor);
		assertNotNull(uploaded);

		verify(ftpClient).transferAsync(any(InputStream.class), anyString(), any(FTPConfiguration.class));
	}

	@Test
	public void testUpload_invalidData() throws Exception {
		ftpConfig.setEnabled(true);

		FileDescriptor fileDescriptor = mock(FileDescriptor.class);
		Instance instance = new EmfInstance();

		assertNull(adapterService.upload(null, fileDescriptor));
		assertNull(adapterService.upload(instance, fileDescriptor));

		instance.setId("emf:instance");

		assertNull(adapterService.upload(instance, null));
		assertNull(adapterService.upload(instance, fileDescriptor));

	}

	@Test
	public void testUpload_notEnabled() throws Exception {
		ftpConfig.setEnabled(false);

		FileDescriptor fileDescriptor = mock(FileDescriptor.class);
		Instance instance = new EmfInstance();
		instance.setId("emf:instance");
		instance.add(DefaultProperties.MIMETYPE, "image/jpg");
		ftpConfig.setImageFTPServerConfig(new FTPConfiguration());
		FileAndPropertiesDescriptor uploaded = adapterService.upload(instance, fileDescriptor);
		assertNull(uploaded);

		verify(ftpClient, never()).transfer(any(InputStream.class), anyString(), any(FTPConfiguration.class));
	}

	@Test
	public void testUpload_notImage() throws Exception {
		ftpConfig.setEnabled(true);

		FileDescriptor fileDescriptor = mock(FileDescriptor.class);
		Instance instance = new EmfInstance();
		instance.setId("emf:instance");
		instance.add(DefaultProperties.MIMETYPE, "text/html");
		ftpConfig.setImageFTPServerConfig(new FTPConfiguration());
		FileAndPropertiesDescriptor uploaded = adapterService.upload(instance, fileDescriptor);
		assertNull(uploaded);

		verify(ftpClient, never()).transfer(any(InputStream.class), anyString(), any(FTPConfiguration.class));
	}

	@Test
	public void test_getContentAddress_invalidData() throws Exception {
		assertNull(adapterService.getContentUrl(null));
		assertNull(adapterService.getContentUrl(new EmfInstance()));
		Instance instance = new EmfInstance();
		instance.setId("emf:instance");
		assertNull(adapterService.getContentUrl(instance));

		instance.add(DefaultProperties.ATTACHMENT_LOCATION, "instance.jpg");
		assertNull(adapterService.getContentUrl(instance));
	}

	@Test
	public void test_getContentAddress() throws Exception {
		Instance instance = new EmfInstance();
		instance.setId("emf:instance");
		instance.add(DefaultProperties.ATTACHMENT_LOCATION, "emf-instance.jpg");

		ftpConfig.buildConfiguration(null, 0, null, null, "remote");
		ftpConfig.setAccessAddress("localhost");
		assertNotNull(adapterService.getContentUrl(instance));
	}

}
