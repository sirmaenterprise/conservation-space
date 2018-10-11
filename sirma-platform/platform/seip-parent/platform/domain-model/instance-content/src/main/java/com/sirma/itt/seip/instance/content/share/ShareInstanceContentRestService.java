package com.sirma.itt.seip.instance.content.share;

import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.params.RequestParams;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Endpoint that provides means to share a content from the system for the public. Without permission restrictions.
 * <p>
 * TODO: Refactor web methods:
 * <ul>createContentShareTasks -> turn it to post, because if attachments are a lot it can exceed the maximum post</ul>
 * <ul>createContentShareTasks -> so iut returns a map so it can be more generic.</ul>
 *
 * @author A. Kunchev
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 */
@Transactional
@ApplicationScoped
@Consumes(Versions.V2_JSON)
@Produces(Versions.V2_JSON)
@Path("/instances")
public class ShareInstanceContentRestService {

	@Inject
	private ShareInstanceContentService shareInstanceContentService;

	/**
	 * Gets an public link for instance's content. The content behind the link might not be accessible immediately but
	 * after a short delay, because the operation is asynchronous.
	 *
	 * @param id
	 * 		an id of already existing instance.
	 * @param contentFormat
	 * 		if the document is creatable, we export it first. This parameter is the targeted export format.
	 * @return an publicly accessible URL with ShareCode.
	 */
	@GET
	@Path("/{id}/content/share/immediate")
	public String getSharedContentImmediate(@PathParam(RequestParams.KEY_ID) String id,
			@QueryParam("contentFormat") @DefaultValue("pdf") String contentFormat) {
		return shareInstanceContentService.getSharedContentURI(id, contentFormat);
	}

	/**
	 * Gets an public link for instance's content. The content behind the link will NOT be accessible immediately but
	 * only after POST is called the same endpoint, but with the task id.
	 *
	 * @param id
	 * 		an id of already existing instance.
	 * @param contentFormat
	 * 		if the document is creatable, we export it first. This parameter is the targeted export format.
	 * @return list of URLs with ShareCodes.
	 */
	@GET
	@Path("/{id}/content/share")
	public String createContentShareTask(@PathParam(RequestParams.KEY_ID) String id,
			@QueryParam("contentFormat") @DefaultValue("pdf") String contentFormat) {
		return shareInstanceContentService.createContentShareTask(id, contentFormat);
	}

	/**
	 * Executes the actual share for already existing share URL.
	 *
	 * @param taskIdentifier
	 * 		the identifier of the task (ShareCodes).
	 */
	@POST
	@Transactional
	@Path("/content/share/{id}")
	public void triggerTaskExecution(@PathParam(RequestParams.KEY_ID) String taskIdentifier) {
		shareInstanceContentService.triggerContentShareTask(taskIdentifier);
	}

	/**
	 * Gets an public URLs for multiple instances' contents. The content behind the link will NOT be accessible
	 * immediately but only after POST is called the same endpoint, but with list of task identifiers.
	 *
	 * @param instanceIds
	 * 		an id of already existing instance.
	 * @param contentFormat
	 * 		if the document is creatable, we export it first. This parameter is the targeted export format.
	 * @return list of URLs with ShareCodes.
	 */
	@GET
	@Path("/content/share")
	public List<String> createContentShareTasks(@QueryParam(value = "ids") List<String> instanceIds,
			@QueryParam("contentFormat") @DefaultValue("pdf") String contentFormat) {
		return new ArrayList<>(
				shareInstanceContentService.createContentShareTasks(instanceIds, contentFormat).values());
	}

	/**
	 * Executes the actual share for already existing share URL.
	 *
	 * @param taskIdentifiers
	 * 		the identifiers of the task (ShareCodes).
	 */
	@POST
	@Transactional
	@Path("/content/share")
	public void triggerTasksExecution(Collection<String> taskIdentifiers) {
		shareInstanceContentService.triggerContentShareTasks(taskIdentifiers);
	}
}
