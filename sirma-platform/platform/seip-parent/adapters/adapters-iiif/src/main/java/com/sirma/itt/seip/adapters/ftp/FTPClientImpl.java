package com.sirma.itt.seip.adapters.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.seip.IntegerPair;
import com.sirma.itt.seip.adapters.remote.FTPConfiguration;
import com.sirma.itt.seip.adapters.remote.FtpClient;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.monitor.annotations.MetricDefinition;
import com.sirma.itt.seip.monitor.annotations.Monitored;
import com.sirma.itt.seip.monitor.annotations.MetricDefinition.Type;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentStore;
import com.sirma.sep.content.ContentStoreProvider;
import com.sirma.sep.content.LocalStore;
import com.sirma.sep.content.StoreItemInfo;
import com.sirmaenterprise.sep.jms.annotations.DestinationDef;
import com.sirmaenterprise.sep.jms.annotations.QueueListener;
import com.sirmaenterprise.sep.jms.api.SendOptions;
import com.sirmaenterprise.sep.jms.api.SenderService;
import com.sirmaenterprise.sep.jms.convert.ObjectMessageWriter;
import com.sirmaenterprise.sep.jms.exception.JmsRuntimeException;

/**
 * Base class for sending images to a FTP server. <br>
 *
 * @author Nikolay Ch
 * @author BBonev
 */
@ApplicationScoped
public class FTPClientImpl implements FtpClient {
	private static final String STORE_ITEM_INFO_ATTR = "storeItemInfo";
	private static final String CONFIG_ATTR = "config";
	private static final String FILE_NAME_ATTR = "name";
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private FtpClientBuilder ftpClientBuilder;
	private ContentStore localStore;

	@DestinationDef("java:/jms.queue.ImageQueue")
	public static final String IMAGE_QUEUE = "java:/jms.queue.ImageQueue";

	@Inject
	private SenderService senderService;

	@Inject
	private ContentStoreProvider storeProvider;

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
		localStore = contentStore;
	}

	/**
	 * Instantiate a new FTP client instance.
	 */
	public FTPClientImpl() {
		// Empty constructor required for the queue listener.
	}

	@Override
	public void transfer(InputStream stream, String fileName, FTPConfiguration config) throws DMSException {
		FTPClient ftpClient = ftpClientBuilder.buildClient(config);

		try (InputStream inputStream = stream) {
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			String remoteDir = config.getRemoteDir();
			// if ftpClient.changeWorkingDirectory(remoteDir) returns true it moves to the passed dir
			// this may cause problems when using relatives paths
			if (!ftpClient.changeWorkingDirectory(remoteDir)) {
				ftpClient.makeDirectory(remoteDir);
			}
			String remoteLocation = remoteDir + fileName;
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
			String remotePath = config.getRemoteDir() + name;
			boolean isDeleted = client.deleteFile(remotePath);
			if (!isDeleted) {
				LOGGER.warn("Could not delete file {} due to: {}", remotePath, client.getReplyString());
			}
			return isDeleted;
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

		Map<String, Serializable> attributes = new HashMap<>();
		attributes.put(FILE_NAME_ATTR, name);
		attributes.put(CONFIG_ATTR, config);
		attributes.put(STORE_ITEM_INFO_ATTR, storeItemInfo);
		senderService.send(IMAGE_QUEUE, attributes,
				SendOptions.create().withWriter(ObjectMessageWriter.instance()));

		return requestId;
	}

	/**
	 * Upload the temporary file to the ftp server.
	 *
	 * @param fileName
	 *            the new name of the file
	 * @param ftpConfig
	 *            the configuration for the connection
	 * @param tempLocation
	 *            the temp file location
	 * @throws DMSException
	 *             if an error in the ftp connection occurs
	 */
	@SuppressWarnings("boxing")
	@Monitored(@MetricDefinition(name = "ftp_upload_duration_seconds", type = Type.TIMER, descr = "Delayed ftp upload duration in seconds."))
	public void uploadTemporaryFile(String fileName, FTPConfiguration ftpConfig, StoreItemInfo tempLocation)
			throws DMSException {

		// get the temp file that need to be send
		ContentStore contentStore = storeProvider.getStore(tempLocation);
		FileDescriptor descriptor = contentStore.getReadChannel(tempLocation);

		// if null then the file is probably synchronized and removed from the store
		// we cannot do anything
		if (descriptor != null) {
			LOGGER.debug("Begining transfer of delayed file upload of {}", fileName);
			try (InputStream stream = descriptor.getInputStream()) {
				// send the file synchronously
				transfer(stream, fileName, ftpConfig);
				// after success clean up
				contentStore.delete(tempLocation);
			} catch (IOException e) {
				throw new DMSException("Error loading temporary file!", e);

			}
		} else {
			LOGGER.warn("Temp file {} from {} for transfer was not found!", tempLocation.getRemoteId(),
					tempLocation.getProviderType());
		}
	}

	/**
	 * Image queue message observer. Responsible for uploading the temporary files to the ftp
	 * server.
	 *
	 * @param msg
	 *            the image queue message
	 */
	@SuppressWarnings("unchecked")
	@QueueListener(value = IMAGE_QUEUE, concurrencyLevel = 10, txTimeout = 60, timeoutUnit = TimeUnit.MINUTES)
	public void onImageQueueMessage(Message msg) throws JMSException {
		if (msg instanceof ObjectMessage) {
			try {
				Map<String, Serializable> data = (Map<String, Serializable>) ((ObjectMessage) msg).getObject();
				uploadTemporaryFile((String) data.get(FILE_NAME_ATTR), (FTPConfiguration) data.get(CONFIG_ATTR),
						(StoreItemInfo) data.get(STORE_ITEM_INFO_ATTR));
			} catch (DMSException | JMSException e) {
				throw new JmsRuntimeException("Error while processing image queue message", e);
			}
		} else {
			throw new IllegalArgumentException("The message for the image queue isn't in the correct format.");
		}
	}

	private static String createRequestId() {
		return "async_ftp_" + UUID.randomUUID();
	}
}
