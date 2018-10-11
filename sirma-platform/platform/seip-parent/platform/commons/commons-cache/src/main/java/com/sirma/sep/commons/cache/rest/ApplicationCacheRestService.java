package com.sirma.sep.commons.cache.rest;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.cache.SimpleCache;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.json.JsonUtil;

/**
 * Provides services for execution of specific actions over application caches.
 *
 * @author A. Kunchev
 */
// not sure that this is the right place for it
@Transactional
@ApplicationScoped
@Path("/administration")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ApplicationCacheRestService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private EntityLookupCacheContext cacheContext;

	/**
	 * Causes clear of the internal application cache.
	 */
	@GET
	@Path("clearInternalCache")
	public void clearInternalCache() {
		LOGGER.info("Called clearInternalCache from WS port");
		Set<String> activeCaches = cacheContext.getActiveCaches();
		for (String cacheName : activeCaches) {
			if (cacheName.toLowerCase().contains("entity")) {
				LOGGER.info("Clearing {}", cacheName);
				SimpleCache<Serializable, Object> cache = cacheContext.getCache(cacheName, false);
				cache.clear();
			}
		}
	}

	/**
	 * Clears specified cache by its id.
	 *
	 * @param cacheName
	 *            the cache name
	 * @return {@code 200} if the cache is found and successfully cleared <br>
	 *         {@code 404} if the cache is not found <br>
	 *         {@code 400} if the cache id is not passed
	 */
	@GET
	@Path("{cacheId}/clear")
	public Response clearCache(@PathParam("cacheId") String cacheName) {
		if (StringUtils.isBlank(cacheName)) {
			return Response.status(Status.BAD_REQUEST).entity("{}").build();
		}

		EntityLookupCache<Serializable, Object, Serializable> cache = cacheContext.getCache(cacheName);
		if (cache == null) {
			return Response.status(Status.NOT_FOUND).entity("{}").build();
		}

		cache.clear();
		JSONObject object = new JSONObject();
		buildCacheStatistics(object, cacheName, cache);
		return Response.status(Status.OK).entity(object.toString()).build();
	}

	private static void buildCacheStatistics(JSONObject jsonObject, String cacheName,
			EntityLookupCache<Serializable, Object, Serializable> cache) {
		JSONObject values = new JSONObject();
		JsonUtil.addToJson(values, "primary", Integer.toString(cache.primaryKeys().size()));
		JsonUtil.addToJson(values, "secondary", Integer.toString(cache.secondaryKeys().size()));
		JsonUtil.addToJson(jsonObject, cacheName, values);
	}

	/**
	 * Retrieves statistics for the internal application caches.
	 *
	 * @return the cache statistics
	 */
	@GET
	@Path("cacheStatistics")
	public Response getCacheStatistics() {
		Set<String> activeCaches = cacheContext.getActiveCaches();
		JSONObject jsonObject = new JSONObject();
		for (String cacheName : activeCaches) {
			EntityLookupCache<Serializable, Object, Serializable> cache = cacheContext.getCache(cacheName);
			buildCacheStatistics(jsonObject, cacheName, cache);
		}

		return Response.status(Status.OK).entity(jsonObject.toString()).build();
	}
}
