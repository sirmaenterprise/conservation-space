package com.sirma.itt.seip.rule.util;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchInstance;
import com.sirma.itt.seip.search.ResultItemTransformer;
import com.sirma.itt.seip.search.SearchService;

/**
 * Provides means to load the content for an instance for content matching used in the rules
 *
 * @author BBonev
 */
public class ContentLoader {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String CONTENT_QUERY = "instanceQueries/loadInstanceContent";

	@Inject
	private SearchService searchService;

	/**
	 * Load all content for an instance identified by it's id. The argument could be instance id, Instance or
	 * InstanceReference
	 *
	 * @param object the identifier to load the content for
	 * @return the all instance contents concatenated in a single content
	 */
	public String loadContent(Object object) {
		Serializable id = null;
		if (object instanceof InstanceReference) {
			id = ((InstanceReference) object).getId();
		} else if (object instanceof Instance) {
			id = ((Instance) object).getId();
		} else if (object instanceof String) {
			id = (String) object;
		}
		if (id == null) {
			return null;
		}

		Context<String, Object> context = new Context<>(2);
		context.put("instance", id);
		SearchArguments<SearchInstance> arguments = searchService.getFilter(CONTENT_QUERY, SearchInstance.class,
				context);
		if (arguments == null) {
			LOGGER.warn("Could not fetch instance {} content. Query not found: {}", id, CONTENT_QUERY);
			return null;
		}

		String content = searchService.stream(arguments, ResultItemTransformer.asSingleValue("content"))
				.map(Object::toString)
				.map(StringUtils::trimToEmpty)
				.collect(Collectors.joining(""));
		return StringUtils.trimToNull(content);
	}
}
