package com.sirma.cmf.web.userdashboard.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.ControlDefinition;
import com.sirma.itt.emf.definition.model.GenericDefinition;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.emf.search.model.SearchFilter;
import com.sirma.itt.emf.util.SortableComparator;

/**
 * DashletSearchSearchFilterProvider is responsible for loading dashlet filters from dashlet
 * definitions. TODO: to be removed
 * 
 * @author svelikov
 */
@Deprecated
@Named
@ApplicationScoped
public class DashletSearchFilterProvider {

	/** The Constant DASHLET_FILTER. */
	private static final String DASHLET_FILTER = "dashletFilter";

	/** The Constant SORT_FIELDS. */
	private static final String SORT_FIELDS = "sortFields";

	/** The dictionary service instance. */
	@Inject
	private DictionaryService dictionaryService;

	/** The label provider. */
	@Inject
	private LabelProvider labelProvider;

	/**
	 * Load search criteria for given dashlet from dashlet definition.
	 * 
	 * @param dashletPlaceholder
	 *            the dashlet placeholder
	 * @return the pair
	 */
	public Pair<List<SearchFilter>, List<SearchFilter>> loadSearchCriteria(String dashletPlaceholder) {
		List<SearchFilter> filters = new ArrayList<SearchFilter>();
		List<SearchFilter> sorters = new ArrayList<SearchFilter>();
		Pair<List<SearchFilter>, List<SearchFilter>> result = new Pair<List<SearchFilter>, List<SearchFilter>>(
				filters, sorters);
		if (StringUtils.isNullOrEmpty(dashletPlaceholder)) {
			return result;
		}
		GenericDefinition definition = dictionaryService.getDefinition(GenericDefinition.class,
				dashletPlaceholder);
		if (definition == null) {
			return result;
		}

		filters.addAll(loadFiltersForDashlet(definition));
		sorters.addAll(loadSortersForDashlet(definition));
		return result;
	}

	/**
	 * Load filters for dashlet.
	 * 
	 * @param definition
	 *            the definition
	 * @return the list
	 */
	private List<SearchFilter> loadFiltersForDashlet(DefinitionModel definition) {
		List<PropertyDefinition> fields = definition.getFields();
		Collections.sort(fields, new SortableComparator());
		List<SearchFilter> searchFilters = new ArrayList<SearchFilter>(10);
		for (PropertyDefinition propertyDefinition : fields) {
			ControlDefinition controlDefinition = propertyDefinition.getControlDefinition();
			if (controlDefinition != null) {
				String controlId = controlDefinition.getIdentifier();
				if (DASHLET_FILTER.equals(controlId)) {
					searchFilters.add(new SearchFilter(propertyDefinition.getName(),
							propertyDefinition.getLabel(), propertyDefinition.getTooltip(),
							propertyDefinition));
				}
			}
		}
		// set default filter flag to first filter
		if (!searchFilters.isEmpty()) {
			searchFilters.get(0).setDefault(true);
		}
		return searchFilters;
	}

	/**
	 * Load sorters for dashlet.
	 * 
	 * @param definition
	 *            the definition
	 * @return the list
	 */
	private List<SearchFilter> loadSortersForDashlet(DefinitionModel definition) {
		List<PropertyDefinition> fields = definition.getFields();
		Collections.sort(fields, new SortableComparator());
		List<SearchFilter> sorters = new ArrayList<SearchFilter>(10);
		for (PropertyDefinition propertyDefinition : fields) {
			ControlDefinition controlDefinition = propertyDefinition.getControlDefinition();
			if (controlDefinition != null) {
				String controlId = controlDefinition.getIdentifier();
				if (SORT_FIELDS.equals(controlId)) {
					List<PropertyDefinition> sorterDefinitions = controlDefinition.getFields();
					if (sorterDefinitions != null) {
						for (PropertyDefinition sorterDefinition : sorterDefinitions) {
							sorters.add(new SearchFilter(sorterDefinition.getName(),
									sorterDefinition.getLabel(), sorterDefinition.getTooltip(),
									sorterDefinition));
						}
						// we can have only one sort field inside a dashlet definition
						break;
					}
				}
			}
		}
		// set default sorter flag to first sorter
		if (!sorters.isEmpty()) {
			sorters.get(0).setDefault(true);
		}
		return sorters;
	}

}
