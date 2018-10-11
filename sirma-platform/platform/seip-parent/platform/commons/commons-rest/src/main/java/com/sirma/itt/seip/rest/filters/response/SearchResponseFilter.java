package com.sirma.itt.seip.rest.filters.response;

import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.KEY_LIMIT;
import static com.sirma.itt.seip.rest.utils.request.params.RequestParams.KEY_OFFSET;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.rest.annotations.search.Search;
import com.sirma.itt.seip.rest.models.SearchResponseWrapper;
import com.sirma.itt.seip.rest.utils.Versions;

/**
 * Filter responsible for adding Link header to search responses. It adds next and last page links.
 *
 * @author yasko
 */
@Search
@Provider
@Produces(Versions.V2_JSON)
@Priority(Priorities.HEADER_DECORATOR)
public class SearchResponseFilter implements ContainerResponseFilter {

	/** &lt;http://the-link.com&gt;; rel="next" */
	private static final String LINK_HEADER_ITEM_FORMAT = "<%s>; rel=\"%s\"";
	private static final String REL_NEXT = "next";
	private static final String REL_LAST = "last";

	@Override
	public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException {
		String requestUri = req.getUriInfo().getRequestUriBuilder().toTemplate();
		Object rawEntity = res.getEntity();
		if (rawEntity instanceof SearchResponseWrapper) {
			SearchResponseWrapper<?> entity = (SearchResponseWrapper<?>) rawEntity;
			SearchResponseFilter.addLinkHeader(requestUri, res.getHeaders(), entity.getTotal(), entity.getLimit(),
					entity.getOffset());
		}
	}

	private static void addLinkHeader(String requestUri, MultivaluedMap<String, Object> headers, long total, int limit,
			int offset) {
		if (limit == -1 || total < limit) {
			return;
		}

		long lastOffset = total - (long) limit;

		UriBuilder builder = UriBuilder.fromUri(requestUri);
		builder.replaceQueryParam(KEY_OFFSET, offset + limit);
		builder.replaceQueryParam(KEY_LIMIT, limit);
		String next = builder.toTemplate();

		builder = UriBuilder.fromUri(requestUri);
		builder.replaceQueryParam(KEY_OFFSET, lastOffset);
		builder.replaceQueryParam(KEY_LIMIT, limit);
		String last = builder.toTemplate();

		String header = new StringBuilder()
				.append(String.format(LINK_HEADER_ITEM_FORMAT, next, REL_NEXT))
					.append(", ")
					.append(String.format(LINK_HEADER_ITEM_FORMAT, last, REL_LAST))
					.toString();

		headers.add(HttpHeaders.LINK, header);
	}
}
