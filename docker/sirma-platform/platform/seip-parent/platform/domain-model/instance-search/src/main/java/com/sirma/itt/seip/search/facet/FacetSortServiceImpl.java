package com.sirma.itt.seip.search.facet;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.search.facet.Facet;
import com.sirma.itt.seip.domain.search.facet.FacetConfiguration;
import com.sirma.itt.seip.domain.search.facet.FacetSortOptions;
import com.sirma.itt.seip.domain.search.facet.FacetValue;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * Service for sorting facets and their values. The sorting depends on valid {@link FacetConfiguration} in the provided
 * {@link Facet}s.
 *
 * @author Mihail Radkov
 * @since 1.10.1
 */
public class FacetSortServiceImpl implements FacetSortService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public Collection<Facet> sort(Collection<Facet> facets) {
		for (Facet facet : facets) {
			TimeTracker tracker = TimeTracker.createAndStart();
			List<FacetValue> values = facet.getValues();
			if (CollectionUtils.isEmpty(values)) {
				continue;
			}

			FacetConfiguration facetConfig = facet.getFacetConfiguration();
			if (facetConfig == null) {
				throw new IllegalArgumentException("Facet configuration cannot be null.");
			}

			String sort = facetConfig.getSort();

			if (FacetSortOptions.SORT_ALPHABETICAL.getValue().equalsIgnoreCase(sort)) {
				values.sort((v1, v2) -> compareByText(v1, v2));
			} else if (FacetSortOptions.SORT_MATCH.getValue().equalsIgnoreCase(sort)) {
				values.sort((v1, v2) -> compareByCount(v1, v2));
			}

			if (FacetSortOptions.SORT_DESCENDING.getValue().equalsIgnoreCase(facetConfig.getSortOrder())) {
				Collections.reverse(facet.getValues());
			}
			LOGGER.trace("Facet id=[{}] sorted in {} ms.", facet.getId(), tracker.stop());
		}
		return facets
				.stream()
					.sorted((f1, f2) -> compareByOrder(f1, f2))
					.collect(Collectors.toCollection(ArrayList::new));
	}

	/**
	 * Compares the provided {@link FacetValue}s by their text labels.
	 *
	 * @param value1
	 *            - the first value for compare
	 * @param value2
	 *            - the second value for compare
	 * @return the compare flag calculated by {@link String#compareTo(String)}.
	 */
	private static int compareByText(FacetValue value1, FacetValue value2) {
		return value1.getText().compareTo(value2.getText());
	}

	/**
	 * Compares the provided {@link FacetValue}s by their counts.
	 *
	 * @param v1
	 *            - the first value for compare
	 * @param v2
	 *            - the second value for compare
	 * @return the compare flag calculated by {@link Long#compareTo(Long)}.
	 */
	private static int compareByCount(FacetValue v1, FacetValue v2) {
		return Long.compare(v1.getCount(), v2.getCount());
	}

	/**
	 * Compares the provided {@link Facet}s by the orders in their {@link FacetConfiguration}.
	 *
	 * @param v1
	 *            - the first facet for compare
	 * @param v2
	 *            - the second facet for compare
	 * @return the compare flag calculated by {@link Integer#compareTo(Integer)} .
	 */
	private static int compareByOrder(Facet facet1, Facet facet2) {
		Integer facet1Order = facet1.getFacetConfiguration().getOrder();
		Integer facet2Order = facet2.getFacetConfiguration().getOrder();
		return facet1Order.compareTo(facet2Order);
	}
}
