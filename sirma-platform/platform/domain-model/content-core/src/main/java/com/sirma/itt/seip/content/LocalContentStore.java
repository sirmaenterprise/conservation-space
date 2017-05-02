package com.sirma.itt.seip.content;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.content.descriptor.LocalFileDescriptor;
import com.sirma.itt.seip.io.FileDescriptor;

/**
 * Default implementation of {@link ContentStore} that can write to local file system. If no other store is defined this
 * will be used. The store operates on a folder that need to have write permissions.
 *
 * @author BBonev
 */
@LocalStore
@ApplicationScoped
public class LocalContentStore implements ContentStore {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String PATH_PATTERN = "yyyy" + File.separatorChar + "dd" + File.separatorChar + "MM"
			+ File.separatorChar + "HH" + File.separatorChar + "mm";

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "content.localstore.path", defaultValue = ".", sensitive = true, type = File.class, label = "Location to the local server store folder.")
	private ConfigurationProperty<File> storeRootLocation;

	/**
	 * Adds store validation
	 */
	@PostConstruct
	protected void init() {
		storeRootLocation.addConfigurationChangeListener(config -> validateLocalStore(config));
	}

	private static void validateLocalStore(ConfigurationProperty<File> location) {
		if (location.isNotSet()) {
			LOGGER.warn(
					"Store location does not exists. Please make sure the store location is created and the application has write permissions");
			return;
		}
		File file = location.get();
		LOGGER.info("Verifying local store location: {}", file.getAbsolutePath());
		if (file.isFile()) {
			LOGGER.warn("Configured store location to a file. The store may not operate properly");
		}
		File testDir = new File(file, Long.toString(System.currentTimeMillis()));
		if (testDir.mkdir()) {
			LOGGER.info("Found valid configuration for local store at: {}", file.getAbsolutePath());
		} else {
			LOGGER.warn("No write permissions for local store at: {}", file.getAbsolutePath());
		}
		if (!testDir.delete()) {
			LOGGER.trace("Could not delete test folder");
		}
	}

	@Override
	public StoreItemInfo add(Serializable instance, Content descriptor) {
		if (descriptor == null) {
			return null;
		}
		File file = createNewFile();
		long written = -1L;
		try {
			LOGGER.trace("Adding new file: {}", file);
			written = descriptor.getContent().writeTo(file);
		} catch (IOException e) {
			if (!file.delete()) {
				LOGGER.trace(
						"Could not delete the file on error while writting. Probably does not exist or cannot be accessed");
			}
			throw new StoreException("Cannot write to file " + file.getAbsolutePath(), e);
		}
		return buildStoreInfo(toExternalId(file), written);
	}

	@Override
	public StoreItemInfo update(Serializable instance, Content descriptor, StoreItemInfo previousVersion) {
		if (isFromThisStore(previousVersion)) {
			try {
				LOGGER.trace("Overriding file: {}", previousVersion.getRemoteId());
				long written = descriptor.getContent().writeTo(resolveFile(previousVersion.getRemoteId()));
				return buildStoreInfo(previousVersion.getRemoteId(), written);
			} catch (IOException e) {
				throw new StoreException("Could not override contents of file: " + previousVersion.getRemoteId(), e);
			}
		}
		return null;
	}

	private StoreItemInfo buildStoreInfo(String id, long written) {
		return createStoreInfo().setContentLength(written).setRemoteId(id);
	}

	@Override
	public FileDescriptor getReadChannel(StoreItemInfo storeInfo) {
		if (isFromThisStore(storeInfo)) {
			File resolvedFile = resolveFile(storeInfo.getRemoteId());
			if (resolvedFile.exists()) {
				return new LocalFileDescriptor(resolvedFile);
			}
		}
		return null;
	}

	@Override
	public boolean delete(StoreItemInfo itemInfo) {
		boolean isDeleted = false;
		if (isFromThisStore(itemInfo)) {
			isDeleted = resolveFile(itemInfo.getRemoteId()).delete();
			if (isDeleted) {
				LOGGER.trace("Deleted file: {}", itemInfo.getRemoteId());
			} else {
				LOGGER.warn("Could not delete file: {}", itemInfo.getRemoteId());
			}
		}
		return isDeleted;
	}

	private String toExternalId(File file) {
		// does not include the store location so that the folder could be reconfigured
		String absolutePath = storeRootLocation.get().getAbsolutePath();
		return file.getAbsolutePath().substring(absolutePath.length());
	}

	private File resolveFile(String id) {
		return new File(storeRootLocation.get(), id);
	}

	private File getFolder() {
		File directory = new File(storeRootLocation.get(), new SimpleDateFormat(PATH_PATTERN).format(new Date()));
		if (!directory.exists() && !directory.mkdirs()) {
			throw new StoreException("Cannot create store folder: " + directory.getAbsolutePath());
		}
		return directory;
	}

	private File createNewFile() {
		return new File(getFolder(), UUID.randomUUID() + ".bin");
	}

	@Override
	public String getName() {
		return LocalStore.NAME;
	}

}
