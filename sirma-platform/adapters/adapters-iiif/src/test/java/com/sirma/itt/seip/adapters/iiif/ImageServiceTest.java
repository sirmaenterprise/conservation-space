package com.sirma.itt.seip.adapters.iiif;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.seip.adapters.ftp.FTPClientImpl;
import com.sirma.itt.seip.adapters.ftp.FtpClientBuilder;
import com.sirma.itt.seip.adapters.remote.FTPConfiguration;
import com.sirma.itt.seip.adapters.remote.FtpClient;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.io.FileAndPropertiesDescriptor;
import com.srima.itt.seip.adapters.mock.FileDescriptorMock;
import com.srima.itt.seip.adapters.mock.ImageServerConfigurationsMock;

/**
 * Tests the functionalities of the image service.
 *
 * @author Nikoaly Ch
 */
@RunWith(MockitoJUnitRunner.class)
public class ImageServiceTest {

	private static final String USER = "user";

	@InjectMocks
	private ImageAdapterServiceImpl imageAdapter;

	@Spy
	FtpClient ftpClient = new FTPClientImpl(new FtpClientBuilder(), null, null);

	@Spy
	private ImageServerConfigurationsMock ftpConfigMock;

	private static FakeFtpServer ftpServer;

	@BeforeClass
	public static void init() {
		ftpServer = new FakeFtpServer();

		FileSystem fileSystem = new UnixFakeFileSystem();
		fileSystem.add(new DirectoryEntry("/"));
		fileSystem.add(new DirectoryEntry("/images/"));
		ftpServer.setFileSystem(fileSystem);

		UserAccount userAccount = new UserAccount(USER, USER, "/");
		ftpServer.addUserAccount(userAccount);

		ftpServer.setServerControlPort(0);
		ftpServer.start();
	}

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		ftpConfigMock.buildConfiguration("localhost", ftpServer.getServerControlPort(), USER, USER, "/images/");
		ftpConfigMock.setAccessAddress("http://localhost:8008");
	}

	/**
	 * Tests if the data is properly proceeded by the image service.
	 *
	 * @throws DMSException
	 *             when an error occurs
	 * @throws IOException
	 */
	@Test
	public void testWithCorrectData() throws DMSException, IOException {
		InputStream stream = new ByteArrayInputStream("testData".getBytes(StandardCharsets.UTF_8));
		FileDescriptorMock fileDescriptor = new FileDescriptorMock();
		fileDescriptor.setInputStream(stream);
		EmfInstance instance = new EmfInstance();
		Map<String, Serializable> properties = new HashMap<>();
		properties.put(DefaultProperties.MIMETYPE, "image/png");
		instance.setProperties(properties);
		instance.setId("test");
		FileAndPropertiesDescriptor propertyDescriptor = imageAdapter.upload(instance, fileDescriptor);
		String newName = "test.png";
		assertEquals(newName, propertyDescriptor.getProperties().get(DefaultProperties.ATTACHMENT_LOCATION));
	}

	/**
	 * Tests if the service build the uri correctly.
	 */
	@Test
	public void testProvidedURI() {
		EmfInstance instance = new EmfInstance();
		instance.setId("test");
		Map<String, Serializable> properties = new HashMap<>(2);
		properties.put(DefaultProperties.ATTACHMENT_LOCATION, "test.png");
		instance.setProperties(properties);
		String providedURI = imageAdapter.getContentUrl(instance);
		FTPConfiguration ftpConfig = ftpConfigMock.getImageFTPServerConfig().get();
		String uri = ftpConfigMock.getImageAccessServerAddress().get() + ftpConfig.getRemoteDir()
				+ "test.png";
		assertEquals(providedURI, uri);
	}

	@AfterClass
	public static void cleanUp() {
		ftpServer.stop();
	}
}
