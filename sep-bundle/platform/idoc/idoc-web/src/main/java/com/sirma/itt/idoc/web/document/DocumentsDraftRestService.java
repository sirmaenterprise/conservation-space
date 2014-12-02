package com.sirma.itt.idoc.web.document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.sirma.itt.cmf.beans.model.DraftInstance;
import com.sirma.itt.cmf.services.DraftService;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.emf.rest.model.RestInstance;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.util.sanitize.ContentSanitizer;

/**
 * The DocumentsDraftRestService provides access to draft instances for given
 * document.
 * 
 * @author bbanchev
 */
@ApplicationScoped
@Path("/intelligent-document/drafts")
@Produces(MediaType.APPLICATION_JSON)
public class DocumentsDraftRestService extends EmfRestService {

	/** The draft service. */
	@Inject
	private DraftService draftService;

	@Inject
	private ContentSanitizer contentSanitizer;

	/**
	 * Creates the.
	 * 
	 * @param rest
	 *            the rest
	 * @return the rest instance
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public DraftInstance create(RestInstance rest) {

		User currentUser = (User) getCurrentUser();
		if (currentUser == null) {
			throw new EmfRuntimeException("No authenticated user!");
		}

		rest.setContent(contentSanitizer.sanitize(rest.getContent()));
		Class<? extends Instance> instanceClass = getInstanceClass(rest.getType());
		Instance converted = typeConverter.convert(instanceClass, rest);
		if (converted == null) {
			throw new EmfRuntimeException("Invalid instance for draft creation is provided!");
		}

		Map<String, Serializable> properties = converted.getProperties();
		DraftInstance createdDraft = null;
		if (!properties.containsKey(DefaultProperties.CONTENT)) {
			properties.put(DefaultProperties.CONTENT, rest.getContent());
			createdDraft = draftService.create(converted, currentUser);
			properties.remove(DefaultProperties.CONTENT);
		} else {
			createdDraft = draftService.create(converted, currentUser);
		}
		return createdDraft;
	}

	/**
	 * Gets the draft instance for the given params. If user is not provided all
	 * drafts for the given instance are provided.
	 * 
	 * @param instanceId
	 *            the instance id
	 * @param instanceType
	 *            the instance type
	 * @param username
	 *            the username
	 * @return the list of associated drafts
	 */
	@GET
	public List<DraftInstance> find(@QueryParam("instanceId") String instanceId,
			@QueryParam("instanceType") String instanceType, @QueryParam("username") String username) {
		Instance instanceInternal = fetchInstance(instanceId, instanceType);
		if (instanceInternal == null) {
			throw new EmfRuntimeException(
					"Instance not found in the system! Draft could be loaded for valid instance (id: '"
							+ instanceId + "' with type: '" + instanceType + "')!");
		}
		User user = null;
		if (username != null) {
			user = resourceService.getResource(username, ResourceType.USER);
		}
		if (user == null) {
			throw new EmfRuntimeException(
					"Missing argument for user! Draft could be loaded only for particular user!");
		}
		DraftInstance draft = draftService.getDraft(instanceInternal, user);
		if (draft != null) {
			List<DraftInstance> drafts = new ArrayList<>(1);
			drafts.add(draft);
			return drafts;
		}
		return Collections.emptyList();
	}

	/**
	 * Delete a draft instance for given instance id and type. User is optional
	 * and deleted only his/her drafts
	 * 
	 * @param instanceId
	 *            the instance id
	 * @param instanceType
	 *            the type of instance
	 * @param username
	 *            the username
	 * @return the rest instance
	 */
	@DELETE
	public List<DraftInstance> delete(@QueryParam("instanceId") String instanceId,
			@QueryParam("instanceType") String instanceType, @QueryParam("username") String username) {
		Instance instanceInternal = fetchInstance(instanceId, instanceType);
		if (instanceInternal == null) {
			throw new EmfRuntimeException(
					"Instance not found in the system! Draft could be loaded for valid instance (id: '"
							+ instanceId + "' with type: '" + instanceType + "')!");
		}
		User user = null;
		if (username != null) {
			user = resourceService.getResource(username, ResourceType.USER);
			if (user == null) {
				throw new EmfRuntimeException("User not found in the system: " + username + "!");
			}
		}
		List<DraftInstance> drafts = new ArrayList<>();
		if (user == null) {
			drafts.addAll(draftService.delete(instanceInternal));
		} else {
			drafts.add(draftService.delete(instanceInternal, user));
		}
		return drafts;
	}
}
