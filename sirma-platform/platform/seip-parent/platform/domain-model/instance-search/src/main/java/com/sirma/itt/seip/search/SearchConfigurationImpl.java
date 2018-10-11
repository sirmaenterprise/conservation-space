/**
 *
 */
package com.sirma.itt.seip.search;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;

/**
 * @author BBonev
 */
@ApplicationScoped
public class SearchConfigurationImpl implements SearchConfiguration {

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "search.result.maxsize", type = Integer.class, defaultValue = "10000", label = "Number of results to be fetched from the database.")
	private ConfigurationProperty<Integer> searchResultMaxSize;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "search.result.pager.pagesize", type = Integer.class, defaultValue = "25", label = "Datascroller configuration properties. Number of rows to be visible in the underlying list.")
	private ConfigurationProperty<Integer> pagerPageSize;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "search.result.pager.maxpages", type = Integer.class, defaultValue = "5", label = "Number of pages to be visible in datascroller.")
	private ConfigurationProperty<Integer> pagerMaxPages;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "dashlet.search.result.pager.pagesize", type = Integer.class, defaultValue = "25", label = "Maximum elements to be displayed in user dashlet.")
	private ConfigurationProperty<Integer> dashletPageSize;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "search.facet.result.exceed.disable", type = Boolean.class, defaultValue = "true", label = "Whether to disable facet calculation if search returns too many results (more than search results max size).")
	private ConfigurationProperty<Boolean> searchFacetResultExceedDisable;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "search.context.update", type = Boolean.class, defaultValue = "true", label = "Specifies if the search bar's context should be updated on view change (this is a web property). Default value is true.")
	private ConfigurationProperty<Boolean> updateSearchContext;

	/**
	 * @return the searchResultMaxSize
	 */
	@Override
	public Integer getSearchResultMaxSize() {
		return searchResultMaxSize.get();
	}

	/**
	 * Getter method for pagerPageSize.
	 *
	 * @return the pagerPageSize
	 */
	@Override
	public int getPagerPageSize() {
		return pagerPageSize.get().intValue();
	}

	/**
	 * Gets the dashlet page size.
	 *
	 * @return the dashlet page size
	 */
	@Override
	public Integer getDashletPageSize() {
		return dashletPageSize.get();
	}

	/**
	 * Getter method for pagerMaxPages.
	 *
	 * @return the pagerMaxPages
	 */
	@Override
	public int getPagerMaxPages() {
		return pagerMaxPages.get().intValue();
	}

	@Override
	public Boolean getSearchFacetResultExceedDisable() {
		return searchFacetResultExceedDisable.get();
	}

	@Override
	public Boolean updateSearchContext() {
		return updateSearchContext.get();
	}

}
