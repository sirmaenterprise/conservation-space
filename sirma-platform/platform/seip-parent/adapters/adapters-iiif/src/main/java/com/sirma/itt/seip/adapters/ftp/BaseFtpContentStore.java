package com.sirma.itt.seip.adapters.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.seip.IntegerPair;
import com.sirma.itt.seip.adapters.remote.FTPConfiguration;
import com.sirma.itt.seip.adapters.remote.FtpClient;
import com.sirma.itt.seip.io.CountingInputStream;
import com.sirma.itt.seip.util.file.FileUtil;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentStore;
import com.sirma.sep.content.ContentStoreMissMatchException;
import com.sirma.sep.content.DeleteContentData;
import com.sirma.sep.content.StoreException;
import com.sirma.sep.content.StoreItemInfo;

/**
 * Base {@link ContentStore} implementation for working with FTP servers. The implementation must provide means to
 * access the remote files.
 *
 * @author BBonev
 */
public abstract class BaseFtpContentStore implements ContentStore {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static final String ASYNC_REQUEST_ID = "asyncRequestID";

	@Inject
	protected javax.enterprise.inject.Instance<FtpClient> ftpClient;

	@Override
	public StoreItemInfo add(Serializable instance, Content descriptor) {
		if (descriptor == null) {
			return null;
		}
		String newRemoteName = buildDestinationFileName(descriptor.getName());
		try (CountingInputStream countingStream = new CountingInputStream(descriptor.getContent().getInputStream())) {
			Serializable asyncRequestId = storeContent(newRemoteName, descriptor.getContentLength(), countingStream);

			StoreItemInfo storeItemInfo = createStoreInfo()
					.setRemoteId(newRemoteName)
					.setContentLength(countingStream.getCount());
			return addAdditionalInfo(storeItemInfo, ASYNC_REQUEST_ID, asyncRequestId);
		} catch (DMSException | IOException e) {
			throw new StoreException("Could not add file", e);
		}
	}

	@Override
	public StoreItemInfo update(Serializable instance, Content descriptor, StoreItemInfo previousVersion) {
		if (!isFromThisStore(previousVersion)) {
			throw new ContentStoreMissMatchException(getName(), previousVersion == null ? null : previousVersion.getProviderType());
		}

		return add(instance, descriptor);
	}

	/**
	 * Store the given stream using the {@link FtpClient} returned by {@link #getFtpClient()}. The method checks the
	 * given content length and calls one of the methods
	 * {@link FtpClient#transferAsync(InputStream, String, FTPConfiguration)} or
	 * {@link FtpClient#transfer(InputStream, String, FTPConfiguration)} depending on the result of
	 * {@link #isTransferSynchronous(Long)}
	 *
	 * @param name
	 *            the remote name
	 * @param contentLength
	 *            the content length of the known data to send or <code>null</code> if not known
	 * @param stream
	 *            the stream of data to store at the ftp server
	 * @return the async request id or <code>null</code> if the file is transferred synchronously
	 * @throws DMSException
	 *             the DMS exception
	 */
	protected Serializable storeContent(String name, Long contentLength, InputStream stream)
			throws DMSException {
		FTPConfiguration ftpConfig = getFtpConfig();
		if (isTransferSynchronous(contentLength)) {
			getFtpClient().transfer(stream, name, ftpConfig);
			return null;
		}

		return getFtpClient().transferAsync(stream, name, ftpConfig);
	}

	/**
	 * Checks if a data of the given size (if known) should be transferred synchronously or asynchronously
	 *
	 * @param contentLength
	 *            the content length to check (may be <code>null</code>)
	 * @return <code>true</code>, if should be synchronously and <code>false</code> if asynchronously
	 */
	protected boolean isTransferSynchronous(Long contentLength) {
		long threshold = getAsyncThreshold();
		return threshold < 0 || contentLength != null && threshold > contentLength;
	}

	@SuppressWarnings({ "unchecked", "static-method" })
	protected StoreItemInfo addAdditionalInfo(StoreItemInfo info, String key, Serializable value) {
		Map<String, Serializable> data = (Map<String, Serializable>) info.getAdditionalData();
		if (data == null) {
			data = new HashMap<>();
		}
		data.put(key, value);
		info.setAdditionalData((Serializable) data);
		return info;
	}

	/**
	 * Extracts additional information from the given {@link StoreItemInfo}
	 *
	 * @param info
	 *            the info
	 * @param key
	 *            the data key
	 * @return the additional info value or <code>null</code>
	 */
	@SuppressWarnings({ "unchecked", "static-method" })
	protected Serializable getAdditionalInfo(StoreItemInfo info, String key) {
		Serializable data = info.getAdditionalData();
		if (data instanceof Map<?, ?>) {
			return ((Map<String, Serializable>) data).get(key);
		}
		return null;
	}

	protected Serializable getAsyncRequestId(StoreItemInfo previousVersion) {
		return getAdditionalInfo(previousVersion, ASYNC_REQUEST_ID);
	}

	@Override
	public boolean delete(StoreItemInfo itemInfo) {
		if (isFromThisStore(itemInfo)) {
			String remoteId = itemInfo.getRemoteId();
			// materialize the configuration for the write location
			// we assume the file is uploaded and processed successfully so it's located in the write directory if different
			// from the read location if we could not delete the file it's probably not processed, yet. We will try to
			// delete it from the original read location as well
			FTPConfiguration ftpConfig = getFtpConfig();
			if (!doDelete(remoteId, ftpConfig)) {
				ftpConfig.setRemoteDir(getBaseReadDir(ftpConfig));
				return doDelete(remoteId, ftpConfig);
			}
			return true;
		}
		throw new ContentStoreMissMatchException(getName(), itemInfo == null ? null : itemInfo.getProviderType());
	}

	private boolean doDelete(String remoteId, FTPConfiguration ftpConfig) {
		boolean isDeleted = false;
		try {
			isDeleted = getFtpClient().delete(remoteId, ftpConfig);
			if (isDeleted) {
				LOGGER.trace("Deleted {}", remoteId);
			}
		} catch (DMSException e) {
			LOGGER.warn("Could not delete remote file {}", remoteId, e);
		}
		return isDeleted;
	}

	@Override
	public Optional<DeleteContentData> prepareForDelete(StoreItemInfo storeInfo) {
		if (!isFromThisStore(storeInfo)) {
			throw new ContentStoreMissMatchException(getName(), storeInfo == null ? null : storeInfo.getProviderType());
		}
		FTPConfiguration ftpConfig = getFtpConfig();
		DeleteContentData data = new DeleteContentData().setStoreName(getName())
				.addProperty("remoteId", storeInfo.getRemoteId())
				.addProperty("readLocation", getBaseReadDir(ftpConfig))
				.addProperty("writeLocation", ftpConfig.getRemoteDir())
				.addProperty("password", Base64.getEncoder().encodeToString(ftpConfig.getPassword().getBytes(StandardCharsets.UTF_8)))
				.addProperty("username", ftpConfig.getUsername())
				.addProperty("activeModePorts", portsToString(ftpConfig.getActiveModePorts()))
				.addProperty("hostname", ftpConfig.getHostname())
				.addProperty("port", ftpConfig.getPort())
				.addProperty("maxAllowedConnections", ftpConfig.getMaxAllowedConnections());
		return Optional.of(data);
	}

	/**
	 * The implementations should provide the location of the stores files. The default implementation assumes they are
	 * in the same directory as uploaded.
	 *
	 * @param ftpConfiguration the current ftp configuration that will be used for path resolving
	 * @return the final location of the uploaded files
	 */
	protected String getBaseReadDir(FTPConfiguration ftpConfiguration) {
		return ftpConfiguration.getRemoteDir();
	}

	private static String portsToString(IntegerPair pair) {
		return pair.getFirst() + "," + pair.getSecond();
	}

	@Override
	public void delete(DeleteContentData deleteContentData) {
		// check for valid argument
		ContentStore.super.delete(deleteContentData);
		
		String remoteId = deleteContentData.getString("remoteId");
		LOGGER.debug("Deleting file {} from FTP Image store", remoteId);

		// materialize the configuration for the write location
		// we assume the file is uploaded and processed successfully so it's located in the write directory if different
		// from the read location if we could not delete the file it's probably not processed, yet. We will try to
		// delete it from the original read location as well
		FTPConfiguration configuration = materializeFtpConfiguration(deleteContentData, false);
		if (!doDelete(remoteId, configuration)) {
			doDelete(remoteId, materializeFtpConfiguration(deleteContentData, true));
		}
	}

	@Override
	public boolean isTwoPhaseDeleteSupported() {
		return true;
	}

	private FTPConfiguration materializeFtpConfiguration(DeleteContentData deleteContentData, boolean usingWriteDir) {
		FTPConfiguration configuration = new FTPConfiguration();
		configuration.setUsername(deleteContentData.getString("username"));
		configuration.setPassword(new String(Base64.getDecoder().decode(deleteContentData.getString("password")), StandardCharsets.UTF_8));
		if (usingWriteDir) {
			configuration.setRemoteDir(deleteContentData.getString("writeLocation"));
		} else {
			configuration.setRemoteDir(deleteContentData.getString("readLocation"));
		}
		configuration.setHostname(deleteContentData.getString("hostname"));
		configuration.setPort(deleteContentData.getInt("port"));
		configuration.setMaxAllowedConnections(deleteContentData.getInt("maxAllowedConnections"));
		// parse the active ports from the format "startPort,endPort"
		String activeModePorts = deleteContentData.getString("activeModePorts");
		String[] ports = activeModePorts.split(",");
		configuration.setActivePortRange(new IntegerPair(Integer.parseInt(ports[0]), Integer.parseInt(ports[1])));
		return configuration;
	}

	/**
	 * Provide the FTP client instance to use.
	 *
	 * @return the ftp client
	 */
	protected FtpClient getFtpClient() {
		return ftpClient.get();
	}

	/**
	 * Gets the configuration that can be used to contacting the FTP server.
	 *
	 * @return the ftp config
	 */
	protected abstract FTPConfiguration getFtpConfig();

	/**
	 * Gets the async threshold. Files bigger than the value returned in bytes will be uploaded asynchronously. To
	 * disable this functionality the returned value should be less than 0 or {@link Long#MAX_VALUE}
	 *
	 * @return the async threshold
	 */
	protected abstract long getAsyncThreshold();

	/**
	 * Builds the destination file name. The default implementation generates a random name and copies the extension if
	 * any from the source or adds a bin extension if the source name is <code>null</code>.
	 *
	 * @param sourceName
	 *            the source name
	 * @return the new name
	 */
	@SuppressWarnings("static-method")
	protected String buildDestinationFileName(String sourceName) {
		String extension = sourceName == null ? "bin" : FileUtil.splitNameAndExtension(sourceName).getSecond();
		return UUID.randomUUID() + "." + extension;
	}

}
