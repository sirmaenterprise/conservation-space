package com.sirma.itt.seip.domain.search;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.util.List;

import com.sirma.itt.seip.collections.CollectionUtils;

/**
 * The Class Sorter that holds all data needed for sorting.
 */
public class Sorter {

	/** The Constant SORT_ASCENDING. */
	public static final String SORT_ASCENDING = "asc";

	/** The Constant SORT_DESCENDING. */
	public static final String SORT_DESCENDING = "desc";

	/** The ascending order. */
	private boolean ascendingOrder = true;

	/** The sort field. */
	private final String sortField;

	/** The allow missing values for sorting */
	private boolean allowMissing = false;

	/**
	 * Codelist numbers to match the values and sort on the code value description
	 */
	private List<Integer> codelistNumbers;

	/**
	 * Marks the current sort field as one pointing to object property and not to data literal
	 */
	private boolean objectProperty = false;


	/**
	 * Ascending sorter.
	 *
	 * @param key
	 *            the key
	 * @return the sorter
	 */
	public static Sorter ascendingSorter(String key) {
		return new Sorter(key, SORT_ASCENDING);
	}

	/**
	 * Descending sorter.
	 *
	 * @param key
	 *            the key
	 * @return the sorter
	 */
	public static Sorter descendingSorter(String key) {
		return new Sorter(key, SORT_DESCENDING);
	}

	/**
	 * Builds the sorter from configuration property. The value could have the following format:
	 * <ul>
	 * <li>key
	 * <li>key|ASC
	 * <li>key|DESC
	 * </ul>
	 * .
	 *
	 * @param configValue
	 *            the config value
	 * @return the sorter or <code>null</code> if the parameter is null
	 */
	public static Sorter buildSorterFromConfig(String configValue) {
		if (configValue == null) {
			return null;
		}
		int indexOfPipe = configValue.indexOf('|');
		String key = configValue;
		String order = SORT_ASCENDING;
		if (indexOfPipe > 0) {
			key = key.substring(0, indexOfPipe);
			order = configValue.substring(indexOfPipe + 1).toLowerCase();
		}
		return new Sorter(key, order);
	}

	/**
	 * Instantiates a new sorter.
	 *
	 * @param key
	 *            the key
	 * @param isAscendingOrder
	 *            if the is ascending
	 */
	public Sorter(String key, boolean isAscendingOrder) {
		sortField = key;
		ascendingOrder = isAscendingOrder;
	}

	/**
	 * Instantiates a new sorter.
	 *
	 * @param key
	 *            the key
	 * @param isAscendingOrder
	 *            if the is ascending
	 * @param allowNull
	 *            the allow null
	 */
	public Sorter(String key, boolean isAscendingOrder, boolean allowNull) {
		sortField = key;
		ascendingOrder = isAscendingOrder;
		allowMissing = allowNull;
	}

	/**
	 * Instantiates a new sorter.
	 *
	 * @param key
	 *            the key
	 * @param order
	 *            the order
	 */
	public Sorter(String key, String order) {
		sortField = key;
		ascendingOrder = order == null || SORT_ASCENDING.equalsIgnoreCase(order);
	}
	
	/**
	 * Instantiates a new sorter
	 * 
	 * @param key
	 *            the key
	 * @param order
	 *           the order
	 * @param codelistNumbers
	 *            list of codelist numbers that the order by field can be
	 */
	public Sorter(String key, String order, List<Integer> codelistNumbers) {
		this(key, order);
		this.codelistNumbers = codelistNumbers;

	}

	/**
	 * Instantiates a new sorter
	 * 
	 * @param key
	 *            the key
	 * @param isAscendingOrder
	 *           if the sorting is in ascending order
	 * @param codelistNumbers
	 *            list of codelist numbers that the order by field can be
	 */
	public Sorter(String key, boolean isAscendingOrder, List<Integer> codelistNumbers) {
		this(key, isAscendingOrder);
		this.codelistNumbers = codelistNumbers;

	}

	/**
	 * Getter method for ascendingOrder.
	 *
	 * @return the ascendingOrder
	 */
	public boolean isAscendingOrder() {
		return ascendingOrder;
	}

	/**
	 * Setter method for ascendingOrder.
	 *
	 * @param ascendingOrder
	 *            the ascendingOrder to set
	 */
	public void setAscendingOrder(boolean ascendingOrder) {
		this.ascendingOrder = ascendingOrder;
	}

	/**
	 * Gets the sort field.
	 *
	 * @return the sortField
	 */
	public String getSortField() {
		return sortField;
	}

	/**
	 * Allow results where the sort field value is missing
	 */
	public void setAllowMissingValues() {
		allowMissing = true;
	}

	/**
	 * Checks if is missing values allowed to be returned by the search. By default missing values are removed from the
	 * search
	 *
	 * @return true, if is missing values are allowed
	 */
	public boolean isMissingValuesAllowed() {
		return allowMissing;
	}

	/**
	 * @return the isCodeList
	 */
	public boolean isCodeListValue() {
		return CollectionUtils.isNotEmpty(codelistNumbers);
	}

	/**
	 * @return the codeLists
	 */
	public List<Integer> getCodelistNumbers() {
		return codelistNumbers;
	}

	/**
	 * @param codeLists
	 *            the codeLists to set
	 */
	public void setCodelistNumbers(List<Integer> codeLists) {
		this.codelistNumbers = codeLists;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (ascendingOrder ? 1231 : 1237);
		result = prime * result + (sortField == null ? 0 : sortField.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Sorter)) {
			return false;
		}
		Sorter other = (Sorter) obj;
		return nullSafeEquals(sortField, other.sortField) && ascendingOrder == other.ascendingOrder;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Sorter [sortField=");
		builder.append(sortField);
		builder.append(", ascendingOrder=");
		builder.append(ascendingOrder);
		builder.append("]");
		return builder.toString();
	}

	public boolean isObjectProperty() {
		return objectProperty;
	}

	public Sorter setObjectProperty(boolean objectProperty) {
		this.objectProperty = objectProperty;
		return this;
	}
}
