package com.sirma.itt.seip.domain.search.facet;

/**
 * Serves as a {@link Facet} configuration POJO.
 *
 * @author nvelkov
 */
public class FacetConfiguration {

	/** Facet name */
	private String name;
	/**
	 * Order of the facet, specifying on which position the facet will be displayed.
	 */
	private Integer order;

	/**
	 * Indicates whether the facet should be hidden or not when more than one object type is displayed in the search
	 * results.
	 */
	private boolean isDefault;

	/**
	 * The sorting mechanism of the facet values.
	 */
	private String sort;

	/**
	 * The sorting order of the facet values.
	 */
	private String sortOrder;

	/**
	 * The page size for the facet values.
	 */
	private int pageSize;

	/**
	 * The label.
	 */
	private String label;

	/**
	 * The default state of the facet.
	 */
	private String state;

	/**
	 * Gets the state
	 *
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	/**
	 * Sets the state
	 *
	 * @param state
	 *            the state
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * Gets the order.
	 *
	 * @return the order
	 */
	public Integer getOrder() {
		return order;
	}

	/**
	 * Sets the order.
	 *
	 * @param order
	 *            the new order
	 */
	public void setOrder(Integer order) {
		this.order = order;
	}

	/**
	 * Checks if is default.
	 *
	 * @return true, if is default
	 */
	public boolean isDefault() {
		return isDefault;
	}

	/**
	 * Sets the default.
	 *
	 * @param isDefault
	 *            the new default
	 */
	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	/**
	 * Gets the sort.
	 *
	 * @return the sort
	 */
	public String getSort() {
		return sort;
	}

	/**
	 * Sets the sort.
	 *
	 * @param sort
	 *            the new sort
	 */
	public void setSort(String sort) {
		this.sort = sort;
	}

	/**
	 * Gets the sort order.
	 *
	 * @return the sort order
	 */
	public String getSortOrder() {
		return sortOrder;
	}

	/**
	 * Sets the sort order.
	 *
	 * @param sortOrder
	 *            the new sort order
	 */
	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}

	/**
	 * Gets the label.
	 *
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label.
	 *
	 * @param label
	 *            the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the page size.
	 *
	 * @return the pageSize
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * Sets the page size.
	 *
	 * @param pageSize
	 *            the pageSize to set
	 */
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

}
