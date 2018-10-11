package com.sirma.itt.seip.domain.search;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.search.facet.Facet;
import com.sirma.itt.seip.domain.search.tree.Condition;

/**
 * Search object used to call the {@code com.sirma.itt.seip.search.SearchService} to perform search/query operation. On
 * of the important properties to fill are the {@link #setDialect(String)}. For optimal result consider using
 * {@link #setMaxSize(int)}, {@link #setQueryTimeout(TimeUnit, int)}.
 *
 * @param <E>
 *            is the type of the result
 * @author BBonev
 */
public class SearchArguments<E> {

	/** Current page number. The page numbers are starting from 1. */
	protected int pageNumber = 1;

	/** Number of results from a given search. */
	protected int resultSize;

	/** The page size. */
	protected int pageSize = 25;

	/** The skip count. */
	protected int skipCount = 0;

	/** The total items. */
	protected int totalItems = -1;
	/** max size for total items to retrieve on first search. */
	private int maxSize = 1000;
	/** The result. */
	protected List<E> result;

	/** The ordered. */
	protected boolean ordered = true;

	/** The sorter. */
	protected List<Sorter> sorter;

	/** the query. */
	protected Query query;

	/**
	 * String query. Contains Native SPARQL Query (from prepared queries) or Sirma advanced search query
	 */
	protected String stringQuery;

	/**
	 * Name of the prepared query if the query is prepared else it is null
	 */
	protected String queryName;

	/** The arguments. */
	protected Map<String, Serializable> arguments;

	/** Custom Query configurations such as include inferred */
	protected Map<String, Serializable> queryConfigurations;

	/** The context. */
	private String context;

	private int queryTimeout;

	/** The dialect. */
	private String dialect;

	/**
	 * The query projection if applicable for the current search engine or query mode.
	 */
	private String projection;

	/**
	 * When this is set to <code>true</code> we care only for the search count returned from the query and nothing else.
	 */
	private boolean countOnly = false;

	private QueryResultPermissionFilter permissionsType = QueryResultPermissionFilter.READ;

	// Facets
	/** Indicates if the search engine will populate the uriToTypeMapping needed for the facetting. */
	private boolean allFoundInstanceIds = false;

	/** Mapping between facets and their IDs. */
	private Map<String, Facet> facets;

	/** Ids (URIs) and instance types of all results returned from the search. */
	private Collection<Serializable> uries;

	private Map<String, Serializable> facetArguments;

	private List<Facet> facetsWithSelectedValues;

	/** Indicates whether the facet values should be retrieved */
	private boolean isFaceted = false;

	/**
	 * Indicates if the faceting should ignore the facet configurations and aggregate all fields, no matter if they have
	 * a configuration or not.
	 **/
	private boolean ignoreFacetConfiguration;

	private Condition condition;

	private Exception searchError;

	private boolean shouldGroupBy = false;

	private Collection<String> groupBy;

	private Map<String, Map<String, Serializable>> aggregated;

	/**
	 * Response variable for the instances to be highlighted in the UI
	 */
	private Collection<String> highlight;

	/**
	 * The Enum QueryResultPermissionFilter.
	 */
	public enum QueryResultPermissionFilter {
		/** Permission to READ */
		READ,
		/** Permission to WRITE */
		WRITE,
		/** No check */
		NONE
	}

	/**
	 * Initializes the {@link SearchArguments} object.
	 */
	public SearchArguments() {
		setQueryTimeout(TimeUnit.SECONDS, 180);
	}

	/**
	 * Getter method for facets.
	 *
	 * @return the facets
	 */
	public Map<String, Facet> getFacets() {
		return facets;
	}

	/**
	 * Setter method for facets.
	 *
	 * @param facets
	 *            the facets to set
	 */
	public void setFacets(Map<String, Facet> facets) {
		this.facets = facets;
	}

	/**
	 * Getter method for facetArguments.
	 *
	 * @return the facetArguments
	 */
	public Map<String, Serializable> getFacetArguments() {
		if (facetArguments == null) {
			facetArguments = new LinkedHashMap<>();
		}
		return facetArguments;
	}

	/**
	 * Setter method for facetArguments.
	 *
	 * @param facetArguments
	 *            the facetArguments to set
	 */
	public void setFacetArguments(Map<String, Serializable> facetArguments) {
		this.facetArguments = facetArguments;
	}

	/**
	 * Checks if the ignoreFacetConfiguration flag is set. It Indicates if the faceting should ignore the facet
	 * configurations and aggregate all fields, no matter if they have a configuration or not.
	 *
	 * @return the ignoreFacetConfiguration
	 */
	public boolean isIgnoreFacetConfiguration() {
		return ignoreFacetConfiguration;
	}

	/**
	 * Sets the ignoreFacetConfiguration flag.
	 *
	 * @param ignoreFacetConfiguration
	 *            the ignoreFacetConfiguration to set
	 */
	public void setIgnoreFacetConfiguration(boolean ignoreFacetConfiguration) {
		this.ignoreFacetConfiguration = ignoreFacetConfiguration;
	}

	/**
	 * Setter method for result.
	 *
	 * @param result
	 *            the result to set
	 */
	public void setResult(List<E> result) {
		this.result = result;
	}

	/**
	 * Getter method for result.
	 *
	 * @return the result
	 */
	public List<E> getResult() {
		return result;
	}

	/**
	 * Getter method for pageSize.
	 *
	 * @return the pageSize
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * Setter method for pageSize.
	 *
	 * @param pageSize
	 *            the pageSize to set
	 */
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	/**
	 * Getter method for skipCount.
	 *
	 * @return the skipCount
	 */
	public int getSkipCount() {
		return skipCount;
	}

	/**
	 * Setter method for skipCount.
	 *
	 * @param skipCount
	 *            the skipCount to set
	 */
	public void setSkipCount(int skipCount) {
		this.skipCount = skipCount;
	}

	/**
	 * Getter method for totalItems.
	 *
	 * @return the totalItems
	 */
	public int getTotalItems() {
		return totalItems;
	}

	/**
	 * Setter method for totalItems.
	 *
	 * @param totalItems
	 *            the totalItems to set
	 */
	public void setTotalItems(int totalItems) {
		this.totalItems = totalItems;
	}

	/**
	 * Setter method for ordered.
	 *
	 * @param ordered
	 *            the ordered to set
	 */
	public void setOrdered(boolean ordered) {
		this.ordered = ordered;
	}

	/**
	 * Getter method for ordered.
	 *
	 * @return the ordered
	 */
	public boolean isOrdered() {
		return ordered;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SearchArguments [pageNumber=");
		builder.append(pageNumber);
		builder.append(", sorter=");
		builder.append(sorter);
		builder.append(", stringQuery=");
		builder.append(stringQuery);
		builder.append(", queryName=");
		builder.append(queryName);
		builder.append(", dialect=");
		builder.append(dialect);
		builder.append(", projection=");
		builder.append(projection);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Adds the given sorter to the list of sorters
	 *
	 * @param sorterToAdd
	 *            the sorter to set
	 */
	public void addSorter(Sorter sorterToAdd) {
		if (sorterToAdd == null) {
			return;
		}
		if (this.sorter == null) {
			sorter = new LinkedList<>();
		}
		this.sorter.add(sorterToAdd);
	}

	/**
	 * Adds all of the given sorters to the list of sorters
	 *
	 * @param sortersToAdd
	 *            the sorters to set
	 */
	public void addSorters(Collection<Sorter> sortersToAdd) {
		if (sortersToAdd == null) {
			return;
		}
		if (this.sorter == null) {
			sorter = new LinkedList<>();
		}
		this.sorter.addAll(sortersToAdd);
	}

	/**
	 * Gets the first sorter object if any.
	 *
	 * @return the sorter or null
	 */
	public Sorter getFirstSorter() {
		if (CollectionUtils.isEmpty(sorter)) {
			return null;
		}
		return sorter.get(0);
	}

	/**
	 * Gets all sorters.
	 *
	 * @return the sorters
	 */
	public List<Sorter> getSorters() {
		if (sorter == null) {
			return Collections.emptyList();
		}
		return sorter;
	}

	/**
	 * Getter method for arguments.
	 *
	 * @return the arguments
	 */
	public Map<String, Serializable> getArguments() {
		if (arguments == null) {
			arguments = new LinkedHashMap<>();
		}
		return arguments;
	}

	/**
	 * Setter method for arguments.
	 *
	 * @param arguments
	 *            the arguments to set
	 */
	public void setArguments(Map<String, Serializable> arguments) {
		this.arguments = arguments;
	}

	/**
	 * Gets the query.
	 *
	 * @return the query
	 */
	public Query getQuery() {
		return query;
	}

	/**
	 * Sets the query.
	 *
	 * @param query
	 *            the query to set
	 */
	public void setQuery(Query query) {
		this.query = query;
	}

	/**
	 * Getter method for pageNumber.
	 *
	 * @return the pageNumber
	 */
	public int getPageNumber() {
		return pageNumber;
	}

	/**
	 * Setter method for pageNumber.
	 *
	 * @param pageNumber
	 *            the pageNumber to set
	 */
	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	/**
	 * Gets the max size for query results.
	 *
	 * @return the max size
	 */
	public int getMaxSize() {
		return maxSize;
	}

	/**
	 * Sets the parameter {@link #maxSize}. Default value is: 1000
	 *
	 * @param maxSize
	 *            the value to set
	 */
	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	/**
	 * @return the context
	 */
	public String getContext() {
		return context;
	}

	/**
	 * Sets the context (site name) to search under. This is foresaw for multitenancy search
	 *
	 * @param context
	 *            the context to set
	 */
	public void setContext(String context) {
		this.context = context;
	}

	/**
	 * Getter method for stringQuery.
	 *
	 * @return the stringQuery
	 */
	public String getStringQuery() {
		return stringQuery;
	}

	/**
	 * Setter method for stringQuery.
	 *
	 * @param stringQuery
	 *            the stringQuery to set
	 */
	public void setStringQuery(String stringQuery) {
		this.stringQuery = stringQuery;
	}

	/**
	 * Getter method for queryName.
	 *
	 * @return the queryName
	 */
	public String getQueryName() {
		return queryName;
	}

	/**
	 * Setter method for queryName.
	 *
	 * @param queryName
	 *            the queryName to set
	 */
	public void setQueryName(String queryName) {
		this.queryName = queryName;
	}

	/**
	 * Getter method for queryTimeout.
	 *
	 * @param unit
	 *            time unit in which the timeout should be represent.
	 * @return the queryTimeout
	 */
	public int getQueryTimeout(TimeUnit unit) {
		return (int) unit.convert(queryTimeout, TimeUnit.MILLISECONDS);
	}

	/**
	 * Setter method for queryTimeout in ms.
	 *
	 * @param unit
	 *            time unit in which the time in the value attribute is provided.
	 * @param value
	 *            the timeout to set
	 */
	public void setQueryTimeout(TimeUnit unit, int value) {
		this.queryTimeout = (int) unit.toMillis(value);
	}

	/**
	 * Gets the search dialect to use when selecting proper search engine to perform the search.
	 *
	 * @return the dialect
	 */
	public String getDialect() {
		return dialect;
	}

	/**
	 * Sets the search dialect to use when searching for search engine to execute the query.
	 *
	 * @param dialect
	 *            the new dialect
	 * @see com.sirma.itt.seip.domain.search.SearchDialects
	 */
	public void setDialect(String dialect) {
		this.dialect = dialect;
	}

	/**
	 * Gets the query projection if applicable for the current search engine or query mode.
	 *
	 * @return the projection
	 */
	public String getProjection() {
		return projection;
	}

	/**
	 * Sets the query projection if applicable for the current search engine or query mode.
	 *
	 * @param projection
	 *            the projection to set
	 */
	public void setProjection(String projection) {
		this.projection = projection;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 1;
		hash = prime * hash + (dialect == null ? 0 : dialect.hashCode());
		hash = prime * hash + pageNumber;
		hash = prime * hash + (projection == null ? 0 : projection.hashCode());
		hash = prime * hash + (queryName == null ? 0 : queryName.hashCode());
		hash = prime * hash + (sorter == null ? 0 : sorter.hashCode());
		hash = prime * hash + (stringQuery == null ? 0 : stringQuery.hashCode());
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof SearchArguments)) {
			return false;
		}
		SearchArguments<?> other = (SearchArguments<?>) obj;
		if (!nullSafeEquals(dialect, other.dialect)
				|| pageNumber != other.pageNumber
				|| !nullSafeEquals(projection, other.projection)) {
			return false;
		}
		return nullSafeEquals(queryName, other.queryName)
				&& nullSafeEquals(sorter, other.sorter)
				&& nullSafeEquals(stringQuery, other.stringQuery);
	}

	/**
	 * If this method returns true only the result count will be returned.
	 *
	 * @return the countOnly
	 */
	public boolean isCountOnly() {
		return countOnly;
	}

	/**
	 * When this is set to <code>true</code> we care only for the search count returned from the query and nothing else.
	 *
	 * @param countOnly
	 *            the countOnly to set
	 */
	public void setCountOnly(boolean countOnly) {
		this.countOnly = countOnly;
	}

	/**
	 * Getter method for shouldApplyPermissions.
	 *
	 * @return the shouldApplyPermissions
	 */
	public boolean shouldApplyPermissions() {
		return permissionsType != QueryResultPermissionFilter.NONE;
	}

	/**
	 * Getter method for getApplyPermissions.
	 *
	 * @return the type of the permissions
	 */
	public QueryResultPermissionFilter getPermissionsType() {
		return permissionsType;
	}

	/**
	 * Setter what type of permissions to be applied on results
	 *
	 * @param permissionsType
	 *            the type of permissions to set
	 */
	public void setPermissionsType(QueryResultPermissionFilter permissionsType) {
		QueryResultPermissionFilter permissionsTypeLocal = permissionsType;
		if (permissionsTypeLocal == null) {
			permissionsTypeLocal = QueryResultPermissionFilter.READ;
		}
		this.permissionsType = permissionsTypeLocal;
	}

	/**
	 * A collection of all uries found the search
	 *
	 * @return the uries
	 */
	public Collection<Serializable> getUries() {
		return uries;
	}

	/**
	 * Set the collection of all uries found during search. The instances will be used for faceting.
	 *
	 * @param uries
	 *            the new uri to type mapping
	 */
	public void setUries(Collection<Serializable> uries) {
		this.uries = uries;
	}

	/**
	 * Getter method for populateUriToTypeMapping.
	 *
	 * @return the populateUriToTypeMapping
	 */
	public boolean shouldReturnAllUries() {
		return allFoundInstanceIds;
	}

	/**
	 * Informs the search that it should return the ids for all instances that matches the search.
	 *
	 * @param allFoundInstanceId
	 *            the all found instance ids
	 */
	public void requestAllFoundInstanceIds(boolean allFoundInstanceId) {
		this.allFoundInstanceIds = allFoundInstanceId;
	}

	/**
	 * Check if it is faceted.
	 *
	 * @return true if it is
	 */
	public boolean isFaceted() {
		return isFaceted;
	}

	/**
	 * Setter method for retrieveFacetValues.
	 *
	 * @param isFaceted
	 *            set if it is faceted
	 */
	public void setFaceted(boolean isFaceted) {
		this.isFaceted = isFaceted;
	}

	/**
	 * Getter method for facetsWithSelectedValues.
	 *
	 * @return the facetsWithSelectedValues
	 */
	public List<Facet> getFacetsWithSelectedValues() {
		return facetsWithSelectedValues;
	}

	/**
	 * Setter method for facetsWithSelectedValues.
	 *
	 * @param facetsWithSelectedValues
	 *            the facetsWithSelectedValues to set
	 */
	public void setFacetsWithSelectedValues(List<Facet> facetsWithSelectedValues) {
		this.facetsWithSelectedValues = facetsWithSelectedValues;
	}

	/**
	 * @return the queryConfigurations
	 */
	public Map<String, Serializable> getQueryConfigurations() {
		if (queryConfigurations == null) {
			queryConfigurations = new LinkedHashMap<>();
		}
		return queryConfigurations;
	}

	/**
	 * @param queryConfigurations
	 *            the queryConfigurations to set
	 */
	public void setQueryConfigurations(Map<String, Serializable> queryConfigurations) {
		this.queryConfigurations = queryConfigurations;
	}

	/**
	 * Gets the search condition
	 *
	 * @return the condition
	 */
	public Condition getCondition() {
		return condition;
	}

	/**
	 * Sets the condition.
	 *
	 * @param searchTree
	 *            to set
	 */
	public void setCondition(Condition searchTree) {
		this.condition = searchTree;
	}

	/**
	 * Gets an associated with the search error. Might be null
	 *
	 * @return the associated with the search error
	 */
	public Exception getSearchError() {
		return searchError;
	}

	/**
	 * Sets the associated with the search error.
	 *
	 * @param searchError
	 *            the error to set
	 */
	public void setSearchError(Exception searchError) {
		this.searchError = searchError;
	}

	/**
	 * Returns the mapping for aggregated data - property to map of values and count.
	 *
	 * @return - the aggregated data map
	 */
	public Map<String, Map<String, Serializable>> getAggregatedData() {
		return aggregated;
	}

	/**
	 * Sets the mapping for aggregated data.
	 *
	 * @param aggregated
	 *            - the map
	 */
	public void setAggregated(Map<String, Map<String, Serializable>> aggregated) {
		this.aggregated = aggregated;
	}

	/**
	 * Returns the collection of properties for which search services should aggregate data.
	 *
	 * @return the properties for aggregating
	 */
	public Collection<String> getGroupBy() {
		return groupBy;
	}

	/**
	 * Sets the properties for which search services should aggregate results for.
	 *
	 * @param groupBy
	 *            - collection of properties
	 */
	public void setGroupBy(Collection<String> groupBy) {
		this.groupBy = groupBy;
	}

	/**
	 * Tells if search services should perform aggregation of data.
	 *
	 * @return true if yes or false otherwise
	 */
	public boolean shouldGroupBy() {
		return shouldGroupBy;
	}

	/**
	 * Sets if search services should perform data aggregation or not.
	 *
	 * @param shouldGroupBy
	 *            - true if yes, false otherwise
	 */
	public void setShouldGroupBy(boolean shouldGroupBy) {
		this.shouldGroupBy = shouldGroupBy;
	}

	/**
	 * Get non null collection that contains the instances that need to be highlighted in the UI.
	 *
	 * @return the instance identifiers that should be highlighted.
	 */
	public Collection<String> getHighlight() {
		if (highlight == null) {
			highlight = new LinkedList<>();
		}
		return highlight;
	}

	/**
	 * Sets the response variable for the instances to be highlighted in the UI. Note that the given identifiers should
	 * be present in the result in order to be highlighted.
	 *
	 * @param highlight the identifiers of the results to highlight
	 */
	public void setHighlight(Collection<String> highlight) {
		this.highlight = highlight;
	}
}
