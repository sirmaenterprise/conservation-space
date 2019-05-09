package com.sirma.sep.content;

import java.util.Objects;
import java.util.Optional;

/**
 * Management service for content manipulations over the whole content stores.<br>
 * Example operations are content store initialization, content store migration, content store deletion, etc.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 02/01/2018
 */
public interface ContentStoreManagementService {

	/**
	 * Fetch the information about the given store
	 *
	 * @param storeName the store to fetch information about
	 * @return the found information if such store exists
	 */
	Optional<StoreInfo> getInfo(String storeName);

	/**
	 * Trigger a deletion of all the content stored in the {@link ContentStore} identified by the given store name.
	 * If the {@link ContentStore} supports two phase deletion it will be deleted asynchronously otherwise the
	 * content will be deleted synchronously and this may take a while.
	 *
	 * @param storeName the store name to empty
	 */
	void emptyContentStore(String storeName);

	/**
	 * Trigger method that should initialize moving the whole content store contents to other store. The operation will
	 * complete relatively fast but the actual content migration will happen asynchronously in background.
	 *
	 * @param sourceStore the content identifier of the content to move
	 * @param targetStore the target store to move the content to
	 * @return the current information about the source store like number of files that will be moved if any.
	 * @throws IllegalArgumentException if the given target store is not valid
	 */
	StoreInfo moveAllContent(String sourceStore, String targetStore);

	/**
	 * Schedule asynchronous content move of a single content to the given target store. This is mainly used for
	 * migrating content from one store to another on some special occasion like instance type change.<br>
	 * The content will be moved asynchronously at the end of the current transaction.
	 *
	 * @param content the content id to move
	 * @param targetStore the target store to move the content to
	 */
	void scheduleSingleContentMove(String content, String targetStore);

	/**
	 * Move the content identified by the given content identifier to the content store identified by the given store
	 * name.
	 *
	 * @param content the content identifier of the content to move
	 * @param targetStore the target store to move the content to
	 * @throws IllegalArgumentException if the given target store is not valid
	 * @throws ContentCorruptedException if the expected content size does not match the transferred size
	 */
	void moveContent(String content, String targetStore);

	/**
	 * Represents a snapshot of the known information about a ContentStore
	 *
	 * @author BBonev
	 */
	class StoreInfo {
		private final String storeName;
		private final int numberOfFiles;
		private final long totalSize;

		/**
		 * Instantiate class by providing the store name
		 *
		 * @param storeName to non null store name to set
		 * @param numberOfFiles the number of files the store contains
		 * @param totalSize the store occupied space in bytes
		 */
		public StoreInfo(String storeName, int numberOfFiles, long totalSize) {
			this.storeName = Objects.requireNonNull(storeName, "Store name is required");
			this.numberOfFiles = numberOfFiles;
			this.totalSize = totalSize;
		}

		/**
		 * The store name that represents the current instance
		 *
		 * @return the unique store name
		 */
		public String getStoreName() {
			return storeName;
		}

		/**
		 * The number of stored files in the store
		 *
		 * @return the number of files
		 */
		public int getNumberOfFiles() {
			return numberOfFiles;
		}

		/**
		 * The space occupied by the store contents in bytes
		 *
		 * @return the store occupied size in bytes
		 */
		public long getTotalSize() {
			return totalSize;
		}
	}
}
