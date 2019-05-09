package com.sirma.sep.content;

import java.io.Serializable;
import java.util.Optional;

import com.sirma.itt.seip.Named;
import com.sirma.itt.seip.io.FileDescriptor;

/**
 * Manages content retrieval and upload to remove servers. <br>
 * The store implementation may throw {@link StoreException} to indicate problems with writing the content to the store.
 *
 * @author BBonev
 */
public interface ContentStore extends Named {

	/**
	 * Adds the content presented by the given {@link Content} to the store. The content will will be retrievable by the
	 * returned {@link StoreItemInfo}.
	 *
	 * @param instance
	 *            the associated instance with the given content
	 * @param descriptor
	 *            the descriptor that provides means of reading the content and storing it via the store
	 * @return the store item info for the written content or <code>null</code> if the descriptor is <code>null</code>.
	 */
	StoreItemInfo add(Serializable instance, Content descriptor);

	/**
	 * Updates a content of an existing stored file. If the content does not exist it may be added to the store
	 *
	 * @param instance
	 *            the associated instance with the given content
	 * @param descriptor
	 *            the descriptor that provides means of reading the content and storing it via the store
	 * @param previousVersion
	 *            the store item that needs to be updated. Note that this item should be produced by the same store.
	 * @return the store item info of the updated file or <code>null</code> if the given previous version is not from
	 *         the store or is <code>null</code>
	 */
	StoreItemInfo update(Serializable instance, Content descriptor, StoreItemInfo previousVersion);

	/**
	 * Provides read only access to the content identified by the given {@link StoreItemInfo}.
	 *
	 * @param storeInfo
	 *            the store info to the content that need to be accessed.
	 * @return the file descriptor or <code>null</code> if the given store info is not from the store.
	 */
	FileDescriptor getReadChannel(StoreItemInfo storeInfo);

	/**
	 * Provides read only access to the content preview identified by the given {@link StoreItemInfo}. This method may
	 * return the same content or other content that is suitable for browser preview like PDF or image.
	 *
	 * @param storeInfo
	 *            the store info to the content that need to be accessed.
	 * @return the file descriptor or <code>null</code> if the given store info is not from the store.
	 */
	default FileDescriptor getPreviewChannel(StoreItemInfo storeInfo) {
		return getReadChannel(storeInfo);
	}

	/**
	 * Deletes the content identified by the given {@link StoreItemInfo}. if the content does not exists or the
	 * descriptor is not from the store the method may do nothing.
	 *
	 * @param itemInfo
	 *            the item info to delete
	 * @return <code>true</code>, if found and successfully deleted
	 */
	boolean delete(StoreItemInfo itemInfo);

	/**
	 * This method is the first part of two phase file deletion process. In order to do actual deletion the
	 * {@link #delete(DeleteContentData)} should be called with result from this method if any
	 * <p>
	 * Calling this method should inform the store that the client will want to delete the file identified by the given
	 * {@link StoreItemInfo}. The actual deletion should not happen during the method invocation. The store should
	 * populate any required properties in the returned {@link DeleteContentData} instance as needed in order for the
	 * store to perform the delete operation at later time without the need of any external and additional information.
	 * The added properties should be only of primitive types and String.
	 * </p>
	 *
	 * @param itemInfo the store item info representing the deleted file.
	 * @return Optional with information that can be used for file deletion. If empty the client will assume the file
	 * was already deleted or not supported.
	 */
	default Optional<DeleteContentData> prepareForDelete(StoreItemInfo itemInfo) {
		if (!isFromThisStore(itemInfo)) {
			throw new ContentStoreMissMatchException(getName(), itemInfo == null ? null : itemInfo.getProviderType());
		}
		return Optional.empty();
	}

	/**
	 * This method is the second part of two phase file deletion process.
	 * <p>
	 * Calling this method should perform a deletion of the file described by the given {@link DeleteContentData}. The
	 * instance should be produced from the same store
	 * </p>
	 *
	 * @param deleteContentData the content data that can be used for actual file deletion
	 */
	default void delete(DeleteContentData deleteContentData) {
		if (deleteContentData == null) {
			throw new ContentStoreMissMatchException(getName(), null);
		}
		if (!getName().equals(deleteContentData.getStoreName())) {
			throw new ContentStoreMissMatchException(getName(), deleteContentData.getStoreName());
		}
	}

	/**
	 * Reports if the current content store supports two phase content delete.
	 *
	 * @return true if the methods {@link #prepareForDelete(StoreItemInfo)} and {@link #delete(DeleteContentData)} will
	 * operate properly
	 */
	default boolean isTwoPhaseDeleteSupported() {
		return false;
	}

	/**
	 * Gets any known metadata for the given content additional to the one provided in the {@link StoreItemInfo}
	 *
	 * @param itemInfo
	 *            the item info
	 * @return the meta data of the file
	 */
	default ContentMetadata getMetadata(StoreItemInfo itemInfo) {
		return ContentMetadata.NO_METADATA;
	}

	/**
	 * Checks if the given {@link StoreItemInfo} is non <code>null</code> and is from this store.
	 *
	 * @param itemInfo
	 *            the item info to check
	 * @return <code>true</code>, if is from this store and <code>false</code> if not or <code>null</code>
	 */
	default boolean isFromThisStore(StoreItemInfo itemInfo) {
		return itemInfo != null && getName().equals(itemInfo.getProviderType());
	}

	/**
	 * Creates the store info instance with populated provider type the name of this store.
	 *
	 * @return the store item info
	 */
	default StoreItemInfo createStoreInfo() {
		return new StoreItemInfo().setProviderType(getName());
	}

	/**
	 * Shows whether the the store supports removal on tenant delete or not.<br>
	 * By default is set to false.
	 *
	 * @return {@code true} if the removal is supported on tenant delete, {@code false} otherwise
	 */
	default boolean isCleanSupportedOnTenantDelete() {
		return false;
	}

	/**
	 * Gets the name of this store. This name will be used later to identify the source of a {@link StoreItemInfo}
	 *
	 * @return the store name, never <code>null</code>.
	 */
	@Override
	String getName();
}