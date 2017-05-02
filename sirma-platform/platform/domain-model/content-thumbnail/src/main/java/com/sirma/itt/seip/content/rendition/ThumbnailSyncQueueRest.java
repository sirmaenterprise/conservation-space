package com.sirma.itt.seip.content.rendition;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sirma.itt.seip.json.JsonUtil;

/**
 * Rest service for administrative management of thumbnails
 *
 * @author BBonev
 */
@Path("/admin/thumbnailQueue")
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
	 */
	@POST
	@Path("reset")
	public void reset() {
		syncQueue.clear();
	}

	/**
	 * Stop queue workers
	 */
	@POST
	@Path("stopWorkers")
	public void stop() {
		syncQueue.disable();
	}

	/**
	 * Start queue workers
	 */
	@POST
	@Path("startWorkers")
	public void start() {
		syncQueue.enable();
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
