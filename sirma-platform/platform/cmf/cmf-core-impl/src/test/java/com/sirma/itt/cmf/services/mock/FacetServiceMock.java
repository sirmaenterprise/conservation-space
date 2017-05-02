package com.sirma.itt.cmf.services.mock;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.search.facet.FacetService;

/**
 * Simple mock for {@link FacetService} used in CI tests.
 *
 * @author Mihail Radkov
 */
public class FacetServiceMock implements FacetService {

	@Override
	public <E extends Instance, S extends SearchArguments<E>> void prepareArguments(SearchRequest request,
			S searchArgs) {
		// Nothing to see here, move along.
	}

	@Override
	public <E extends Instance, S extends SearchArguments<E>> void facet(S arguments) {
		// Nothing to see here, move along.
	}

	@Override
	public <E extends Instance, S extends SearchArguments<E>> void filterObjectFacets(S arguments) {
		// Nothing to see here, move along.
	}

	@Override
	public <E extends Instance, S extends SearchArguments<E>> void assignLabels(S arguments) {
		// Nothing to see here, move along.
	}

	@Override
	public <E extends Instance, S extends SearchArguments<E>> void sort(S arguments) {
		// Nothing to see here, move along.
	}

	@Override
	public <E extends Instance, S extends SearchArguments<E>> S getAvailableFacets(SearchRequest request) {
		return null;
	}
}
