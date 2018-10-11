package com.sirma.itt.seip.instance.content;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.instance.integration.InstanceDispatcher;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentStore;
import com.sirma.sep.content.ContentStoreProvider;
import com.sirma.sep.content.LocalStore;
import com.sirma.sep.content.TemporaryStore;

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
		return findStore(instanceDispatcher.getContentManagementSystem(instance, content)).orElseGet(this::getLocalStore);
	}

	@Override
	public Optional<ContentStore> findStore(String name) {
		if (StringUtils.isEmpty(name)) {
			return Optional.empty();
		}
		return Optional.ofNullable(storeMapping.get(name));
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
		return findStore(instanceDispatcher.getViewManagementSystem(instance, content)).orElseGet(this::getLocalStore);
	}

}
