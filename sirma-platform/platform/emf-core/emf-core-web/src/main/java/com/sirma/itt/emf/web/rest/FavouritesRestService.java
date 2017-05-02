/**
 *
 */
package com.sirma.itt.emf.web.rest;

import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;

import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.rest.RestServiceException;
import com.sirma.itt.seip.resources.favorites.FavouritesService;

/**
 * Contains rest services for processing the favourites instances. Uses FavouritesService to add, remove and extract
 * favourite instances for the user.
 *
 * @author A. Kunchev
 */
@ApplicationScoped
@Path("/favourites")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FavouritesRestService extends EmfRestService {

	private static final String FAILED_TO_CREATE_LINK = "emf.rest.favourite.link.failed.creation";

	private static final String NO_FAVOURITE_INSTANCES = "emf.rest.favourite.no.favorites";

	@Inject
	private FavouritesService favouritesService;

	/**
	 * Gets the currently logged user favourites instances. Uses FavouritesService to extract the collection of
	 * instances. After that the all the instances are transformed to JSON objects and added to the response.
	 *
	 * <pre>
	 * Service path example:
	 * GET emf/services/favourites
	 * </pre>
	 *
	 * @return <b>OK</b> response with message that no instance are found for the current user, if the returned
	 *         collection from the service is empty <b>OR</b><br />
	 *         <b>OK</b> response with the instances collection as a JSON object, if the are favourite instances for the
	 *         current user<br />
	 */
	@GET
	public Response getFavourites() {
		Collection<InstanceReference> favouritesInstances = favouritesService.getAllForCurrentUser();
		if (CollectionUtils.isEmpty(favouritesInstances)) {
			String noInstancesRawMsg = labelProvider.getValue(NO_FAVOURITE_INSTANCES);
			String noInstancesMsg = String.format(noInstancesRawMsg, getCurrentLoggedUser().getDisplayName());
			return buildOkResponse(noInstancesMsg);
		}
		JSONArray response = convertInstanceReferencesToJSON(favouritesInstances);

		return buildOkResponse(response.toString());
	}

	/**
	 * Adds instance to current logged user favourites. Uses FavouritesService to pass the instance, that will be added.
	 * The id and the type of the instance are passed as data parameter, so they can be used to load the instance. This
	 * service consumes POST request, if the id or the type are missing, it will return bad response.
	 *
	 * <pre>
	 * Service path example:
	 * POST emf/services/favourites
	 * data = {
	 *     instanceId   : 'emf:xxxxxxxx-xxxxxxxx-xxxx-xxxxxxxxxxxx',
	 *     instanceType : 'someInstanceType'
	 * }
	 * </pre>
	 *
	 * @param data
	 *            the request data for the service
	 * @return <b>OK</b> response - when the instance is successfully added to the current user favourites.<br />
	 *         <b>BAD</b> response - when the instance id or type are missing or when the operation fails.
	 */
	@POST
	public Response addFavourite(String data) {
		InstanceReference instanceReference = extractInstanceReference(data);

		if (instanceReference != null) {
			boolean isAdded = favouritesService.add(instanceReference);

			if (isAdded) {
				return buildOkResponse(null);
			}
			String errorMessage = labelProvider.getValue(FAILED_TO_CREATE_LINK);
			String formatedMessage = String.format(errorMessage, instanceReference.getIdentifier());
			return buildOkResponse(formatedMessage);
		}

		throw new RestServiceException(EMF_REST_INSTANCE_MISSING_REQUIRED_ARGUMENTS, Status.BAD_REQUEST);
	}

	/**
	 * Removes instance from current logged user favourites. Uses FavouritesService to pass the instance that will be
	 * removed. The id and the type of the instance are passed as a data parameter, so they can be used to load the
	 * instance. This service consumes DELETE request, if the id or type are missing, it will return bad response.
	 *
	 * <pre>
	 * Service path example:
	 * DELETE emf/services/favourites
	 * data = {
	 *     instanceId   : 'emf:xxxxxxxx-xxxxxxxx-xxxx-xxxxxxxxxxxx',
	 *     instanceType : 'someInstanceType'
	 * }
	 * </pre>
	 *
	 * @param data
	 *            the request data for the service
	 * @return <b>OK</b> response - when the instance is successfully removed from the current user favourites.<br />
	 *         <b>BAD</b> response - when the instance id or type are missing.
	 *         <p />
	 *         <b>NOTE! The service will return always response 'OK', regardless the operation result.</b>
	 */
	@DELETE
	public Response removeFavourite(String data) {
		InstanceReference instanceReference = extractInstanceReference(data);

		if (instanceReference == null) {
			throw new RestServiceException(EMF_REST_INSTANCE_MISSING_REQUIRED_ARGUMENTS, Status.BAD_REQUEST);
		}

		favouritesService.remove(instanceReference);
		return buildOkResponse(null);
	}

}
