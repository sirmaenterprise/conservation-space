package com.sirma.itt.seip.adapters.ftp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.net.ftp.FTPClient;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.seip.adapters.remote.FTPConfiguration;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.monitor.NoOpStatistics;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentStore;
import com.sirma.sep.content.ContentStoreProvider;
import com.sirma.sep.content.StoreItemInfo;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.api.SenderService;

/**
 * Class for testing the connection and the upload of a file to a ftp server using the
 * FTPClientImpl.
 */
@RunWith(MockitoJUnitRunner.class)
public class FTPClientImplTest {

	private static final String USER = "user";
	private static FakeFtpServer ftpServer;
	private FTPConfiguration config;

	@InjectMocks
	private FTPClientImpl ftpClient;

	@Spy
	private FtpClientBuilder clientBuilder;

	@Mock
	private ContentStore localStore;

	@Mock
	private SenderService senderService;

	@Mock
	private ContentStore contentStore;

	@Mock
	private ContentStoreProvider storeProvider;

	@Spy
	private Statistics statistics = NoOpStatistics.INSTANCE;

	@BeforeClass
	public static void init() {
		ftpServer = new FakeFtpServer();

		FileSystem fileSystem = new UnixFakeFileSystem();
		fileSystem.add(new DirectoryEntry("/"));
		ftpServer.setFileSystem(fileSystem);

		UserAccount userAccount = new UserAccount(USER, USER, "/");
		ftpServer.addUserAccount(userAccount);

		ftpServer.setServerControlPort(0);
		ftpServer.start();
	}

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);

		config = new FTPConfiguration();
		config.setUsername(USER);
		config.setPassword(USER);
		config.setHostname("localhost");
		config.setSecured(false);
		config.setRemoteDir("/images/");

		config.setPort(ftpServer.getServerControlPort());
		when(storeProvider.getStore(any(StoreItemInfo.class))).thenReturn(contentStore);
	}

	/**
	 * Tests the sending of a file with not secured connection.
	 */
	@Test
	public void testProperlySentData() throws Exception {
		InputStream stream = new ByteArrayInputStream("testData".getBytes(StandardCharsets.UTF_8));

		ftpClient.transfer(stream, "transferred.txt", config);

		assertTrue(ftpServer.getFileSystem().isFile("/images/transferred.txt"));
		assertFalse(ftpServer.getFileSystem().isFile("/images/nottransferred.txt"));
	}

	/**
	 * Tests the sending of a file with not secured connection.
	 */
	@Test
	public void testDelete() throws Exception {
		InputStream stream = new ByteArrayInputStream("testData".getBytes(StandardCharsets.UTF_8));

		ftpClient.transfer(stream, "transferred.txt", config);

		assertTrue(ftpServer.getFileSystem().isFile("/images/transferred.txt"));
		assertTrue(ftpClient.delete("transferred.txt", config));
		assertFalse(ftpServer.getFileSystem().isFile("/images/transferred.txt"));
	}

	@Test(expected = DMSException.class)
	public void invalidUser() throws Exception {
		config.setUsername("test");

		InputStream stream = new ByteArrayInputStream("testData".getBytes(StandardCharsets.UTF_8));

		ftpClient.transfer(stream, "transferred.txt", config);
	}

	@Test(expected = DMSException.class)
	public void invalid_data() throws Exception {
		config.setUsername("test");

		InputStream stream = getClass().getResourceAsStream(getClass().getSimpleName() + ".class");
		// force IO exception
		stream.close();

		ftpClient.transfer(stream, "transferred.txt", config);
	}

	@Test(expected = DMSException.class)
	public void notAuthenticated() throws Exception {
		config.setUsername(null);

		InputStream stream = new ByteArrayInputStream("testData".getBytes(StandardCharsets.UTF_8));

		ftpClient.transfer(stream, "transferred.txt", config);
	}

	@Test(expected = DMSException.class)
	public void errorCommunication_send() throws Exception {
		FTPClient client = mock(FTPClient.class);
		when(clientBuilder.buildClient(config)).thenReturn(client);
		when(client.storeFile(anyString(), any(InputStream.class))).thenThrow(IOException.class);

		try {
			InputStream stream = new ByteArrayInputStream("testData".getBytes(StandardCharsets.UTF_8));
			ftpClient.transfer(stream, "transferred.txt", config);
		} finally {
			verify(clientBuilder).closeClient(client);
		}
	}

	@Test(expected = DMSException.class)
	public void errorCommunication_send_setType() throws Exception {
		FTPClient client = mock(FTPClient.class);
		when(clientBuilder.buildClient(config)).thenReturn(client);
		doThrow(IOException.class).when(client).setFileType(anyInt());

		try {
			InputStream stream = new ByteArrayInputStream("testData".getBytes(StandardCharsets.UTF_8));
			ftpClient.transfer(stream, "transferred.txt", config);
		} finally {
			verify(clientBuilder).closeClient(client);
		}
	}

	@Test(expected = DMSException.class)
	public void errorCommunication_send_InvalidStream() throws Exception {
		FTPClient client = mock(FTPClient.class);
		when(clientBuilder.buildClient(config)).thenReturn(client);
		InputStream inputStream = mock(InputStream.class);
		doThrow(IOException.class).when(inputStream).close();
		try {
			ftpClient.transfer(inputStream, "transferred.txt", config);
		} finally {
			verify(clientBuilder).closeClient(client);
		}
	}

	@Test(expected = DMSException.class)
	public void errorCommunication_send_InvalidStream_null() throws Exception {
		FTPClient client = mock(FTPClient.class);
		when(clientBuilder.buildClient(config)).thenReturn(client);
		try {
			ftpClient.transfer(null, "transferred.txt", config);
		} finally {
			verify(clientBuilder).closeClient(client);
		}
	}

	@Test(expected = DMSException.class)
	public void errorCommunication_delete() throws Exception {
		FTPClient client = mock(FTPClient.class);
		when(clientBuilder.buildClient(config)).thenReturn(client);
		when(client.deleteFile(anyString())).thenThrow(IOException.class);

		try {
			ftpClient.delete("test.txt", config);
		} finally {
			verify(clientBuilder).closeClient(client);
		}
	}

	@Test(expected = DMSException.class)
	public void errorCommunication_send_InvalidStream_and_close() throws Exception {
		FTPClient client = mock(FTPClient.class);
		when(clientBuilder.buildClient(config)).thenReturn(client);
		when(client.storeFile(anyString(), any(InputStream.class))).thenThrow(IOException.class);
		InputStream inputStream = mock(InputStream.class);
		doThrow(IOException.class).when(inputStream).close();
		try {
			ftpClient.transfer(inputStream, "transferred.txt", config);
		} finally {
			verify(clientBuilder).closeClient(client);
		}
	}

	@Test
	public void asyncDataSend() throws Exception {

		when(localStore.add(any(), any(Content.class))).thenReturn(new StoreItemInfo());

		InputStream stream = new ByteArrayInputStream("testData".getBytes(StandardCharsets.UTF_8));
		Serializable asyncId = ftpClient.transferAsync(stream, "test", config);
		assertNotNull(asyncId);

		ArgumentCaptor<Map<String, Serializable>> attributesCaptor = ArgumentCaptor.forClass(Map.class);
		verify(localStore).add(any(), any());
		verify(senderService).send(eq("java:/jms.queue.ImageQueue"), attributesCaptor.capture(),
				any(SendOptions.class));
		assertEquals("test", attributesCaptor.getValue().get("name"));
		assertEquals(config, attributesCaptor.getValue().get("config"));
	}

	@Test
	public void should_uploadAsync() throws Exception {
		FTPClient client = mock(FTPClient.class);
		when(clientBuilder.buildClient(config)).thenReturn(client);
		when(contentStore.getReadChannel(any(StoreItemInfo.class)))
				.thenReturn(FileDescriptor.create(() -> new ByteArrayInputStream(new byte[0]), 0));
		when(client.storeFile(anyString(), any(InputStream.class))).thenReturn(true);
		ftpClient.uploadTemporaryFile("name", config, new StoreItemInfo());

		verify(client).storeFile(anyString(), any(InputStream.class));
		verify(contentStore).delete(any(StoreItemInfo.class));
	}

	@Test(expected = EmfRuntimeException.class)
	public void should_throwException_onUploadAsync_withErrorFromContentStore() throws Exception {
		when(contentStore.getReadChannel(any(StoreItemInfo.class))).thenReturn(FileDescriptor.create(() -> {
			throw new EmfRuntimeException();
		}, -1));

		ftpClient.uploadTemporaryFile("name", new FTPConfiguration(), new StoreItemInfo());
	}

	@Test(expected = DMSException.class)
	public void should_throwException_onUploadAsync_withErrorDuringSend() throws Exception {
		when(contentStore.getReadChannel(any(StoreItemInfo.class)))
				.thenReturn(FileDescriptor.create(() -> new ByteArrayInputStream(new byte[0]), 0));

		doThrow(DMSException.class).when(clientBuilder).buildClient(any(FTPConfiguration.class));

		ftpClient.uploadTemporaryFile("name", new FTPConfiguration(), new StoreItemInfo());
	}

	@Test
	public void should_notUpload_withMissingFile() throws Exception {
		ftpClient.uploadTemporaryFile("name", new FTPConfiguration(), new StoreItemInfo());

		verify(clientBuilder, never()).buildClient(any(FTPConfiguration.class));
		verify(contentStore, never()).delete(any(StoreItemInfo.class));
	}

	@AfterClass
	public static void cleanUp() {
		ftpServer.stop();
	}

}
