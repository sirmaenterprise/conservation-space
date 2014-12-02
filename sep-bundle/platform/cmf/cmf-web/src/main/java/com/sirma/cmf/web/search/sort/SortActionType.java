package com.sirma.cmf.web.search.sort;

import com.sirma.cmf.web.search.SortAction;
import com.sirma.cmf.web.util.LabelConstants;
import com.sirma.itt.cmf.constants.CaseProperties;
import com.sirma.itt.emf.label.LabelProvider;

/**
 * Sort action types.
 * 
 * @author svelikov
 */
public enum SortActionType implements SortAction {

	/** */
	MODIFIED_ON(CaseProperties.MODIFIED_ON),
	/** */
	CASE_NUMBER(CaseProperties.UNIQUE_IDENTIFIER),
	/** */
	CASE_TYPE(CaseProperties.TYPE),
	/** */
	CASE_STATE(CaseProperties.STATUS),
	/** */
	CREATED_ON(CaseProperties.CREATED_ON),
	// /** */currently not supported
	// KEYWORDS(CaseProperties.KEYWORDS),
	/** */
	CREATED_FROM(CaseProperties.CREATED_BY);

	/**
	 * The sorter type.
	 */
	private String type;

	/**
	 * Constructor.
	 * 
	 * @param type
	 *            The sorter type.
	 */
	private SortActionType(String type) {
		this.type = type;
	}

	/**
	 * Get sorter type by name if exists.
	 * 
	 * @param type
	 *            Sorter type.
	 * @return {@link SortActionType}.
	 */
	public static SortActionType getSorterType(String type) {
		SortActionType[] availableTypes = values();
		for (SortActionType sortActionType : availableTypes) {
			if (sortActionType.type.equals(type)) {
				return sortActionType;
			}
		}

		return null;
	}

	/**
	 * Getter method for type.
	 * 
	 * @return the type
	 */
	@Override
	public String getType() {
		return type;
	}

	/**
	 * Setter method for type.
	 * 
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Getter method for label.
	 * 
	 * @param labelProvider
	 *            the label provider
	 * @return the label
	 */
	public String getLabel(LabelProvider labelProvider) {
		return labelProvider.getValue(LabelConstants.CASE_SEARCH_ARGS_PROPERTY_PREF + type);
	}

	@Override
	public String getLabel() {
		return null;
	}

}
