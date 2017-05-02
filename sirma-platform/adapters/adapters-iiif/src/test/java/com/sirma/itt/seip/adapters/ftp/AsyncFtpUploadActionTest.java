package com.sirma.itt.seip.adapters.ftp;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.seip.adapters.remote.FTPConfiguration;
import com.sirma.itt.seip.adapters.remote.FtpClient;
import com.sirma.itt.seip.content.ContentStore;
import com.sirma.itt.seip.content.ContentStoreProvider;
import com.sirma.itt.seip.content.StoreItemInfo;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.tasks.SchedulerContext;

/**
 * Tests for {@link AsyncFtpUploadAction}
 *
 * @author BBonev
 */
public class AsyncFtpUploadActionTest {

	@InjectMocks
	private AsyncFtpUploadAction action;

	@Mock
	private ContentStore contentStore;
	@Mock
	private ContentStoreProvider storeProvider;
	@Mock
	private FtpClient ftpClient;
	@Spy
	private Statistics statistics = Statistics.NO_OP;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(storeProvider.getStore(any(StoreItemInfo.class))).thenReturn(contentStore);
	}

	@Test
	public void execute() throws Exception {
		SchedulerContext context = new SchedulerContext();
		context.put(AsyncFtpUploadAction.FILE_NAME, "name");
		context.put(AsyncFtpUploadAction.TEMP_LOCATION, new StoreItemInfo());
		context.put(AsyncFtpUploadAction.FTP_CONFIG, new FTPConfiguration());

		when(contentStore.getReadChannel(any(StoreItemInfo.class)))
				.thenReturn(FileDescriptor.create(() -> new ByteArrayInputStream(new byte[0]), 0));

		action.execute(context);

		verify(ftpClient).transfer(any(), anyString(), any());
		verify(contentStore).delete(any());
	}

	@Test(expected = EmfRuntimeException.class)
	public void execute_withError() throws Exception {
		SchedulerContext context = new SchedulerContext();
		context.put(AsyncFtpUploadAction.FILE_NAME, "name");
		context.put(AsyncFtpUploadAction.TEMP_LOCATION, new StoreItemInfo());
		context.put(AsyncFtpUploadAction.FTP_CONFIG, new FTPConfiguration());

		when(contentStore.getReadChannel(any(StoreItemInfo.class))).thenReturn(FileDescriptor.create(() -> {
			throw new EmfRuntimeException();
		} , -1));

		action.execute(context);
	}

	@Test(expected = DMSException.class)
	public void execute_withErrorDuringSend() throws Exception {
		SchedulerContext context = new SchedulerContext();
		context.put(AsyncFtpUploadAction.FILE_NAME, "name");
		context.put(AsyncFtpUploadAction.TEMP_LOCATION, new StoreItemInfo());
		context.put(AsyncFtpUploadAction.FTP_CONFIG, new FTPConfiguration());

		when(contentStore.getReadChannel(any(StoreItemInfo.class)))
				.thenReturn(FileDescriptor.create(() -> new ByteArrayInputStream(new byte[0]), 0));

		doThrow(DMSException.class).when(ftpClient).transfer(any(), anyString(), any());

		action.execute(context);
	}

	@Test
	public void execute_noFile() throws Exception {
		SchedulerContext context = new SchedulerContext();
		context.put(AsyncFtpUploadAction.FILE_NAME, "name");
		context.put(AsyncFtpUploadAction.TEMP_LOCATION, new StoreItemInfo());
		context.put(AsyncFtpUploadAction.FTP_CONFIG, new FTPConfiguration());

		action.execute(context);

		verify(ftpClient, never()).transfer(any(), anyString(), any());
		verify(contentStore, never()).delete(any());
	}
}
