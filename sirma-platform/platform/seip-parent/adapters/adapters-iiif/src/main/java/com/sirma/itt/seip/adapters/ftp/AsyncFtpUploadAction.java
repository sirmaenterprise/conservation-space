package com.sirma.itt.seip.adapters.ftp;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.adapters.remote.FTPConfiguration;
import com.sirma.itt.seip.tasks.SchedulerActionAdapter;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.sep.content.StoreItemInfo;

/**
 * Task scheduler operation for delayed FTP file upload.
 *
 * @deprecated Don't schedule async ftp uploads. This was implemented before the jms queues. Use jms
 *             queues with the {@link FTPClientImpl}.
 * @author BBonev
 */
@ApplicationScoped
@Named(AsyncFtpUploadAction.NAME)
@Deprecated
public class AsyncFtpUploadAction extends SchedulerActionAdapter {
	/** Action name. */
	public static final String NAME = "asyncFtpUpload";
	/**
	 * Configuration for the serialized {@link StoreItemInfo} used for temporary storing of the file
	 * before reaching {@link AsyncFtpUploadAction}
	 */
	public static final String TEMP_LOCATION = "tempLocation";
	/** The name of the remote file that should be used */
	public static final String FILE_NAME = "fileName";
	/**
	 * Configuration for the serialized {@link FTPConfiguration} that should be used for accessing
	 * the remote FTP server.
	 */
	public static final String FTP_CONFIG = "ftpConfig";

	private static final List<Pair<String, Class<?>>> PARAM_VALIDATION = Arrays.asList(
			new Pair<>(TEMP_LOCATION, Serializable.class), new Pair<>(FILE_NAME, String.class),
			new Pair<>(FTP_CONFIG, Serializable.class));

	@Inject
	private FTPClientImpl ftpClient;

	@Override
	public void execute(SchedulerContext context) throws Exception {
		String fileName = context.getIfSameType(FILE_NAME, String.class);
		FTPConfiguration ftpConfig = context.getIfSameType(FTP_CONFIG, FTPConfiguration.class);
		StoreItemInfo tempLocation = context.getIfSameType(TEMP_LOCATION, StoreItemInfo.class);

		ftpClient.uploadTemporaryFile(fileName, ftpConfig, tempLocation);
	}

	@Override
	protected List<Pair<String, Class<?>>> validateInput() {
		return PARAM_VALIDATION;
	}

}
