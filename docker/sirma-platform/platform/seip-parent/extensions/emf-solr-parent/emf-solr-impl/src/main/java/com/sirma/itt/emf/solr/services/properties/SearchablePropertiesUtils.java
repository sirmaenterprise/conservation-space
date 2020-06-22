package com.sirma.itt.emf.solr.services.properties;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.search.SearchableProperty;

/**
 * Provides utility methods for {@link SearchableProperty}.
 *
 * @author nvelkov
 */
public class SearchablePropertiesUtils {

	/**
	 * A private constructor that hides the implicit public one.
	 */
	private SearchablePropertiesUtils() {
		// Utility class.
	}

	/**
	 * Perform the operation from the mergeConsumer on each two equal {@link SearchableProperty} objects. Used for
	 * merging properties of the searchable properties e.g. merge their codelist collections.
	 *
	 * @param toBeModified
	 *            the list to be modified
	 * @param matchAgainst
	 *            the list to be matched against
	 * @param mergeConsumer
	 *            the operation that is going to be performed on each two equal searchable properties
	 */
	public static <C extends Collection<SearchableProperty>> void merge(C toBeModified, C matchAgainst,
			BiConsumer<SearchableProperty, SearchableProperty> mergeConsumer) {
		for (SearchableProperty propertyToBeModified : toBeModified) {
			for (SearchableProperty propertyToMatchAgainst : matchAgainst) {
				if (propertyToBeModified.equals(propertyToMatchAgainst)) {
					mergeConsumer.accept(propertyToBeModified, propertyToMatchAgainst);
					break;
				}
			}
		}
	}

	/**
	 * Perform the operation from the mergeConsumer on each two equal {@link SearchableProperty} objects. Used for
	 * merging properties of the searchable properties e.g. merge their codelist collections. <br>
	 * The method also combines the two collections by adding the elements of the second collection to the first.
	 *
	 * @param <C>
	 *            the generic type
	 * @param toBeModified
	 *            the list to be modified
	 * @param matchAgainst
	 *            the list to be matched against
	 * @param mergeConsumer
	 *            the operation that is going to be performed on each two equal searchable properties
	 * @return the combined set
	 */
	public static <C extends Collection<SearchableProperty>> C combine(C toBeModified, C matchAgainst,
			BiConsumer<SearchableProperty, SearchableProperty> mergeConsumer) {
		merge(toBeModified, matchAgainst, mergeConsumer);
		toBeModified.addAll(matchAgainst);
		return toBeModified;
	}

	/**
	 * Creates a deep copy of the collection by using the
	 * {@link SearchableProperty#SearchableProperty(SearchableProperty)} constructor.
	 *
	 * @param searchableProperties
	 *            the collection that is going to be copied
	 * @return a deep copy of the collection
	 */
	public static Set<SearchableProperty> clone(Collection<SearchableProperty> searchableProperties) {
		if (CollectionUtils.isNotEmpty(searchableProperties)) {
			return searchableProperties.stream().map(SearchableProperty::new).collect(Collectors.toSet());
		}
		return CollectionUtils.emptySet();
	}

	/**
	 * Adds or retains all elements from the target collection to the source collection.
	 *
	 * @param source
	 *            the collection where the elements are going to be added or retained
	 * @param target
	 *            the collection with the new elements
	 * @param retain
	 *            indicates whether the elements should be retained
	 */
	public static void addOrRetain(Collection<SearchableProperty> source, Collection<SearchableProperty> target,
			boolean retain) {
		if (retain && !source.isEmpty()) {
			source.retainAll(target);
		} else {
			source.addAll(target);
		}
	}

	/**
	 * Sort the collection according to the provided comparator.
	 *
	 * @param searchableProperties
	 *            collection to be sorted
	 * @param comparator
	 *            Comparator to be used to compare the elements of the collection
	 * @return the sorted collection
	 */
	public static List<SearchableProperty> sort(Collection<SearchableProperty> searchableProperties,
			Comparator<SearchableProperty> comparator) {
		return searchableProperties.stream().sorted(comparator).collect(Collectors.toList());
	}
}
