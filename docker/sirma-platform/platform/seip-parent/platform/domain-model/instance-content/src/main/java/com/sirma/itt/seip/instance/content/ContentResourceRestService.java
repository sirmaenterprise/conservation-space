package com.sirma.itt.seip.instance.content;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;

import com.sirma.itt.seip.content.ContentResourceManagerService;
import com.sirma.itt.seip.rest.annotations.security.PublicResource;
import com.sirma.sep.content.ContentInfo;

/**
 * Rest service that returns the requested (icon) content.
 *
 * @author Nikolay Ch
 */
@Path("/content/resource")
@ApplicationScoped
public class ContentResourceRestService {

	@Inject
	private ContentResourceManagerService contentResourceManager;

	/**
	 * Return the icon for the instance with the given size.
	 *
	 * @param contentId
	 *            the content id
	 * @return
	 */
	@GET
	@Transactional
	@PublicResource(tenantParameterName = "tenantId")
	@Path("/{tenantId}/{contentId}")
	public Response getResource(@PathParam("contentId") String contentId) {
		return getResourceInternal(contentId);
	}

	Response getResourceInternal(String contentId) {
		CacheControl cacheControl = new CacheControl();
		cacheControl.setMaxAge(86400);
		cacheControl.setPrivate(true);

		ContentInfo contentInfo = contentResourceManager.getContent(contentId, null);

		if (contentInfo.exists()) {

			return Response.ok(contentInfo.getInputStream()).cacheControl(cacheControl)
					.header("Content-Type", contentInfo.getMimeType()).build();
		}
		return Response.status(404).build();
	}

}
