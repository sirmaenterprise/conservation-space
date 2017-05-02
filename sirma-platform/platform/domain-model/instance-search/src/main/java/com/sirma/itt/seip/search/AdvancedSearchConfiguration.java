package com.sirma.itt.seip.search;

import java.util.List;

import com.sirma.itt.seip.time.DateRangeConfig;

/**
 * Holds configuration related to the advanced search.
 *
 * @author Mihail Radkov
 */
public class AdvancedSearchConfiguration {

	private List<DateRangeConfig> dateRanges;

	/**
	 * Default constructor.
	 */
	public AdvancedSearchConfiguration() {
	}

	/**
	 * Constructs new configuration with the provided date ranges.
	 *
	 * @param dateRanges
	 *            the provided date ranges
	 */
	public AdvancedSearchConfiguration(List<DateRangeConfig> dateRanges) {
		this.dateRanges = dateRanges;
	}

	/**
	 * Getter method for dateRanges.
	 *
	 * @return the dateRanges
	 */
	public List<DateRangeConfig> getDateRanges() {
		return dateRanges;
	}

	/**
	 * Setter method for dateRanges.
	 *
	 * @param dateRanges
	 *            the dateRanges to set
	 */
	public void setDateRanges(List<DateRangeConfig> dateRanges) {
		this.dateRanges = dateRanges;
	}

}
