package com.sirma.itt.seip.instance.draft.rest;

import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.KEY_ID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.sirma.itt.seip.domain.rest.InternalServerErrorException;
import com.sirma.itt.seip.instance.draft.DraftInstance;
import com.sirma.itt.seip.instance.draft.DraftService;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Rest service for instance drafts. Contains logic for create, retrieve and delete instance drafts.
 *
 * @author A. Kunchev
 */
@Transactional
@Path("/instances/{id}/drafts")
@Produces(Versions.V2_JSON)
@ApplicationScoped
public class InstanceDraftRestService {

	@Inject
	private DraftService draftService;

	/**
	 * Creates draft for given instance. The content is sanitized, before the draft is created. The draft is created for
	 * the currently logged used.
	 * <p>
	 * <b>Note that there can be only one draft per instance/user pair!</b>
	 * </p>
	 *
	 * @param id
	 *            the id of the instance for which should be created draft
	 * @param rawContent
	 *            the content that should be saved for the draft
	 * @return the created draft information
	 * @throws InternalServerErrorException
	 *             when the internal service fail to create draft
	 */
	@POST
	public DraftInstance createDraft(@PathParam(KEY_ID) String id, String rawContent) {
		if (rawContent == null) {
			throw new BadRequestException("Cannot create draft for [null] content.");
		}

		DraftInstance draft = draftService.create(id, null, rawContent);

		if (draft == null) {
			throw new InternalServerErrorException("Failed to create draft for instance with id: " + id);
		}

		return draft;
	}

	/**
	 * Retrieves draft for given instance and currently logged user.
	 * <p>
	 * <b>Note that there is only one draft per instance/user pair!</b>
	 * </p>
	 *
	 * @param id
	 *            the id of the instance which drafts are searched
	 * @return the drafts information for the given instance, if any
	 */
	@GET
	public DraftInstance getDraft(@PathParam(KEY_ID) String id) {
		return draftService.getDraft(id, null);
	}

	/**
	 * Deletes draft for given instance and currently authenticated user.
	 *
	 * @param id
	 *            the id of the instance which drafts should be deleted
	 * @return deleted draft information, if any
	 */
	@DELETE
	public DraftInstance deleteDraft(@PathParam(KEY_ID) String id) {
		return draftService.delete(id, null);
	}

}
