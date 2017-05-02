package com.sirma.itt.seip.eai.cs.service.search;

import static com.sirma.itt.seip.eai.cs.EAIServicesConstants.SEARCH_INCLUDE_REFERENCES;
import static com.sirma.itt.seip.eai.cs.EAIServicesConstants.SEARCH_INCLUDE_THUMBNAILS;
import static com.sirma.itt.seip.eai.cs.EAIServicesConstants.SEARCH_INSTANTIATE_MISSING_INSTANCES;

import java.util.HashMap;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.eai.service.search.ExternalSystemSearchEngine;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.search.SearchEngine;

/**
 * Include CS specific code processing based on the {@link ExternalSystemSearchEngine} mechanism
 * 
 * @author bbanchev
 */
@ApplicationScoped
@Extension(target = SearchEngine.TARGET_NAME, order = 19, priority = 10)
public class CSExternalSystemSearch extends ExternalSystemSearchEngine {

	@Override
	public boolean prepareSearchArguments(SearchRequest request, SearchArguments<Instance> searchArguments) {
		if (super.prepareSearchArguments(request, searchArguments)) {
			if (searchArguments.getArguments() == null) {
				searchArguments.setArguments(new HashMap<>(3, 3));
			}
			// set the search settings for user search mode
			searchArguments.getArguments().putIfAbsent(SEARCH_INCLUDE_REFERENCES, Boolean.TRUE);
			searchArguments.getArguments().putIfAbsent(SEARCH_INSTANTIATE_MISSING_INSTANCES, Boolean.TRUE);
			searchArguments.getArguments().putIfAbsent(SEARCH_INCLUDE_THUMBNAILS, Boolean.TRUE);
			return true;
		}
		return false;
	}

}
