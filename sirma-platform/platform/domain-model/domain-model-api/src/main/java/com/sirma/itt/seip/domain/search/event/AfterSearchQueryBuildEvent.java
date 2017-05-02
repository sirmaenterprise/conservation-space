package com.sirma.itt.seip.domain.search.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.event.EmfEvent;

/**
 * Event fired after search arguments object is build in order to allow arguments update before the search to be
 * executed
 *
 * @author svelikov
 */
@Documentation("Event fired after search arguments object is build in order to allow arguments update before the search to be executed.")
public class AfterSearchQueryBuildEvent implements EmfEvent {

	private String filtername;

	/** The search arguments. */
	private SearchArguments<?> searchArguments;

	/**
	 * Instantiates a new after search query build event.
	 *
	 * @param searchArguments
	 *            the search arguments
	 * @param filtername
	 *            the filtername
	 */
	public AfterSearchQueryBuildEvent(SearchArguments<?> searchArguments, String filtername) {
		this.searchArguments = searchArguments;
		this.filtername = filtername;
	}

	/**
	 * Getter method for searchArguments.
	 *
	 * @return the searchArguments
	 */
	public SearchArguments<?> getSearchArguments() {
		return searchArguments;
	}

	/**
	 * Setter method for searchArguments.
	 *
	 * @param searchArguments
	 *            the searchArguments to set
	 */
	public void setSearchArguments(SearchArguments<?> searchArguments) {
		this.searchArguments = searchArguments;
	}

	/**
	 * Getter method for filtername.
	 *
	 * @return the filtername
	 */
	public String getFiltername() {
		return filtername;
	}

	/**
	 * Setter method for filtername.
	 *
	 * @param filtername
	 *            the filtername to set
	 */
	public void setFiltername(String filtername) {
		this.filtername = filtername;
	}

}
