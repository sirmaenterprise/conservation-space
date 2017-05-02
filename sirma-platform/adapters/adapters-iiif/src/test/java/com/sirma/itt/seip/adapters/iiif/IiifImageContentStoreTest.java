package com.sirma.itt.seip.adapters.iiif;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.net.ftp.FTPClient;
import org.hamcrest.Matcher;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.adapters.ftp.BaseFtpContentStore;
import com.sirma.itt.seip.adapters.ftp.FTPClientImpl;
import com.sirma.itt.seip.adapters.ftp.FtpClientBuilder;
import com.sirma.itt.seip.adapters.remote.DMSClientException;
import com.sirma.itt.seip.adapters.remote.FTPConfiguration;
import com.sirma.itt.seip.adapters.remote.FtpClient;
import com.sirma.itt.seip.adapters.remote.RESTClient;
import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentMetadata;
import com.sirma.itt.seip.content.ContentStore;
import com.sirma.itt.seip.content.StoreException;
import com.sirma.itt.seip.content.StoreItemInfo;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.tasks.DefaultSchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerEntry;
import com.sirma.itt.seip.tasks.SchedulerEntryStatus;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.srima.itt.seip.adapters.mock.ImageServerConfigurationsMock;

/**
 * Tests for {@link IiifImageContentStore}.
 *
 * @author BBonev
 */
public class IiifImageContentStoreTest {

	private static final String USER = "user";

	@InjectMocks
	private IiifImageContentStore contentStore;

	@Spy
	private ImageServerConfigurationsMock imageServerConfigurations = new ImageServerConfigurationsMock();

	@Mock
	private RESTClient restClientMock;

	@Spy
	private InstanceProxyMock<RESTClient> restClient = new InstanceProxyMock<>(null);

	@Spy
	private FtpClientBuilder clientBuilder;

	@InjectMocks
	private FTPClientImpl ftpClientInstance;

	@Mock
	private SchedulerService schedulerService;

	@Mock
	private ContentStore localStore;

	@Spy
	private InstanceProxyMock<FtpClient> ftpClient = new InstanceProxyMock<>(null);

	private static FakeFtpServer ftpServer;

	@BeforeClass
	public static void setupFtpServer() {
		ftpServer = new FakeFtpServer();

		FileSystem fileSystem = new UnixFakeFileSystem();
		fileSystem.add(new DirectoryEntry("/"));
		fileSystem.add(new DirectoryEntry("/store/"));
		ftpServer.setFileSystem(fileSystem);

		UserAccount userAccount = new UserAccount(USER, USER, "/");
		ftpServer.addUserAccount(userAccount);

		ftpServer.setServerControlPort(0);
		ftpServer.start();
	}

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		restClient.set(restClientMock);
		ftpClient.set(ftpClientInstance);

		FTPConfiguration config = new FTPConfiguration();
		config.setUsername(USER);
		config.setPassword(USER);
		config.setHostname("localhost");
		config.setSecured(false);
		config.setRemoteDir("/store/");
		config.setPort(ftpServer.getServerControlPort());

		imageServerConfigurations.setImageFTPServerConfig(config);
		imageServerConfigurations.setAsyncThreshold(-1L);

		when(schedulerService.buildEmptyConfiguration(SchedulerEntryType.IMMEDIATE))
				.thenReturn(new DefaultSchedulerConfiguration());
		when(localStore.add(any(), any())).thenReturn(new StoreItemInfo());
	}

	@Test
	public void addFileToStoreOverFtp() throws Exception {
		Content descriptor = Content.createEmpty().setName("test.txt").setContent("testData",
				StandardCharsets.UTF_8.name());
		StoreItemInfo itemInfo = contentStore.add(null, descriptor);
		assertNotNull(itemInfo);
		assertTrue(itemInfo.getRemoteId().endsWith("txt"));

		assertTrue(ftpServer.getFileSystem().exists(
				imageServerConfigurations.getImageFTPServerConfig().get().getRemoteDir() + itemInfo.getRemoteId()));
	}

	@Test
	public void addFileToStoreOverFtp_async() throws Exception {
		Content descriptor = Content.createEmpty().setName("test.txt").setContent("testData",
				StandardCharsets.UTF_8.name());
		imageServerConfigurations.setAsyncThreshold(1L);
		StoreItemInfo itemInfo = contentStore.add(null, descriptor);
		assertNotNull(itemInfo);
		assertTrue(itemInfo.getRemoteId().endsWith("txt"));
		assertNotNull(itemInfo.getAdditionalData());
		assertTrue(itemInfo.getAdditionalData() instanceof Map<?, ?>);

		verify(schedulerService).schedule(anyString(), any(), any());
		assertFalse(ftpServer.getFileSystem().exists(
				imageServerConfigurations.getImageFTPServerConfig().get().getRemoteDir() + itemInfo.getRemoteId()));
	}

	@Test(expected = StoreException.class)
	public void addFileToStoreOverFtp_withError() throws Exception {
		Content descriptor = Content.createEmpty().setName("test.txt").setContent("testData",
				StandardCharsets.UTF_8.name());

		FTPClient mockClient = mock(FTPClient.class);
		doThrow(IOException.class).when(mockClient).storeFile(anyString(), any(InputStream.class));
		when(clientBuilder.buildClient(imageServerConfigurations.getImageFTPServerConfig().get()))
				.thenReturn(mockClient);

		contentStore.add(null, descriptor);
	}

	@Test(expected = StoreException.class)
	public void addFileToStoreOverFtp_withError_and_close() throws Exception {
		InputStream inputStream = mock(InputStream.class);
		doThrow(IOException.class).when(inputStream).close();
		Content descriptor = Content
				.createEmpty()
					.setName("test.txt")
					.setContent(FileDescriptor.create("test", () -> inputStream, 4L));

		FTPClient mockClient = mock(FTPClient.class);
		doThrow(IOException.class).when(mockClient).storeFile(anyString(), any(InputStream.class));
		when(clientBuilder.buildClient(imageServerConfigurations.getImageFTPServerConfig().get()))
				.thenReturn(mockClient);

		contentStore.add(null, descriptor);
	}

	@Test
	public void updateFileOverFtp() throws Exception {
		Content descriptor = Content.createEmpty().setName("test.txt").setContent("testData",
				StandardCharsets.UTF_8.name());
		StoreItemInfo itemInfo = contentStore.add(null, descriptor);
		assertNotNull(itemInfo);
		assertTrue(itemInfo.getRemoteId().endsWith("txt"));

		assertTrue(ftpServer.getFileSystem().exists(
				imageServerConfigurations.getImageFTPServerConfig().get().getRemoteDir() + itemInfo.getRemoteId()));

		Content newData = Content
				.createEmpty()
					.setContent("updatedData", StandardCharsets.UTF_8.name())
					.setName("test.txt");

		StoreItemInfo updatedInfo = contentStore.update(null, newData, itemInfo);

		assertNotNull(updatedInfo);
		assertTrue(ftpServer.getFileSystem().exists(
				imageServerConfigurations.getImageFTPServerConfig().get().getRemoteDir() + updatedInfo.getRemoteId()));
	}

	@Test
	public void updateFileOverFtp_async() throws Exception {
		Content descriptor = Content.createEmpty().setName("test.txt").setContent("testData",
				StandardCharsets.UTF_8.name());
		StoreItemInfo itemInfo = contentStore.add(null, descriptor);
		assertNotNull(itemInfo);
		assertTrue(itemInfo.getRemoteId().endsWith("txt"));

		assertTrue(ftpServer.getFileSystem().exists(
				imageServerConfigurations.getImageFTPServerConfig().get().getRemoteDir() + itemInfo.getRemoteId()));

		imageServerConfigurations.setAsyncThreshold(1L);

		Content newData = Content
				.createEmpty()
					.setContent("updatedData", StandardCharsets.UTF_8.name())
					.setName("test.txt");

		itemInfo.setAdditionalData(
				(Serializable) Collections.singletonMap(BaseFtpContentStore.ASYNC_REQUEST_ID, "asyncId"));
		SchedulerEntry entry = new SchedulerEntry();
		entry.setStatus(SchedulerEntryStatus.COMPLETED);
		when(schedulerService.getScheduleEntry("asyncId")).thenReturn(entry);

		StoreItemInfo updatedInfo = contentStore.update(null, newData, itemInfo);

		assertNotNull(updatedInfo);
		assertNotNull(updatedInfo.getAdditionalData());
		verify(schedulerService, never()).save(any());
	}

	@Test
	public void updateFileOverFtp_cancelPreviousAsync() throws Exception {
		Content descriptor = Content.createEmpty().setName("test.txt").setContent("testData",
				StandardCharsets.UTF_8.name());
		StoreItemInfo itemInfo = contentStore.add(null, descriptor);
		assertNotNull(itemInfo);
		assertTrue(itemInfo.getRemoteId().endsWith("txt"));

		assertTrue(ftpServer.getFileSystem().exists(
				imageServerConfigurations.getImageFTPServerConfig().get().getRemoteDir() + itemInfo.getRemoteId()));

		imageServerConfigurations.setAsyncThreshold(1L);

		Content newData = Content
				.createEmpty()
					.setContent("updatedData", StandardCharsets.UTF_8.name())
					.setName("test.txt");

		itemInfo.setAdditionalData(
				(Serializable) Collections.singletonMap(BaseFtpContentStore.ASYNC_REQUEST_ID, "asyncId"));
		SchedulerEntry entry = new SchedulerEntry();
		entry.setStatus(SchedulerEntryStatus.RUNNING);
		when(schedulerService.getScheduleEntry("asyncId")).thenReturn(entry);

		StoreItemInfo updatedInfo = contentStore.update(null, newData, itemInfo);

		assertNotNull(updatedInfo);
		assertNotNull(updatedInfo.getAdditionalData());
		verify(schedulerService).save(any());
	}

	@Test(expected = StoreException.class)
	public void updateFileOverFtp_withError() throws Exception {
		Content descriptor = Content.createEmpty().setName("test.txt").setContent("testData",
				StandardCharsets.UTF_8.name());
		StoreItemInfo itemInfo = contentStore.add(null, descriptor);
		assertNotNull(itemInfo);
		assertTrue(itemInfo.getRemoteId().endsWith("txt"));

		assertTrue(ftpServer.getFileSystem().exists(
				imageServerConfigurations.getImageFTPServerConfig().get().getRemoteDir() + itemInfo.getRemoteId()));

		Content newData = Content
				.createEmpty()
					.setContent("updatedData", StandardCharsets.UTF_8.name())
					.setName("test.txt");

		FTPClient mockClient = mock(FTPClient.class);
		doThrow(IOException.class).when(mockClient).storeFile(anyString(), any(InputStream.class));
		when(clientBuilder.buildClient(imageServerConfigurations.getImageFTPServerConfig().get()))
				.thenReturn(mockClient);

		contentStore.update(null, newData, itemInfo);
	}

	@Test(expected = StoreException.class)
	public void updateFileOverFtp_withError_endClose() throws Exception {
		InputStream inputStream = mock(InputStream.class);
		doThrow(IOException.class).when(inputStream).close();
		Content descriptor = Content
				.createEmpty()
					.setName("test.txt")
					.setContent(FileDescriptor.create("test", () -> inputStream, 4L));

		FTPClient mockClient = mock(FTPClient.class);
		doThrow(IOException.class).when(mockClient).storeFile(anyString(), any(InputStream.class));
		when(clientBuilder.buildClient(imageServerConfigurations.getImageFTPServerConfig().get()))
				.thenReturn(mockClient);

		StoreItemInfo itemInfo = new StoreItemInfo()
				.setProviderType(IiifImageContentStore.STORE_NAME)
					.setRemoteId("someId");

		contentStore.update(null, descriptor, itemInfo);
	}

	@Test
	public void deleteFileOverFtp() throws Exception {
		Content descriptor = Content.createEmpty().setName("test.txt").setContent("testData",
				StandardCharsets.UTF_8.name());
		StoreItemInfo itemInfo = contentStore.add(null, descriptor);
		assertNotNull(itemInfo);
		assertTrue(itemInfo.getRemoteId().endsWith("txt"));

		assertTrue(ftpServer.getFileSystem().exists(
				imageServerConfigurations.getImageFTPServerConfig().get().getRemoteDir() + itemInfo.getRemoteId()));

		assertTrue(contentStore.delete(itemInfo));
		assertFalse(ftpServer.getFileSystem().exists(
				imageServerConfigurations.getImageFTPServerConfig().get().getRemoteDir() + itemInfo.getRemoteId()));
	}

	@Test
	public void getReadChannel() throws Exception {
		mockValidRemoteContent();
		imageServerConfigurations.setAccessAddress("localhost/store/");

		StoreItemInfo storeInfo = new StoreItemInfo()
				.setProviderType(IiifImageContentStore.STORE_NAME)
					.setRemoteId("test.txt");
		FileDescriptor readChannel = contentStore.getReadChannel(storeInfo);
		assertNotNull(readChannel);
		assertEquals("localhost/store/test.txt", readChannel.getId());
	}

	@Test
	public void getReadChannel_nonExistingContent() throws Exception {
		when(restClientMock.rawRequest(any(GetMethod.class), any(URI.class))).then(a -> {
			HttpMethod method = mock(HttpMethod.class);
			when(method.getResponseBodyAsStream()).thenThrow(IOException.class);
			return method;
		});

		imageServerConfigurations.setAccessAddress("localhost/store/");

		StoreItemInfo storeInfo = new StoreItemInfo()
				.setProviderType(IiifImageContentStore.STORE_NAME)
					.setRemoteId("test.txt");
		FileDescriptor readChannel = contentStore.getReadChannel(storeInfo);
		assertNotNull(readChannel);
	}

	@Test
	public void getReadChannelWhenTheAddressDoesntEndWithSlash() throws Exception {
		mockValidRemoteContent();
		imageServerConfigurations.setAccessAddress("localhost/store");

		StoreItemInfo storeInfo = new StoreItemInfo()
				.setProviderType(IiifImageContentStore.STORE_NAME)
					.setRemoteId("test.txt");
		FileDescriptor readChannel = contentStore.getReadChannel(storeInfo);
		assertNotNull(readChannel);
		assertEquals("localhost/store/test.txt", readChannel.getId());
	}

	private void mockValidRemoteContent() throws DMSClientException {
		String metaDataResponse = "{\"height\":10, \"width\":10 }";
		when(restClientMock.rawRequest(any(GetMethod.class), any(URI.class))).then(a -> {
			HttpMethod method = mock(HttpMethod.class);
			when(method.getResponseBodyAsStream())
					.thenReturn(new ByteArrayInputStream(metaDataResponse.getBytes(StandardCharsets.UTF_8)));
			return method;
		});
	}

	@Test
	public void testInvalidData() throws Exception {
		contentStore.add(null, null);
		contentStore.update(null, null, null);
		contentStore.update(null, mock(Content.class), null);
		contentStore.update(null, mock(Content.class), new StoreItemInfo());
		contentStore.delete(null);
		contentStore.delete(new StoreItemInfo());
		contentStore.getReadChannel(null);
		contentStore.getReadChannel(new StoreItemInfo());
		contentStore.getMetadata(null);
		contentStore.getMetadata(new StoreItemInfo());
	}

	@Test
	public void getMetadata() throws Exception {
		imageServerConfigurations.setIiifServerAddress("localhost");
		StoreItemInfo itemInfo = new StoreItemInfo("remoteId", contentStore.getName(), 10, "text/plain", null);
		String metaDataResponse = "{\"height\":10, \"width\":10 }";

		Matcher<URI> getValidResponse = CustomMatcher.of((URI uri) -> {
			return uri != null && uri.toString().contains("remoteId");
		});
		when(restClientMock.rawRequest(any(GetMethod.class), argThat(getValidResponse))).then(a -> {
			HttpMethod method = mock(HttpMethod.class);
			when(method.getResponseBodyAsStream())
					.thenReturn(new ByteArrayInputStream(metaDataResponse.getBytes(StandardCharsets.UTF_8)));
			return method;
		});

		Matcher<URI> getInvalidResponse = CustomMatcher.of((URI uri) -> {
			return uri != null && uri.toString().contains("someId");
		});
		when(restClientMock.rawRequest(any(GetMethod.class), argThat(getInvalidResponse))).thenThrow(IOException.class);

		ContentMetadata metadata = contentStore.getMetadata(itemInfo);

		assertNotNull(metadata);
		assertEquals(10, metadata.getInt("height"));
		assertEquals(10, metadata.getInt("width"));
		assertEquals("remoteId", metadata.getString("id"));

		// call with fresh copy so verify that the data will be fetched from the cached value
		StoreItemInfo storeItemInfo = new StoreItemInfo(itemInfo.getRemoteId(), itemInfo.getProviderType(),
				itemInfo.getContentLength(), itemInfo.getContentType(), itemInfo.getAdditionalData());
		metadata = contentStore.getMetadata(storeItemInfo);

		assertNotNull(metadata);
		assertEquals(10, metadata.getInt("height"));
		assertEquals(10, metadata.getInt("width"));
		assertEquals("remoteId", metadata.getString("id"));

		verify(restClientMock).rawRequest(any(HttpMethod.class), any(URI.class));
	}

	@Test
	public void getMetadata_DefaultImage() throws Exception {
		imageServerConfigurations.setIiifServerAddress("localhost");
		StoreItemInfo itemInfo = new StoreItemInfo("remoteId", contentStore.getName(), 10, "text/plain", null);
		String metaDataResponse = "{\"height\":10, \"width\":10 }";

		Matcher<URI> getValidResponse = CustomMatcher.of((URI uri) -> {
			return uri != null && uri.toString().contains("defaultImage");
		});
		when(restClientMock.rawRequest(any(GetMethod.class), argThat(getValidResponse))).then(a -> {
			HttpMethod method = mock(HttpMethod.class);
			when(method.getResponseBodyAsStream())
					.thenReturn(new ByteArrayInputStream(metaDataResponse.getBytes(StandardCharsets.UTF_8)));
			return method;
		});

		Matcher<URI> getInvalidResponse = CustomMatcher.of((URI uri) -> {
			return uri != null && uri.toString().contains("remoteId");
		});
		when(restClientMock.rawRequest(any(GetMethod.class), argThat(getInvalidResponse))).thenThrow(IOException.class);

		ContentMetadata metadata = contentStore.getMetadata(itemInfo);

		assertNotNull(metadata);
		assertEquals(10, metadata.getInt("height"));
		assertEquals(10, metadata.getInt("width"));
		assertEquals("defaultImage", metadata.getString("id"));

		// should call the remote service again because the first time failed and get the information about the default
		// image
		StoreItemInfo storeItemInfo = new StoreItemInfo(itemInfo.getRemoteId(), itemInfo.getProviderType(),
				itemInfo.getContentLength(), itemInfo.getContentType(), itemInfo.getAdditionalData());
		metadata = contentStore.getMetadata(storeItemInfo);

		assertNotNull(metadata);
		assertEquals(10, metadata.getInt("height"));
		assertEquals(10, metadata.getInt("width"));
		assertEquals("defaultImage", metadata.getString("id"));

		verify(restClientMock, times(4)).rawRequest(any(HttpMethod.class), any(URI.class));
	}

	@AfterClass
	public static void cleanUp() {
		ftpServer.stop();
	}
}
