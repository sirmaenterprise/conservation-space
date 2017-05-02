/**
 *
 */
package com.sirma.itt.seip.instance.content;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentStore;
import com.sirma.itt.seip.content.ContentStoreProvider;
import com.sirma.itt.seip.content.LocalStore;
import com.sirma.itt.seip.content.TemporaryStore;
import com.sirma.itt.seip.instance.integration.InstanceDispatcher;

/**
 * Default content store provider that operates using the {@link InstanceDispatcher} for find the name of the store to
 * return.
 *
 * @author BBonev
 */
@Singleton
public class ContentStoreProviderImpl implements ContentStoreProvider {

	@Inject
	@LocalStore
	private ContentStore localStore;

	@Inject
	@TemporaryStore
	private ContentStore tempStore;

	@Inject
	@Any
	private javax.enterprise.inject.Instance<ContentStore> stores;

	/**
	 * Cache for the store mappings. This is also optimization for CDI implementation of
	 * javax.enterprise.inject.Instance that too much invocation leads to excessive CDI context creation and huge memory
	 * consumption
	 */
	private Map<String, ContentStore> storeMapping = new HashMap<>();

	@Inject
	private InstanceDispatcher instanceDispatcher;

	/**
	 * Initialize store mapping
	 */
	@PostConstruct
	protected void init() {
		for (ContentStore contentStore : stores) {
			storeMapping.put(contentStore.getName(), contentStore);
		}
	}

	@Override
	public ContentStore getStore(Serializable instance, Content content) {
		return getStore(instanceDispatcher.getContentManagementSystem(instance, content));
	}

	@Override
	public ContentStore getStore(String name) {
		return storeMapping.getOrDefault(name, getLocalStore());
	}

	@Override
	public ContentStore getLocalStore() {
		return localStore;
	}

	@Override
	public ContentStore getTempStore() {
		return tempStore;
	}

	@Override
	public ContentStore getViewStore(Serializable instance, Content content) {
		return getStore(instanceDispatcher.getViewManagementSystem(instance, content));
	}

}
