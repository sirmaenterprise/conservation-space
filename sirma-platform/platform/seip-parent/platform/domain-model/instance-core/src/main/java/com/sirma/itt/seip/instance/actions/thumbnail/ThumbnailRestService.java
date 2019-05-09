package com.sirma.itt.seip.instance.actions.thumbnail;

import java.io.Serializable;
import java.util.Collections;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.instance.actions.relations.AddRelationRequest;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Rest service for executing operations related to instances thumbnails.
 *
 * @author A. Kunchev
 */
@Path("/instances")
@ApplicationScoped
@Consumes(Versions.V2_JSON)
@Produces(Versions.V2_JSON)
public class ThumbnailRestService {

	@Inject
	private Actions actions;

	/**
	 * Executes operation that adds thumbnail to given target instance. If the instance already has thumbnail, it is
	 * removed first and then added the new one.
	 *
	 * @param request
	 *            {@link AddThumbnailRequest} object containing needed data for the request
	 * @return OK status if the operation is executed successfully
	 */
	@POST
	@Path("/{id}/actions/thumbnail")
	public Instance addThumbnail(AddThumbnailRequest request) {
		AddRelationRequest addRelationRequest = new AddRelationRequest();
		Serializable thumbnailSource = Objects.requireNonNull(request.getThumbnailObjectId(),
				"Missing thumbnail object id in the request!");
		addRelationRequest.setRelations(Collections.singletonMap(LinkConstants.HAS_THUMBNAIL,
				Collections.singleton(thumbnailSource.toString())));
		addRelationRequest.setRemoveExisting(true);
		addRelationRequest.setTargetId(request.getTargetId());
		addRelationRequest.setTargetReference(request.getTargetReference());
		addRelationRequest.setPlaceholder(request.getPlaceholder());
		addRelationRequest.setContextPath(request.getContextPath());
		addRelationRequest.setUserOperation(request.getUserOperation());
		return (Instance) actions.callAction(addRelationRequest);
	}

}
