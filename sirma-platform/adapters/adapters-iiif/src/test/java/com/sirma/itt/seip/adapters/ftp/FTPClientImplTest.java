package com.sirma.itt.seip.adapters.ftp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import org.apache.commons.net.ftp.FTPClient;
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

import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.seip.adapters.remote.FTPConfiguration;
import com.sirma.itt.seip.adapters.remote.FtpClient;
import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentStore;
import com.sirma.itt.seip.content.StoreItemInfo;
import com.sirma.itt.seip.tasks.DefaultSchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntry;
import com.sirma.itt.seip.tasks.SchedulerEntryStatus;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.tasks.TransactionMode;
import com.sirma.itt.seip.testutil.CustomMatcher;

/**
 * Class for testing the connection and the upload of a file to a ftp server using the FTPClientImpl.
 */
public class FTPClientImplTest {

	private static final String USER = "user";
	private static FakeFtpServer ftpServer;
	private FTPConfiguration config;

	@InjectMocks
	private FTPClientImpl ftpClient;

	@Spy
	private FtpClientBuilder clientBuilder;
	@Mock
	private SchedulerService schedulerService;
	@Mock
	private ContentStore localStore;

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

		config = new FTPConfiguration();
		config.setUsername(USER);
		config.setPassword(USER);
		config.setHostname("localhost");
		config.setSecured(false);
		config.setRemoteDir("/images/");

		config.setPort(ftpServer.getServerControlPort());
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

		SchedulerConfiguration configuration = new DefaultSchedulerConfiguration()
				.setType(SchedulerEntryType.IMMEDIATE);
		when(schedulerService.buildEmptyConfiguration(SchedulerEntryType.IMMEDIATE)).thenReturn(configuration);

		when(localStore.add(any(), any(Content.class))).thenReturn(new StoreItemInfo());

		InputStream stream = new ByteArrayInputStream("testData".getBytes(StandardCharsets.UTF_8));
		Serializable asyncId = ftpClient.transferAsync(stream, "test", config);
		assertNotNull(asyncId);

		verify(localStore).add(any(), any());
		verify(schedulerService).schedule(eq(AsyncFtpUploadAction.NAME), eq(configuration),
				any(SchedulerContext.class));
		assertFalse(configuration.isSynchronous());
		assertEquals(configuration.getTransactionMode(), TransactionMode.NOT_SUPPORTED);
		assertTrue(configuration.isPersistent());
		assertTrue(configuration.isRemoveOnSuccess());

	}

	@Test
	public void getAsyncRequestStatus() throws Exception {
		when(schedulerService.getScheduleEntry(anyString())).thenReturn(createEntry(SchedulerEntryStatus.FAILED),
				createEntry(SchedulerEntryStatus.COMPLETED), createEntry(SchedulerEntryStatus.RUNNING), null);

		assertEquals(FtpClient.OperationStatus.FAILED, ftpClient.getAsyncOperationStatus("requestId"));
		assertEquals(FtpClient.OperationStatus.COMPLETED, ftpClient.getAsyncOperationStatus("requestId"));
		assertEquals(FtpClient.OperationStatus.RUNNING, ftpClient.getAsyncOperationStatus("requestId"));
		assertEquals(FtpClient.OperationStatus.COMPLETED, ftpClient.getAsyncOperationStatus("requestId"));
	}

	@Test
	public void cancelAsyncRequest() throws Exception {
		when(schedulerService.getScheduleEntry(anyString())).thenReturn(createEntry(SchedulerEntryStatus.RUNNING), null);
		ftpClient.cancelAsyncRequest("requestId");
		verify(schedulerService).save(argThat(CustomMatcher
				.of(entry -> entry.getStatus() == SchedulerEntryStatus.COMPLETED, "Status does not match")));
		ftpClient.cancelAsyncRequest("requestId");
		verify(schedulerService, times(1)).save(any(SchedulerEntry.class));
	}

	private static SchedulerEntry createEntry(SchedulerEntryStatus status) {
		SchedulerEntry entry = new SchedulerEntry();
		entry.setStatus(status);
		return entry;
	}

	@AfterClass
	public static void cleanUp() {
		ftpServer.stop();
	}

}
