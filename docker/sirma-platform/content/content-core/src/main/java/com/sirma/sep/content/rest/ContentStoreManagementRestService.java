package com.sirma.sep.content.rest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sirma.itt.seip.rest.annotations.security.AdminResource;
import com.sirma.itt.seip.rest.exceptions.ResourceNotFoundException;
import com.sirma.sep.content.ContentStoreManagementService;

/**
 * Content store management REST endpoint
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 03/05/2018
 */
@AdminResource
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Path("/content/store")
public class ContentStoreManagementRestService {

	@Inject
	private ContentStoreManagementService storeManagementService;

	/**
	 * Retrieve the current information about the requested content store.
	 *
	 * @param storeName the requested store name
	 * @return store information
	 * @throws ResourceNotFoundException when the requested content store is not found
	 */
	@GET
	@Path("{storeName}")
	public ContentStoreManagementService.StoreInfo getInfo(@PathParam("storeName") String storeName) {
		return storeManagementService.getInfo(storeName)
				.orElseThrow(() -> new ResourceNotFoundException("Content store " + storeName + " not found!"));
	}

	/**
	 * Trigger content store transfer from one store to another. The actual content copy happens in background. <br>
	 * {@code GET /content/store/{storeName}} could be used to check the remaining files that need to be moved.
	 *
	 * @param sourceStore the content copy source
	 * @param targetStore the content copy location
	 * @return the information about the original store. In other words what is going to be moved to the other store
	 */
	@POST
	@Path("{storeName}/transferTo/{targetStore}")
	public ContentStoreManagementService.StoreInfo move(@PathParam("storeName") String sourceStore,
			@PathParam("targetStore") String targetStore) {
		return storeManagementService.moveAllContent(sourceStore, targetStore);
	}
}

