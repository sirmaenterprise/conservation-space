package com.sirma.itt.seip.adapters.iiif;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;
import org.hamcrest.Matcher;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.FileSystemEntry;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.adapters.ftp.BaseFtpContentStore;
import com.sirma.itt.seip.adapters.ftp.FTPClientImpl;
import com.sirma.itt.seip.adapters.ftp.FtpClientBuilder;
import com.sirma.itt.seip.adapters.iip.IIPServerImageProvider;
import com.sirma.itt.seip.adapters.remote.FTPConfiguration;
import com.sirma.itt.seip.adapters.remote.FtpClient;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.rest.client.HTTPClient;
import com.sirma.itt.seip.tasks.SchedulerEntry;
import com.sirma.itt.seip.tasks.SchedulerEntryStatus;
import com.sirma.itt.seip.testutil.CustomMatcher;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentMetadata;
import com.sirma.sep.content.ContentStore;
import com.sirma.sep.content.ContentStoreMissMatchException;
import com.sirma.sep.content.DeleteContentData;
import com.sirma.sep.content.StoreException;
import com.sirma.sep.content.StoreItemInfo;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.api.SenderService;

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
	private HTTPClient httpClient;

	@Spy
	private FtpClientBuilder clientBuilder;

	@InjectMocks
	private FTPClientImpl ftpClientInstance;

	@Mock
	private SenderService senderService;

	@Mock
	private ContentStore localStore;

	@Spy
	private InstanceProxyMock<FtpClient> ftpClient = new InstanceProxyMock<>(null);

	@Mock
	private IIPServerImageProvider iipServerImageProvider;

	private static FakeFtpServer ftpServer;

	@BeforeClass
	public static void setupFtpServer() {
		ftpServer = new FakeFtpServer();

		FileSystem fileSystem = new UnixFakeFileSystem();
		fileSystem.add(new DirectoryEntry("/"));
		fileSystem.add(new DirectoryEntry("/store/"));
		fileSystem.add(new DirectoryEntry("/output/"));
		ftpServer.setFileSystem(fileSystem);

		UserAccount userAccount = new UserAccount(USER, USER, "/");
		ftpServer.addUserAccount(userAccount);

		ftpServer.setServerControlPort(0);
		ftpServer.start();
	}

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		ftpClient.set(ftpClientInstance);
		ReflectionUtils.setFieldValue(ftpClientInstance, "senderService", senderService);

		FTPConfiguration config = new FTPConfiguration();
		config.setUsername(USER);
		config.setPassword(USER);
		config.setHostname("localhost");
		config.setSecured(false);
		config.setRemoteDir("/store/");
		config.setPort(ftpServer.getServerControlPort());

		imageServerConfigurations.setImageFTPServerConfig(config);
		imageServerConfigurations.setAsyncThreshold(-1L);

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

		verify(senderService).send(anyString(), anyMap(), any(SendOptions.class));
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

		StoreItemInfo updatedInfo = contentStore.update(null, newData, itemInfo);

		assertNotNull(updatedInfo);
		assertNotNull(updatedInfo.getAdditionalData());
		verify(senderService, never()).send(anyString(), any(), any());
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

		StoreItemInfo updatedInfo = contentStore.update(null, newData, itemInfo);

		assertNotNull(updatedInfo);
		assertNotNull(updatedInfo.getAdditionalData());
		verify(senderService).send(anyString(), anyMap(), any(SendOptions.class));
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
	public void deleteFileInTwoSteps() throws Exception {
		Content descriptor = Content.createEmpty().setName("test.txt").setContent("testData",
				StandardCharsets.UTF_8.name());
		StoreItemInfo itemInfo = contentStore.add(null, descriptor);
		assertNotNull(itemInfo);
		assertTrue(itemInfo.getRemoteId().endsWith("txt"));

		imageServerConfigurations.setAccessAddress("localhost/output");

		// move the file to other directory after upload to simulate the actual behaviour of the application
		// as the uploaded files are moved to other directory after processing them
		String uploadedFileLocation = imageServerConfigurations.getImageFTPServerConfig().get().getRemoteDir() + itemInfo.getRemoteId();
		String downloadLocation = "/output/" + itemInfo.getRemoteId();
		assertTrue(ftpServer.getFileSystem().exists(uploadedFileLocation));
		FileSystemEntry entry = ftpServer.getFileSystem().getEntry(uploadedFileLocation);
		ftpServer.getFileSystem().add(entry.cloneWithNewPath(downloadLocation));
		assertTrue("The file should be present at it's new location", ftpServer.getFileSystem().exists(uploadedFileLocation));
		ftpServer.getFileSystem().delete(uploadedFileLocation);

		Optional<DeleteContentData> contentData = contentStore.prepareForDelete(itemInfo);
		assertTrue(contentData.isPresent());
		contentStore.delete(contentData.get());

		assertFalse(ftpServer.getFileSystem().exists(uploadedFileLocation));
		assertFalse("The moved file should be deleted", ftpServer.getFileSystem().exists(downloadLocation));
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
		when(httpClient.execute(any(URI.class), any(ResponseHandler.class))).thenThrow(IOException.class);

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

	@Test
	public void getPreviewChannel() throws Exception {
		when(iipServerImageProvider.getImageUrl(any(String.class), any(Dimension.class), anyBoolean()))
				.thenReturn("localhost/iip-server?IIIF=emf:dummy/full/full/0/default.jpg");

		imageServerConfigurations.setIiifServerAddress("localhost/iip-server?IIIF=");

		StoreItemInfo storeInfo = new StoreItemInfo().setProviderType(IiifImageContentStore.STORE_NAME)
				.setRemoteId("dummy.png");
		FileDescriptor readChannel = contentStore.getPreviewChannel(storeInfo);
		assertNotNull(readChannel);
		assertEquals("localhost/iip-server?IIIF=emf:dummy/full/full/0/default.jpg", readChannel.getId());
	}

	@Test
	public void should_UseRealImageContentDimension_When_RealWidthIsLesThanConfigurationOne() {
		verifyWightAndHeight(0, 44, 0, 44);
	}

	@Test
	public void should_ConfigurationWightAndCalculatedHeight_When_RealWidthIsBiggerThanConfigurationOne() {
		verifyWightAndHeight(3840, 4521, 1920, 2260);
	}

	private void verifyWightAndHeight(int imageWidth, int imageHeight, int expectedWidth, int expectedHeight) {
		when(iipServerImageProvider.getImageUrl(any(String.class), any(Dimension.class), anyBoolean())).thenReturn(
				"localhost/iip-server?IIIF=emf:dummy/full/full/0/default.jpg");
		imageServerConfigurations.setIiifServerAddress("localhost/iip-server?IIIF=");

		Map<String, Serializable> additionalData = new HashMap<>();
		additionalData.put("width", imageWidth);
		additionalData.put("height", imageHeight);

		Map<String, Map<String, Serializable>> imageMetadata = new HashMap<>();
		imageMetadata.put("imageMetadata", additionalData);
		StoreItemInfo storeInfo = new StoreItemInfo().setProviderType(IiifImageContentStore.STORE_NAME)
				.setRemoteId("dummy.png").setAdditionalData((Serializable) imageMetadata);
		FileDescriptor readChannel = contentStore.getPreviewChannel(storeInfo);

		verify(iipServerImageProvider).getImageUrl(eq(null), argThat(matchesWidthAndHeight(expectedWidth, expectedHeight)), eq(true));
	}

	private CustomMatcher<Dimension<Integer>> matchesWidthAndHeight(int wight, int height) {
		return CustomMatcher.of(invocation -> {
			assertEquals(wight, (int) invocation.getWidth());
			assertEquals(height, (int) invocation.getHeight());
		});
	}

	private void mockValidRemoteContent() {
		String metaDataResponse = "{\"height\":10, \"width\":10 }";
		when(httpClient.execute(any(URI.class), any(ResponseHandler.class)))
				.thenReturn(new ByteArrayInputStream(metaDataResponse.getBytes(StandardCharsets.UTF_8)));
	}

	@Test
	public void testInvalidData() throws Exception {
		contentStore.add(null, null);
		contentStore.getMetadata(null);
		contentStore.getMetadata(new StoreItemInfo());
	}


	@Test(expected = ContentStoreMissMatchException.class)
	public void update_ShouldFailForNullPreviousInfo() {
		contentStore.update(null, null, null);
	}

	@Test(expected = ContentStoreMissMatchException.class)
	public void update_ShouldFailForMissMatchPreviousInfo() {
		contentStore.update(null, null, new StoreItemInfo().setProviderType("random"));
	}

	@Test(expected = ContentStoreMissMatchException.class)
	public void delete_ShouldFailForNullPreviousInfo() {
		contentStore.delete((StoreItemInfo) null);
	}

	@Test(expected = ContentStoreMissMatchException.class)
	public void delete_ShouldFailForMissMatchPreviousInfo() {
		contentStore.delete(new StoreItemInfo().setProviderType("random"));
	}

	@Test(expected = ContentStoreMissMatchException.class)
	public void getReadChannel_ShouldFailForNullPreviousInfo() {
		contentStore.getReadChannel(null);
	}

	@Test(expected = ContentStoreMissMatchException.class)
	public void getReadChannel_ShouldFailForMissMatchPreviousInfo() {
		contentStore.getReadChannel(new StoreItemInfo().setProviderType("random"));
	}

	@Test(expected = ContentStoreMissMatchException.class)
	public void prepareForDelete_ShouldFailForNullPreviousInfo() {
		contentStore.prepareForDelete(null);
	}

	@Test(expected = ContentStoreMissMatchException.class)
	public void prepareForDelete_ShouldFailForMissMatchPreviousInfo() {
		contentStore.prepareForDelete(new StoreItemInfo().setProviderType("random"));
	}

	@Test(expected = ContentStoreMissMatchException.class)
	public void deletePreparedData_ShouldFailForNullPreviousInfo() {
		contentStore.delete((DeleteContentData) null);
	}

	@Test(expected = ContentStoreMissMatchException.class)
	public void deletePreparedData_ShouldFailForMissMatchPreviousInfo() {
		contentStore.delete(new DeleteContentData().setStoreName("random"));
	}

	@Test
	public void getMetadata() throws Exception {
		imageServerConfigurations.setIiifServerAddress("localhost");
		StoreItemInfo itemInfo = new StoreItemInfo("remoteId", contentStore.getName(), 10, "text/plain", null);
		String metaDataResponse = "{\"height\":10, \"width\":10 }";

		Matcher<URI> getValidResponse = CustomMatcher
				.of((URI uri) -> uri != null && uri.toString().contains("remoteId"));
		mockHttpClientResponse(metaDataResponse, getValidResponse);

		Matcher<URI> getInvalidResponse = CustomMatcher
				.of((URI uri) -> uri != null && uri.toString().contains("someId"));
		when(httpClient.execute(argThat(getInvalidResponse), any(ResponseHandler.class)))
				.thenThrow(IOException.class);

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

		verify(httpClient).execute(any(URI.class), any(ResponseHandler.class));
	}

	@Test
	public void getMetadata_DefaultImage() throws Exception {
		imageServerConfigurations.setIiifServerAddress("localhost");
		StoreItemInfo itemInfo = new StoreItemInfo("defaultImage", contentStore.getName(), 10, "text/plain", null);
		String metaDataResponse = "{\"height\":10, \"width\":10 }";

		Matcher<URI> getValidResponse = CustomMatcher
				.of((URI uri) -> uri != null && uri.toString().contains("defaultImage"));
		mockHttpClientResponse(metaDataResponse, getValidResponse);

		Matcher<URI> getInvalidResponse = CustomMatcher
				.of((URI uri) -> uri != null && uri.toString().contains("remoteId"));
		doThrow(IOException.class).when(httpClient).execute(argThat(getInvalidResponse), any(ResponseHandler.class));

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

		verify(httpClient).execute(any(URI.class), any(ResponseHandler.class));
	}

	private void mockHttpClientResponse(String metaDataResponse, Matcher<URI> getValidResponse) {
		doAnswer(invocation -> {
			ResponseHandler responseReader = invocation.getArgumentAt(1, ResponseHandler.class);
			HttpResponse httpResponse = mock(HttpResponse.class);
			HttpEntity httpEntity = mock(HttpEntity.class);
			StatusLine statusLine = mock(StatusLine.class);
			when(statusLine.getStatusCode()).thenReturn(200);
			when(httpResponse.getStatusLine()).thenReturn(statusLine);
			when(httpEntity.getContent())
					.thenReturn(new ByteArrayInputStream(metaDataResponse.getBytes(StandardCharsets.UTF_8)));
			when(httpResponse.getEntity()).thenReturn(httpEntity);
			return responseReader.handleResponse(httpResponse);
		}).when(httpClient).execute(argThat(getValidResponse), any(ResponseHandler.class));
	}

	@AfterClass
	public static void cleanUp() {
		ftpServer.stop();
	}
}
