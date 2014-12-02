package com.sirma.cmf.web.caseinstance.discussion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;

import com.sirma.cmf.web.search.SearchConstants;
import com.sirma.cmf.web.search.SearchPageType;
import com.sirma.cmf.web.search.SearchTypeSelectedEvent;
import com.sirma.cmf.web.search.facet.FacetSearchAction;
import com.sirma.cmf.web.search.facet.FacetSearchFilter;
import com.sirma.cmf.web.search.facet.MessageFilterType;
import com.sirma.cmf.web.search.facet.event.SearchFilterUpdateEvent;
import com.sirma.cmf.web.search.facet.event.UpdatedSearchFilterBinding;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.emf.search.model.SearchArgumentsMap;

/**
 * MessageSearchAction backing bean.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class MessageSearchAction extends FacetSearchAction<Instance, SearchArguments<Instance>>
		implements Serializable {

	private static final long serialVersionUID = -4113123840967759062L;

	/**
	 * On search page selected.
	 * 
	 * @param event
	 *            the event
	 */
	public void onSearchPageSelected(
			@Observes @SearchPageType(SearchConstants.MESSAGE_SEARCH) SearchTypeSelectedEvent event) {

		onCreate();
	}

	@Override
	public String applySearchFilter(String filterType) {
		// Auto-generated method stub
		return null;
	}

	/**
	 * Getter method for messageFilters.
	 * 
	 * @return the messageFilters
	 */
	public List<FacetSearchFilter> getMessageFilters() {

		List<FacetSearchFilter> filters = new ArrayList<FacetSearchFilter>();
		filters.add(createFilter(MessageFilterType.ALL_MESSAGES.getFilterName(), true, true));

		SearchFilterUpdateEvent event = new SearchFilterUpdateEvent();
		event.setFacetSearchFilters(filters);

		UpdatedSearchFilterBinding updatedSearchFilterBinding = new UpdatedSearchFilterBinding(
				UpdatedSearchFilterBinding.MESSAGE);
		searchFilterUpdateEvent.select(updatedSearchFilterBinding).fire(event);

		return event.getFacetSearchFilters();
	}

	@Override
	protected String fetchResults() {
		// Auto-generated method stub
		return null;
	}

	@Override
	protected SearchArgumentsMap<Instance, SearchArguments<Instance>> initSearchData() {
		// Auto-generated method stub
		return null;
	}

	@Override
	protected Class<Instance> getEntityClass() {
		// Auto-generated method stub
		return null;
	}

}
