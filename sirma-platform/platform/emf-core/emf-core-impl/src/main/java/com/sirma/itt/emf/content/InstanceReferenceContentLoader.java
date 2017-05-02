package com.sirma.itt.emf.content;

import java.io.Serializable;

import javax.inject.Inject;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.content.ContentLoader;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchInstance;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.search.SearchService;

/**
 * Extension that can load content using search.
 *
 * @author BBonev
 */
@Extension(target = ContentLoader.TARGET_NAME, order = 10)
public class InstanceReferenceContentLoader implements ContentLoader {

	public static final String CONTENT_QUERY = "instanceQueries/loadInstanceContent";

	@Inject
	private SearchService searchService;

	@Override
	public boolean isApplicable(Object object) {
		return object instanceof InstanceReference;
	}

	@Override
	public String loadContent(Object object) {
		InstanceReference reference = (InstanceReference) object;
		Context<String, Object> context = new Context<>(2);
		context.put("instance", reference.getIdentifier());

		SearchArguments<SearchInstance> arguments = searchService.getFilter(CONTENT_QUERY, SearchInstance.class,
				context);

		searchService.search(Instance.class, arguments);

		if (CollectionUtils.isNotEmpty(arguments.getResult())) {
			Instance instance = arguments.getResult().get(0);
			if (instance.getProperties() != null) {
				Serializable serializable = instance.getProperties().get("content");
				if (serializable instanceof String) {
					return (String) serializable;
				}
			}
		}
		return null;
	}

}
