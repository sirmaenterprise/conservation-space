package com.sirma.itt.cmf.content.extract.mail;

import java.io.File;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.sirma.itt.cmf.content.extract.MailExtractor;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.io.TempFileProvider;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.FileUtil;

/**
 * Base class for mail extractor implementations to hold the common logic.
 * 
 * @author BBonev
 * @param <T>
 *            the generic type
 */
public abstract class BaseMailExtractor<T> implements MailExtractor<T> {

	/** The temp file provider. */
	@Inject
	protected TempFileProvider tempFileProvider;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> extractMail(FileDescriptor descriptor,
			boolean deleteParts, File tempDir, String prefix, String... parts) {
		if (parts == null) {
			return Collections.emptyMap();
		}
		Map<String, Object> model = CollectionUtils.createHashMap(parts.length);
		for (String string : parts) {
			if (PART_ATTACHMENTS.equals(string)) {
				List<File> attachmentList = new LinkedList<>();
				try {
					Pair<T, String> openedMessage = openMessage(descriptor.getInputStream());
					// prepare parent
					File tempStoreDir = getTempFolder(tempDir);
					Pair<String, String> nameAndExtension = FileUtil.splitNameAndExtension(prefix);
					// extract and fill the list
					extractAttachments(openedMessage, nameAndExtension.getFirst() + "_",
							tempStoreDir, deleteParts, attachmentList);
					model.put(PART_ATTACHMENTS, attachmentList);
					postExtract(descriptor, openedMessage, deleteParts);
				} catch (Exception e) {
					getLogger().error("Error during exraction of mail!", e);
				}
			} else {
				throw new EmfRuntimeException("Unimplemented operation!");
			}
		}

		return model;
	}

	/**
	 * Extract attachments.
	 * 
	 * @param message
	 *            the message
	 * @param prefix
	 *            the prefix
	 * @param tempStoreDir
	 *            the temp store dir
	 * @param deleteParts
	 *            the delete parts
	 * @param attachmentList
	 *            the attachment list
	 * @throws Exception
	 *             the exception
	 */
	protected abstract void extractAttachments(Pair<T, String> message, String prefix,
			File tempStoreDir, boolean deleteParts, List<File> attachmentList) throws Exception;

	/**
	 * Post extract.
	 * 
	 * @param descriptor
	 *            the descriptor
	 * @param openedMessage
	 *            the opened message
	 * @param deleteParts
	 *            the delete parts
	 * @throws Exception
	 *             the exception
	 */
	protected abstract void postExtract(FileDescriptor descriptor,
			Pair<T, String> openedMessage, boolean deleteParts) throws Exception;

	/**
	 * Gets the temp folder.
	 * 
	 * @param tempDir
	 *            the temp dir
	 * @return the temp folder
	 */
	protected File getTempFolder(File tempDir) {
		File tempStoreDir = tempDir;
		if ((tempStoreDir == null) || !tempStoreDir.canWrite()) {
			Serializable sessionFolder = RuntimeConfiguration
					.getConfiguration(RuntimeConfigurationProperties.UPLOAD_SESSION_FOLDER);
			if ((sessionFolder instanceof File) && (sessionFolder != tempDir)) {
				tempStoreDir = getTempFolder((File) sessionFolder);
			} else {
				tempStoreDir = tempFileProvider.createTempDir(UUID.randomUUID().toString());
				tempStoreDir.mkdirs();
			}
		}
		return tempStoreDir;
	}

	/**
	 * Gets the logger.
	 * 
	 * @return the logger
	 */
	protected abstract Logger getLogger();
}
