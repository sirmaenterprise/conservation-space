package com.sirma.itt.emf.search.event;

import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.domain.Context;
import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired after user has changed any search criteria. This event holds the search context
 * object with some default properties. The actual search arguments object is built after this
 * event.
 * 
 * @author svelikov
 */
@Documentation("Event fired after user has changed any search criteria. This event holds the search context object with some default properties. The actual search arguments object is built after this event.")
public class BeforeSearchQueryBuildEvent implements EmfEvent {

	private String filtername;

	private PropertyDefinition filterDefinition;

	private Context<String, Object> context;

	/**
	 * Instantiates a new before search query build event.
	 * 
	 * @param context
	 *            the context
	 * @param filtername
	 *            the filtername
	 * @param filterDefinition
	 *            the filter definition
	 */
	public BeforeSearchQueryBuildEvent(Context<String, Object> context, String filtername,
			PropertyDefinition filterDefinition) {
		this.context = context;
		this.filtername = filtername;
		this.filterDefinition = filterDefinition;
	}

	/**
	 * Getter method for context.
	 * 
	 * @return the context
	 */
	public Context<String, Object> getContext() {
		return context;
	}

	/**
	 * Setter method for context.
	 * 
	 * @param context
	 *            the context to set
	 */
	public void setContext(Context<String, Object> context) {
		this.context = context;
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

	/**
	 * Getter method for filterDefinition.
	 * 
	 * @return the filterDefinition
	 */
	public PropertyDefinition getFilterDefinition() {
		return filterDefinition;
	}

	/**
	 * Setter method for filterDefinition.
	 * 
	 * @param filterDefinition
	 *            the filterDefinition to set
	 */
	public void setFilterDefinition(PropertyDefinition filterDefinition) {
		this.filterDefinition = filterDefinition;
	}
}
