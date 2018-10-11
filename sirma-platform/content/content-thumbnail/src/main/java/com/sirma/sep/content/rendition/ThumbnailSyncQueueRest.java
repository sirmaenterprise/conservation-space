package com.sirma.sep.content.rendition;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.sirma.itt.seip.json.JsonUtil;

/**
 * Rest service for administrative management of thumbnails
 *
 * @author BBonev
 */
@Path("/system/admin/thumbnailQueue")
@ApplicationScoped
public class ThumbnailSyncQueueRest {

	@Inject
	private ThumbnailSyncQueue syncQueue;

	/**
	 * Gets thumbnail queue status. Returns the status of the workers and the active count of entries waiting for
	 * processing
	 *
	 * @return the queue status
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getStatus() {
		JSONObject object = new JSONObject();
		JsonUtil.addToJson(object, "active", syncQueue.size());
		JsonUtil.addToJson(object, "workers", new JSONArray(syncQueue.getWorkerInfo()));
		return object.toString();
	}

	/**
	 * Gets the waiting elements of the queue
	 *
	 * @return the active count as json structure
	 */
	@GET
	@Path("active")
	@Produces(MediaType.APPLICATION_JSON)
	public String getActiveCount() {
		JSONObject object = new JSONObject();
		JsonUtil.addToJson(object, "active", syncQueue.size());
		return object.toString();
	}

	/**
	 * Reset waiting queue
	 *
	 * @param provider optional provider name to reset: alfresco4/iiif
	 */
	@POST
	@Path("reset")
	public void reset(@QueryParam("provider") String provider) {
		if (StringUtils.isBlank(provider)) {
			syncQueue.clear();
		} else {
			syncQueue.clear(provider);
		}
	}

	/**
	 * Restart queue workers
	 */
	@POST
	@Path("restartWorkers")
	public void restartWorkers() {
		syncQueue.disable();
		syncQueue.enable();
	}
}
