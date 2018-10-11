package com.sirma.sep.content;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.sep.content.descriptor.LocalFileDescriptor;

/**
 * Content store implementation that works with local temporary files
 *
 * @author BBonev
 */
@TemporaryStore
@ApplicationScoped
public class TempContentStore implements ContentStore {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private TempFileProvider tempFileProvider;

	private TempContentStore() {
		// required for CDI proxying
	}

	/**
	 * Instantiates a new temp content store.
	 *
	 * @param tempFileProvider
	 *            the temp file provider
	 */
	@Inject
	public TempContentStore(TempFileProvider tempFileProvider) {
		this.tempFileProvider = tempFileProvider;
	}

	@Override
	public StoreItemInfo add(Serializable instance, Content descriptor) {
		if (descriptor == null) {
			return null;
		}
		File oldCopy = tempFileProvider.createTempFile("oldVersion", null);
		try {
			long written = descriptor.getContent().writeTo(oldCopy);
			return createStoreInfo().setRemoteId(oldCopy.getName()).setContentLength(written);
		} catch (IOException e) {
			LOGGER.warn("Could not create temp copy", e);
			tempFileProvider.deleteFile(oldCopy);
			return null;
		}
	}

	@Override
	public StoreItemInfo update(Serializable instance, Content descriptor, StoreItemInfo previousVersion) {
		if (isFromThisStore(previousVersion) && descriptor != null) {
			try {
				long written = descriptor.getContent().writeTo(getFileFromStore(previousVersion));
				return createStoreInfo().setContentLength(written).setRemoteId(previousVersion.getRemoteId());
			} catch (IOException e) {
				LOGGER.warn("Could not write to local temp folder", e);
			}
			return null;
		}
		throw new ContentStoreMissMatchException(getName(), previousVersion == null ? null : previousVersion.getProviderType());
	}

	private File getFileFromStore(StoreItemInfo previousVersion) {
		return new File(tempFileProvider.getTempDir(), previousVersion.getRemoteId());
	}

	@Override
	public FileDescriptor getReadChannel(StoreItemInfo storeInfo) {
		if (isFromThisStore(storeInfo)) {
			File file = getFileFromStore(storeInfo);
			if (file.exists()) {
				return new LocalFileDescriptor(file);
			}
			return null;
		}
		throw new ContentStoreMissMatchException(getName(), storeInfo == null ? null : storeInfo.getProviderType());
	}

	@Override
	public boolean delete(StoreItemInfo itemInfo) {
		if (isFromThisStore(itemInfo)) {
			tempFileProvider.deleteFile(getFileFromStore(itemInfo));
			return true;
		}
		throw new ContentStoreMissMatchException(getName(), itemInfo == null ? null : itemInfo.getProviderType());
	}

	@Override
	public Optional<DeleteContentData> prepareForDelete(StoreItemInfo itemInfo) {
		if (!isFromThisStore(itemInfo)) {
			throw new ContentStoreMissMatchException(getName(), itemInfo == null ? null : itemInfo.getProviderType());
	}
		File resolvedFile = getFileFromStore(itemInfo);
		if (!resolvedFile.exists()) {
			return Optional.empty();
		}
		DeleteContentData data = new DeleteContentData()
				.setStoreName(getName())
				.addProperty("location", resolvedFile.getAbsolutePath());
		return Optional.of(data);
	}

	@Override
	public void delete(DeleteContentData deleteContentData) {
		// check for valid argument
		ContentStore.super.delete(deleteContentData);

		String location = deleteContentData.getString("location");
		tempFileProvider.deleteFile(new File(location));
	}

	@Override
	public boolean isTwoPhaseDeleteSupported() {
		return true;
	}

	@Override
	public String getName() {
		return TemporaryStore.NAME;
	}

}
