package com.sirma.sep.content;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.concurrent.locks.ContextualLock;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationGroupDefinition;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.GroupConverterContext;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.sep.content.descriptor.LocalFileDescriptor;

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
	private static final String PATH_PATTERN = "yyyy" + File.separatorChar + "MM" + File.separatorChar + "dd"
			+ File.separatorChar + "HH" + File.separatorChar + "mm";

	@ConfigurationPropertyDefinition(defaultValue = ".", sensitive = true, type = File.class, converter = "directory", label = "Location to the local server store folder.")
	private static final String LOCALSTORE_PATH = "content.localstore.path";
	@ConfigurationGroupDefinition(properties = LOCALSTORE_PATH, type = File.class, label = "Location to the local server store folder.")
	private static final String LOCALSTORE_TENANT_PATH = "content.localstore.tenant_path";

	@Inject
	private SecurityContextManager contextManager;

	@Inject
	@Configuration(LOCALSTORE_PATH)
	private ConfigurationProperty<File> storeRootLocation;

	@Inject
	@Configuration(LOCALSTORE_TENANT_PATH)
	private ConfigurationProperty<File> tenantStoreRootLocation;

	@Inject
	private ContextualLock movePermission;

	@ConfigurationConverter(LOCALSTORE_TENANT_PATH)
	static File buildTenantSpecificLocalStore(GroupConverterContext context, SecurityContext securityContext) {
		File path = context.get(LOCALSTORE_PATH);
		if (path.getAbsolutePath().contains(securityContext.getCurrentTenantId())) {
			return path;
		}
		return new File(path, securityContext.getCurrentTenantId());
	}

	/**
	 * Adds store validation
	 */
	@PostConstruct
	protected void init() {
		storeRootLocation.addConfigurationChangeListener(LocalContentStore::validateLocalStore);
		tenantStoreRootLocation.addConfigurationChangeListener(LocalContentStore::validateLocalStore);
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
		if (testDir.mkdirs()) {
			LOGGER.info("Found valid configuration for local store at: {}", file.getAbsolutePath());
		} else {
			LOGGER.warn("No write permissions for local store at: {}", file.getAbsolutePath());
		}
		try {
			Files.delete(testDir.toPath());
		} catch (IOException e) {
			LOGGER.trace("Could not delete test folder", e);
		}
	}

	@Override
	public StoreItemInfo add(Serializable instance, Content descriptor) {
		if (descriptor == null) {
			return null;
		}
		File file = createNewFile();
		long written;
		try {
			LOGGER.trace("Adding new file: {}", file);
			written = descriptor.getContent().writeTo(file);
		} catch (IOException e) {
			try {
				Files.deleteIfExists(file.toPath());
			} catch (IOException e1) {
				LOGGER.trace("Could not delete the file on error while writting.", e1);
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
				File resolveFile = resolveFile(previousVersion.getRemoteId());
				if (resolveFile == null) {
					resolveFile = handleMissingFile(previousVersion);
				}
				long written = descriptor.getContent().writeTo(resolveFile);
				return buildStoreInfo(previousVersion.getRemoteId(), written);
			} catch (IOException e) {
				throw new StoreException("Could not override contents of file: " + previousVersion.getRemoteId(), e);
			}
		}
		throw new ContentStoreMissMatchException(getName(), previousVersion == null ? null : previousVersion.getProviderType());
	}

	private File handleMissingFile(StoreItemInfo previousVersion) {
		LOGGER.warn("File which should be updated is missing. Attempting to recover by creating "
				+ "new file, where the data will be stored.");
		File resolveFile = new File(getStoreLocation(), previousVersion.getRemoteId());
		// creates parent folder if does not exist, before returning the file
		getOrCreateFolder(resolveFile.getParentFile());
		return resolveFile;
	}

	private StoreItemInfo buildStoreInfo(String id, long written) {
		return createStoreInfo().setContentLength(written).setRemoteId(id);
	}

	@Override
	public FileDescriptor getReadChannel(StoreItemInfo storeInfo) {
		if (isFromThisStore(storeInfo)) {
			File resolvedFile = resolveFile(storeInfo.getRemoteId());
			if (resolvedFile != null && resolvedFile.exists()) {
				return new LocalFileDescriptor(resolvedFile);
			}
			return null;
		}
		throw new ContentStoreMissMatchException(getName(), storeInfo == null ? null : storeInfo.getProviderType());
	}

	@Override
	public boolean delete(StoreItemInfo itemInfo) {
		if (isFromThisStore(itemInfo)) {
			File file = resolveFile(itemInfo.getRemoteId());
			if (file == null) {
				return false;
			}
			try {
				Files.delete(file.toPath());
				LOGGER.trace("Deleted file: {}", itemInfo.getRemoteId());
			} catch (IOException e) {
				LOGGER.warn("Could not delete file: {} due to: {}", itemInfo.getRemoteId(), e.getMessage(), e);
				return false;
			}
			return true;
		}
		throw new ContentStoreMissMatchException(getName(), itemInfo == null ? null : itemInfo.getProviderType());
	}

	@Override
	public Optional<DeleteContentData> prepareForDelete(StoreItemInfo itemInfo) {
		if (!isFromThisStore(itemInfo)) {
			throw new ContentStoreMissMatchException(getName(), itemInfo == null ? null : itemInfo.getProviderType());
		}
		File resolvedFile = resolveFile(itemInfo.getRemoteId());
		if (resolvedFile == null || !resolvedFile.exists()) {
			return Optional.empty();
		}

		DeleteContentData data = new DeleteContentData()
				.setStoreName(getName())
				.addProperty("location", resolvedFile.getAbsolutePath())
				// a way to fallback if the store moved before actual delete
				.addProperty("filename", itemInfo.getRemoteId());
		return Optional.of(data);
	}

	@Override
	public void delete(DeleteContentData deleteContentData) {
		// check for valid argument
		ContentStore.super.delete(deleteContentData);

		String location = deleteContentData.getString("location");
		try {
			Files.delete(new File(location).toPath());
		} catch (NoSuchFileException e) {
			LOGGER.trace("Failed to complete standard delete of file at {}", location, e);
			// The filename is resolved using configuration which has to be executed in the tenant's config.
			File file = contextManager.executeAsTenant(deleteContentData.getTenantId())
					.function(this::resolveFile, deleteContentData.getString("filename"));
			if (file == null) {
				// file not found, nothing to delete
				return;
			}
			try {
				if (!file.getAbsolutePath().equals(location)) {
					Files.deleteIfExists(file.toPath());
					// if deleted successfully - the store was changed/moved
				} // else: same location something else is wrong
				return;
			} catch (IOException e1) {
				LOGGER.warn("Could not delete file: {}", file.getAbsolutePath());
				LOGGER.trace("", e1);
			}
			LOGGER.warn("Could not delete file: {} as it's probably already deleted", location);
		} catch (IOException e) {
			// something happen with the file
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Could not delete file: {}", location, e);
			} else {
				LOGGER.warn("Could not delete file: {} due to: {}", location, e.getMessage());
			}
		}
	}

	@Override
	public boolean isTwoPhaseDeleteSupported() {
		return true;
	}

	private String toExternalId(File file) {
		// does not include the store location so that the folder could be reconfigured
		File location = getStoreLocation();
		return file.getAbsolutePath().substring(location.getAbsolutePath().length());
	}

	private File getStoreLocation() {
		return tenantStoreRootLocation.requireConfigured("Could not load local store location").get();
	}

	private File getBaseStoreLocation() {
		return storeRootLocation.requireConfigured("Could not load base local store location").get();
	}

	private File resolveFile(String id) {
		if (id == null) {
			return null;
		}
		// look for the file at the default location
		File file = new File(getStoreLocation(), id);
		if (file.exists()) {
			return file;
		}
		// if not found look for the file in the base location without the tenant id in the path
		// this is generally the case for files stored before the store separation by tenants
		// production servers are not expected to have many files in the local store in the first place
		File baseFile = new File(getBaseStoreLocation(), id);
		if (baseFile.exists()) {
			// move the file to it's proper location in background
			moveFile(baseFile, file);
			if (baseFile.exists()) {
				// file was not moved successfully
				return baseFile;
			}
		}
		if (file.exists()) {
			// file relocated successfully
			return file;
		}
		return null;
	}

	private void moveFile(File moveFrom, File moveTo) {
		// simulate non concurrent move operations
		movePermission.lock();
		try {
			moveInternal(moveFrom, moveTo);
		} finally {
			movePermission.unlock();
		}
	}

	private static void moveInternal(File moveFrom, File moveTo) {
		if (!shouldMoveToDestination(moveFrom, moveTo)) {
			return;
		}
		// make sure the target directory exists otherwise the move fails
		moveTo.getParentFile().mkdirs();
		try {
			Files.move(moveFrom.toPath().toAbsolutePath(), moveTo.toPath().toAbsolutePath(), StandardCopyOption.ATOMIC_MOVE);
		} catch (AtomicMoveNotSupportedException e) {
			try {
				Files.move(moveFrom.toPath().toAbsolutePath(), moveTo.toPath().toAbsolutePath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e1) {
				e1.addSuppressed(e);
				LOGGER.warn("Could not move file {} to {}", moveFrom.getAbsolutePath(), moveTo.getAbsolutePath(), e1);
			}
		} catch (IOException e) {
			LOGGER.warn("Could not move file {} to {}", moveFrom.getAbsolutePath(), moveTo.getAbsolutePath(), e);
		}
	}

	private static boolean shouldMoveToDestination(File moveFrom, File moveTo) {
		if (moveTo.exists()) {
			if (moveTo.length() == moveFrom.length()) {
				try {
					Files.delete(moveFrom.toPath());
				} catch (IOException e) {
					LOGGER.warn("Could not complete moving of file {} to {}", moveFrom.getAbsolutePath(),
							moveTo.getAbsolutePath(), e);
				}
				// file already moved, just delete the original
				return false;
			}

			// files do not match, probably server was restarted during file move
			// back it up and we will try again
			try {
				Path moveToPath = moveTo.toPath().toAbsolutePath();
				Path newName = moveToPath.resolveSibling(moveTo.getName() + ".old");
				Files.move(moveToPath, newName);
				LOGGER.info("Found file at {} with unexpected file size. Renamed to {}", moveToPath,
						newName.getFileName());
			} catch (IOException e) {
				LOGGER.warn("Unable to rename potentially corrupted file {}. Will try to override it",
						moveTo.getAbsolutePath(), e);
			}
		}
		return true;
	}

	private File createNewFile() {
		File parent = new File(getStoreLocation(), new SimpleDateFormat(PATH_PATTERN).format(new Date()));
		return new File(getOrCreateFolder(parent), UUID.randomUUID() + ".bin");
	}

	private static File getOrCreateFolder(File directory) {
		if (!directory.exists() && !directory.mkdirs()) {
			if (directory.exists()) {
				// created by other thread
				return directory;
			}
			throw new StoreException("Cannot create store folder: " + directory.getAbsolutePath());
		}
		return directory;
	}

	@Override
	public String getName() {
		return LocalStore.NAME;
	}

	@Override
	public boolean isCleanSupportedOnTenantDelete() {
		return true;
	}
}