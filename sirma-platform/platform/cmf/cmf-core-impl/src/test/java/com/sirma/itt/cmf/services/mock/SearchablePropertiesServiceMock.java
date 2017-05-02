package com.sirma.itt.cmf.services.mock;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchableProperty;
import com.sirma.itt.seip.search.SearchablePropertiesService;

/**
 * Simple mock for {@link SearchablePropertiesService} used in CI tests.
 *
 * @author Mihail Radkov
 */
public class SearchablePropertiesServiceMock implements SearchablePropertiesService {

	@Override
	public void reset() {
	}

	@Override
	public List<SearchableProperty> getSearchableSolrProperties(String forType, Boolean commonOnly, Boolean multiValued,
			Boolean skipObjectProperties) {
		return Collections.emptyList();
	}

	@Override
	public List<SearchableProperty> getSearchableSemanticProperties(String forType) {
		return Collections.emptyList();
	}

	@Override
	public Map<String, List<PropertyDefinition>> getTypeFields(Instance instance, String type, String definitionId) {
		return Collections.emptyMap();
	}

	@Override
	public Optional<SearchableProperty> getSearchableProperty(String forType, String propertyId) {
		return null;
	}

}
