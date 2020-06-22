package com.sirma.sep.content;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.sirma.itt.seip.security.annotation.OnTenantRemove;

/**
 * Clears all {@link ContentStore}s which are marked for clearing, when tenant is deleted.
 *
 * @author A. Kunchev
 */
class ContentStoresCleaner {

	@Any
	@Inject
	private Instance<ContentStore> stores;

	@Inject
	private ContentStoreManagementService contentManagementService;

	@OnTenantRemove
	void cleanStoresOnTenantDelete() {
		stores.forEach(store -> {
			if (store.isCleanSupportedOnTenantDelete()) {
				contentManagementService.emptyContentStore(store.getName());
			}
		});
	}
}