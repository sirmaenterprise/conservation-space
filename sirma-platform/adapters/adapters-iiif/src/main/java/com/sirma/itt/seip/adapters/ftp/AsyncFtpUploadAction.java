package com.sirma.itt.seip.adapters.ftp;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.adapters.remote.FTPConfiguration;
import com.sirma.itt.seip.adapters.remote.FtpClient;
import com.sirma.itt.seip.content.ContentStore;
import com.sirma.itt.seip.content.ContentStoreProvider;
import com.sirma.itt.seip.content.StoreItemInfo;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.tasks.SchedulerActionAdapter;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * Task scheduler operation for delayed FTP file upload.
 *
 * @author BBonev
 */
@ApplicationScoped
@Named(AsyncFtpUploadAction.NAME)
public class AsyncFtpUploadAction extends SchedulerActionAdapter {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/** Action name. */
	public static final String NAME = "asyncFtpUpload";
	/**
	 * Configuration for the serialized {@link StoreItemInfo} used for temporary storing of the file before reaching
	 * {@link AsyncFtpUploadAction}
	 */
	public static final String TEMP_LOCATION = "tempLocation";
	/** The name of the remote file that should be used */
	public static final String FILE_NAME = "fileName";
	/**
	 * Configuration for the serialized {@link FTPConfiguration} that should be used for accessing the remote FTP
	 * server.
	 */
	public static final String FTP_CONFIG = "ftpConfig";

	private static final List<Pair<String, Class<?>>> PARAM_VALIDATION = Arrays.asList(
			new Pair<>(TEMP_LOCATION, Serializable.class), new Pair<>(FILE_NAME, String.class),
			new Pair<>(FTP_CONFIG, Serializable.class));

	@Inject
	private ContentStoreProvider storeProvider;
	@Inject
	private FtpClient ftpClient;
	@Inject
	private Statistics statistics;

	@Override
	@SuppressWarnings("boxing")
	public void execute(SchedulerContext context) throws Exception {
		String fileName = context.getIfSameType(FILE_NAME, String.class);
		FTPConfiguration ftpConfig = context.getIfSameType(FTP_CONFIG, FTPConfiguration.class);
		StoreItemInfo tempLocaltion = context.getIfSameType(TEMP_LOCATION, StoreItemInfo.class);

		// get the temp file that need to be send
		ContentStore contentStore = storeProvider.getStore(tempLocaltion);
		FileDescriptor descriptor = contentStore.getReadChannel(tempLocaltion);

		// if null then the file is probably synchronized and removed from the store
		// we cannot do anything
		if (descriptor != null) {
			TimeTracker tracker = statistics.createTimeStatistics(getClass(), "delayedFtpUpload").begin();
			LOGGER.debug("Begining transfer of delayed file upload of {}", fileName);
			try (InputStream stream = descriptor.getInputStream()) {
				// send the file synchronously
				ftpClient.transfer(stream, fileName, ftpConfig);
				// after success clean up
				contentStore.delete(tempLocaltion);
			} finally {
				LOGGER.debug("End of file transfer for {}. It took {} ms", fileName, tracker.stop());
			}
		} else {
			LOGGER.warn("Temp file {} from {} for transfer was not found!", tempLocaltion.getRemoteId(),
					tempLocaltion.getProviderType());
		}
	}

	@Override
	protected List<Pair<String, Class<?>>> validateInput() {
		return PARAM_VALIDATION;
	}

}
