package com.sirma.itt.seip.domain.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Base search DTO for map results.
 *
 * @param <C>
 *            is the type of the map key
 * @param <H>
 *            is the value type
 * @author borislav banchev
 */
public class SearchArgumentsMap<C, H> extends SearchArguments<C> {

	/**
	 * Instantiates a new search arguments.
	 */
	public SearchArgumentsMap() {
		// nothing to add
	}

	private Map<C, H> resultMap;

	/**
	 * Getter method for result.
	 *
	 * @return the resultMap
	 */
	public Map<C, H> getResultMap() {
		return resultMap;
	}

	/**
	 * Setter method for result.
	 *
	 * @param resultMap
	 *            the resultMap to set
	 */
	public void setResultMap(Map<C, H> resultMap) {
		this.resultMap = resultMap;
	}

	@Override
	public List<C> getResult() {
		if (result == null && resultMap != null) {
			return new ArrayList<C>(resultMap.keySet());
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder
				.append("SearchArgumentsMap [getResultMap()=")
					.append(getResultMap())
					.append(", getPageSize()=")
					.append(getPageSize())
					.append(", getSkipCount()=")
					.append(getSkipCount())
					.append(", getTotalItems()=")
					.append(getTotalItems())
					.append(", isOrdered()=")
					.append(isOrdered())
					.append("]");
		return builder.toString();
	}

}
