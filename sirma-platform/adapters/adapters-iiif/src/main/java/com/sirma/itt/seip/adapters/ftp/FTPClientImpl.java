package com.sirma.itt.seip.adapters.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.seip.IntegerPair;
import com.sirma.itt.seip.adapters.remote.FTPConfiguration;
import com.sirma.itt.seip.adapters.remote.FtpClient;
import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentStore;
import com.sirma.itt.seip.content.LocalStore;
import com.sirma.itt.seip.content.StoreItemInfo;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.tasks.SchedulerConfiguration;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerEntry;
import com.sirma.itt.seip.tasks.SchedulerEntryStatus;
import com.sirma.itt.seip.tasks.SchedulerEntryType;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.tasks.TransactionMode;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * Base class for sending images to a FTP server. <br>
 *
 * @author Nikolay Ch
 * @author BBonev
 */
@ApplicationScoped
public class FTPClientImpl implements FtpClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private FtpClientBuilder ftpClientBuilder;
	private SchedulerService schedulerService;
	private ContentStore localStore;

	/**
	 * Instantiates a new FTP client instance using the given actual client builder.
	 *
	 * @param ftpClientBuilder
	 *            the ftp client builder
	 * @param schedulerService
	 *            the scheduler service
	 * @param contentStore
	 *            the content store
	 */
	@Inject
	public FTPClientImpl(FtpClientBuilder ftpClientBuilder, SchedulerService schedulerService,
			@LocalStore ContentStore contentStore) {
		this.ftpClientBuilder = ftpClientBuilder;
		this.schedulerService = schedulerService;
		localStore = contentStore;
	}

	@Override
	public void transfer(InputStream stream, String fileName, FTPConfiguration config) throws DMSException {
		FTPClient ftpClient = ftpClientBuilder.buildClient(config);

		try (InputStream inputStream = stream) {
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			String remoteLocation = config.getRemoteDir() + fileName;
			String tempFileLocation = remoteLocation + ".uploading";
			setTransferMode(ftpClient, config);
			doTransfer(ftpClient, inputStream, tempFileLocation);
			boolean success = ftpClient.rename(tempFileLocation, remoteLocation);
			if (!success) {
				LOGGER.warn("Unsuccessful renaming from {} to {}", tempFileLocation, remoteLocation);
			}
		} catch (IOException e) {
			throw new DMSException("An error occured while trying to send data to the FTP server.", e);
		} finally {
			ftpClientBuilder.closeClient(ftpClient);
		}
	}

	private static void setTransferMode(FTPClient ftpClient, FTPConfiguration config) {
		if (config.isUseActiveMode()) {
			ftpClient.enterLocalActiveMode();
			IntegerPair activeModePorts = config.getActiveModePorts();
			ftpClient.setActivePortRange(activeModePorts.getFirst().intValue(), activeModePorts.getSecond().intValue());
		} else {
			// force passive mode so the data transfer connection is open from the client to the server
			// this is useful then the client has a firewall up that prevents the server from calling the client
			// at a random port, in this mode the server firewall could be configured for a specific port range
			ftpClient.enterLocalPassiveMode();
		}
	}

	@SuppressWarnings("boxing")
	private static void doTransfer(FTPClient ftpClient, InputStream inputStream, String remoteLocation)
			throws IOException, DMSException {
		TimeTracker tracker = TimeTracker.createAndStart();
		LOGGER.debug("Transferring to remote file: {}", remoteLocation);
		if (ftpClient.storeFile(remoteLocation, inputStream)) {
			LOGGER.debug("Transfer complete for remote file: {}. Transfer took {} ms", remoteLocation, tracker.stop());
		} else {
			throw new DMSException(
					"Transfer of file: " + remoteLocation + " failed due to: " + ftpClient.getReplyString());
		}
	}

	@Override
	public boolean delete(String name, FTPConfiguration config) throws DMSException {
		FTPClient client = ftpClientBuilder.buildClient(config);

		try {
			return client.deleteFile(config.getRemoteDir() + name);
		} catch (IOException e) {
			throw new DMSException("Failed executing delete operation for file: " + name, e);
		} finally {
			ftpClientBuilder.closeClient(client);
		}
	}

	@Override
	public Serializable transferAsync(InputStream stream, String name, FTPConfiguration config) throws DMSException {
		// create one type content instance to persist in the local store for later retrieval
		Content content = Content.createEmpty().setContent(FileDescriptor.create(() -> stream, -1L)).setName(name);
		StoreItemInfo storeItemInfo = localStore.add(null, content);
		String requestId = createRequestId();

		SchedulerConfiguration configuration = buildSchedulerConfig(requestId, config);
		SchedulerContext context = buildContext(name, config, storeItemInfo);

		schedulerService.schedule(AsyncFtpUploadAction.NAME, configuration, context);
		return requestId;
	}

	private static SchedulerContext buildContext(String name, FTPConfiguration config, StoreItemInfo storeItemInfo) {
		SchedulerContext context = new SchedulerContext();
		context.put(AsyncFtpUploadAction.FILE_NAME, name);
		context.put(AsyncFtpUploadAction.TEMP_LOCATION, storeItemInfo);
		context.put(AsyncFtpUploadAction.FTP_CONFIG, config);
		return context;
	}

	private SchedulerConfiguration buildSchedulerConfig(String requestId, FTPConfiguration config) {
		SchedulerConfiguration configuration = schedulerService
				.buildEmptyConfiguration(SchedulerEntryType.IMMEDIATE)
					.setMaxRetryCount(50)
					.setRetryDelay(Long.valueOf(60))
					.setIncrementalDelay(false)
					.setSynchronous(false)
					.setPersistent(true)
					.setTransactionMode(TransactionMode.NOT_SUPPORTED)
					.setRemoveOnSuccess(true)
					.setMaxActivePerGroup(config.getHostname(), config.getMaxAllowedConnections());
		configuration.setIdentifier(requestId);
		return configuration;
	}

	private static String createRequestId() {
		return "async_ftp_" + UUID.randomUUID();
	}

	@Override
	public OperationStatus getAsyncOperationStatus(Serializable requestId) {
		SchedulerEntry entry = schedulerService.getScheduleEntry((String) requestId);
		if (entry == null || entry.getStatus() == SchedulerEntryStatus.COMPLETED) {
			return OperationStatus.COMPLETED;
		}
		if (SchedulerEntryStatus.ACTIVE_STATES.contains(entry.getStatus())) {
			return OperationStatus.RUNNING;
		}
		return OperationStatus.FAILED;
	}

	@Override
	public void cancelAsyncRequest(Serializable requestId) {
		SchedulerEntry entry = schedulerService.getScheduleEntry((String) requestId);
		if (entry != null) {
			entry.setStatus(SchedulerEntryStatus.COMPLETED);
			schedulerService.save(entry);
		}
	}
}
