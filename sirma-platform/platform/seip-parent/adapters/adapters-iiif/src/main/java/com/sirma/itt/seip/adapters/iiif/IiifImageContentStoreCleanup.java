package com.sirma.itt.seip.adapters.iiif;

import javax.inject.Inject;

import com.sirma.itt.seip.security.annotation.OnTenantRemove;
import com.sirma.sep.content.ContentStoreManagementService;

/**
 * Listens for tenant remove event to delete all content stored in the IMAGE store for IIIF/IIP server
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 03/01/2018
 */
class IiifImageContentStoreCleanup {

	@Inject
	private ContentStoreManagementService contentManagementService;

	@OnTenantRemove
	void cleanImageContentStoreOnTenantDelete() {
		contentManagementService.emptyContentStore(IiifImageContentStore.STORE_NAME);
	}
}
