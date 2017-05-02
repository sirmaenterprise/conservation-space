package com.sirma.itt.seip.search.rest.handlers.readers;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.domain.search.tree.Condition;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.itt.seip.rest.utils.request.RequestInfo;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.search.converters.JsonToConditionConverter;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * {@link MessageBodyReader} for {@link SearchArguments} for {@link Instance instances}.
 *
 * @author yasko
 */
@Provider
@Consumes(Versions.V2_JSON)
public class SearchArgumentsReader implements MessageBodyReader<SearchArguments<Instance>> {

	@BeanParam
	private RequestInfo requestInfo;

	@Inject
	private SearchService searchService;

	@Inject
	private JsonToConditionConverter converter;

	@Override
	public boolean isReadable(Class<?> type, Type generic, Annotation[] annotations, MediaType media) {
		if (!ReflectionUtils.isTypeArgument(generic, Instance.class)) {
			return false;
		}
		return SearchArguments.class.isAssignableFrom(type);
	}

	@Override
	public SearchArguments<Instance> readFrom(Class<SearchArguments<Instance>> type, Type generic,
			Annotation[] annotations, MediaType media, MultivaluedMap<String, String> headers, InputStream in)
			throws IOException {

		Condition tree = JSON.readObject(in, converter::parseCondition);

		MultivaluedMap<String, String> queryParams = requestInfo.getUriInfo().getQueryParameters();
		SearchRequest searchRequest = new SearchRequest(CollectionUtils.createHashMap(headers.size() + queryParams.size()));
		searchRequest.setSearchTree(tree);
		searchRequest.getRequest().putAll(headers);
		searchRequest.getRequest().putAll(queryParams);
		return searchService.parseRequest(searchRequest);
	}

}
