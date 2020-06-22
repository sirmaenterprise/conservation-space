package com.sirma.itt.seip.eai.model.request.query;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * The {@link RawQuery} is holder for nested queries of type {@link QueryEntry}
 * 
 * @author bbanchev
 */
@JsonTypeName(value = "query")
public class RawQuery implements QueryEntry {
	private static final long serialVersionUID = 2039716791854524754L;
	@JsonIgnore
	private List<QueryEntry> entries = new LinkedList<>();

	/**
	 * Adds the entry to the group.
	 *
	 * @param readValue
	 *            the read value
	 */
	public void addEntry(QueryEntry readValue) {
		entries.add(readValue);
	}

	/**
	 * Gets the entries.
	 *
	 * @return the entries
	 */
	public List<QueryEntry> getEntries() {
		return entries;
	}

	@Override
	public String toString() {
		return QueryToString.toString(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entries == null) ? 0 : entries.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof RawQuery))
			return false;
		RawQuery other = (RawQuery) obj;
		if (entries == null) {
			if (other.entries != null)
				return false;
		} else if (!entries.equals(other.entries))
			return false;
		return true;
	}

}
