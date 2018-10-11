package com.sirma.itt.emf.semantic.queries;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import com.sirma.itt.seip.search.NamedQueries;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * SPARQL queries filter provider. Provides builds that can be used to produce builders for specific purposes. The
 * default filters are defined in {@link NamedQueries.Filters}
 *
 * @author BBonev
 */
@Singleton
public class SparqlQueryFilterProvider {

	/**
	 * Index used to create unique names for the filters that require checks
	 */
	private static final AtomicLong CHECK_INDEX = new AtomicLong(100);

	private static final String IS_DETELED = "\n%s " + EMF.PREFIX + ":" + EMF.IS_DELETED.getLocalName()
			+ " \"true\"^^xsd:boolean . ";

	private static final String IS_NOT_DETELED = "\n%s " + EMF.PREFIX + ":" + EMF.IS_DELETED.getLocalName()
			+ " \"false\"^^xsd:boolean . ";

	private static final String IS_NOT_REVISION = "\n\toptional {\n\t%s " + EMF.PREFIX + ":"
			+ EMF.REVISION_TYPE.getLocalName() + " " + EMF.PREFIX + ":" + EMF.TYPE_REVISION.getLocalName() + ".\n\t%s "
			+ EMF.PREFIX + ":" + EMF.IS_DELETED.getLocalName() + " ?check%s.\n\t}\n\tFILTER(! BOUND( ?check%s)).\n";

	private static final String IS_REVISION = "\n%s " + EMF.PREFIX + ":" + EMF.REVISION_TYPE.getLocalName() + " "
			+ EMF.PREFIX + ":" + EMF.TYPE_REVISION.getLocalName() + " . ";

	private static final Map<String, Function<String, String>> FILTERS;
	static {
		FILTERS = new HashMap<>();
		FILTERS.put(NamedQueries.Filters.IS_DELETED, var -> String.format(IS_DETELED, var));
		FILTERS.put(NamedQueries.Filters.IS_NOT_DELETED, var -> String.format(IS_NOT_DETELED, var));
		FILTERS.put(NamedQueries.Filters.IS_NOT_REVISION, var -> {
			long index = CHECK_INDEX.getAndIncrement();
			return String.format(IS_NOT_REVISION, var, var, index, index);
		});
		FILTERS.put(NamedQueries.Filters.IS_REVISION, var -> String.format(IS_REVISION, var));
		FILTERS.put(NamedQueries.Filters.SKIP, var -> "");
	}

	/**
	 * Gets the filter for the specified name or <code>null</code> if not such is defined. The builder argument is the
	 * name of the variable that this filter should apply for example: {@code ?instance}
	 *
	 * @param name
	 *            the name of the filter to look for
	 * @return the filter or <code>null</code> if no such is defined.
	 */
	public Function<String, String> getFilterBuilder(String name) {
		return FILTERS.get(name);
	}

	/**
	 * Gets the filters for the specified names or empty collection if non are found.
	 *
	 * @param filterNames
	 *            the filter names to resolve
	 * @return the filters or empty collection non are found.
	 * @see #getFilterBuilder(String)
	 */
	public List<Function<String, String>> getFilterBuilders(String... filterNames) {
		if (filterNames == null || filterNames.length == 0) {
			return Collections.emptyList();
		}
		return Arrays.asList(filterNames).stream().map(this::getFilterBuilder).filter(Objects::nonNull).collect(
				Collectors.toList());
	}
}
