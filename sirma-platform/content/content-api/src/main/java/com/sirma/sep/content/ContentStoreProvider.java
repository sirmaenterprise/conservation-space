package com.sirma.sep.content;

import java.io.Serializable;
import java.util.Optional;

/**
 * Factory for content store providers. The provider implementation may use information for
 * {@code com.sirma.itt.seip.instance.integration.InstanceDispatcher} in order to provide correct store implementation.
 *
 * @author BBonev
 * @see ContentStore
 */
public interface ContentStoreProvider {

	/**
	 * Gets the store that can handle the content for the given instance.
	 *
	 * @param instance
	 *            the instance
	 * @param content
	 *            the content
	 * @return the store or default local store if no supported store is found.
	 */
	ContentStore getStore(Serializable instance, Content content);

	/**
	 * Gets any store by name
	 *
	 * @param name
	 *            the name of the store to look for
	 * @return the store or if not found <code>null</code> or a default store
	 * @see ContentStore#getName()
	 */
	Optional<ContentStore> findStore(String name);

	/**
	 * Gets a store that can handle the given {@link StoreItemInfo}. <br>
	 * Default implementation uses the {@link StoreItemInfo#getProviderType()} to determine the store to return.
	 *
	 * @param storeItemInfo
	 *            the store item info
	 * @return the store or <code>null</code> if the item info is <code>null</code> or its
	 *         {@link StoreItemInfo#getProviderType()}
	 */
	default ContentStore getStore(StoreItemInfo storeItemInfo) {
		if (storeItemInfo == null || storeItemInfo.getProviderType() == null) {
			return null;
		}
		return findStore(storeItemInfo.getProviderType()).orElse(null);
	}

	/**
	 * Returns special content store that operates on local file system. This is the default store if no other store
	 * could handle the request
	 *
	 * @return the local store instance
	 */
	ContentStore getLocalStore();

	/**
	 * Returns special content store that operates on the local file system with temporary files
	 *
	 * @return the temporary store
	 */
	ContentStore getTempStore();

	/**
	 * Gets the {@link ContentStore} for instance views for the given instance.
	 *
	 * @param instance
	 *            the instance
	 * @param content
	 *            the content
	 * @return the view store or default local store if no supported store is found.
	 */
	ContentStore getViewStore(Serializable instance, Content content);
}
