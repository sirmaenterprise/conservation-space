package com.sirma.itt.seip.adapters.ftp;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.adapters.remote.FTPConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.sep.content.StoreItemInfo;

/**
 * Tests for {@link AsyncFtpUploadAction}
 *
 * @author BBonev
 * @author nvelkov
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("deprecation")
public class AsyncFtpUploadActionTest {

	@Mock
	private FTPClientImpl FTPClient;

	@InjectMocks
	private AsyncFtpUploadAction action;

	@Test
	public void should_callUploadService() throws Exception {
		SchedulerContext context = new SchedulerContext();
		context.put(AsyncFtpUploadAction.FILE_NAME, "name");
		context.put(AsyncFtpUploadAction.TEMP_LOCATION, new StoreItemInfo());
		context.put(AsyncFtpUploadAction.FTP_CONFIG, new FTPConfiguration());

		action.execute(context);

		verify(FTPClient).uploadTemporaryFile(eq("name"), any(FTPConfiguration.class), any(StoreItemInfo.class));
	}
}
