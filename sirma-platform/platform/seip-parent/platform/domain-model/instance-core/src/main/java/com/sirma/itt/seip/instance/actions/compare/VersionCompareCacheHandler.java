package com.sirma.itt.seip.instance.actions.compare;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.rest.cache.CacheHandler;
import com.sirma.itt.seip.rest.utils.request.params.RequestParams;

/**
 * {@link CacheHandler} for requests to version comparison service.
 *
 * @author yasko
 */
@Singleton
public class VersionCompareCacheHandler implements CacheHandler {

	@Inject
	private InstanceTypeResolver resolver;

	@Override
	public void handleResponse(UriInfo info, ContainerResponseContext responseContext) {
		EntityTag etag = buildEtag(info);
		if (etag == null) {
			return;
		}

		MultivaluedMap<String, Object> responseHeaders = responseContext.getHeaders();
		responseHeaders.add(HttpHeaders.ETAG, etag.toString());
		responseHeaders.add(HttpHeaders.CACHE_CONTROL, "no-cache");
	}

	@Override
	public ResponseBuilder handleRequest(UriInfo info, Request request) {
		EntityTag etag = buildEtag(info);
		if (etag == null) {
			return null;
		}
		return request.evaluatePreconditions(etag);
	}

	private EntityTag buildEtag(UriInfo info) {
		String id = info.getPathParameters().getFirst(RequestParams.KEY_ID);
		String first = info.getQueryParameters().getFirst("first");
		String second = info.getQueryParameters().getFirst("second");

		if (StringUtils.isAnyBlank(id, first, second)) {
			return null;
		}

		Collection<Instance> instaces = resolver.resolveInstances(Arrays.asList(first, second));
		StringBuilder etag = new StringBuilder(id).append('-');
		for (Instance instance : instaces) {
			etag.append(instance.get("modifiedOn", Date.class).getTime());
		}
		return EntityTag.valueOf(etag.toString());
	}
}
