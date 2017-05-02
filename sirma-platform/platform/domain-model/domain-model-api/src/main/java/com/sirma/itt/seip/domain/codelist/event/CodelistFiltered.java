package com.sirma.itt.seip.domain.codelist.event;

import java.util.Map;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.event.EmfEvent;

/**
 * Fired for additional codelist filtering
 *
 * @author BBonev
 */
@Documentation("Fired for additional codelist filtering")
public class CodelistFiltered implements EmfEvent {

	/** The values. */
	private Map<String, CodeValue> values;

	/**
	 * Instantiates a new codelist filtered.
	 */
	public CodelistFiltered() {
		// nothing to do here
	}

	/**
	 * Instantiates a new codelist filtered.
	 *
	 * @param values
	 *            the values
	 */
	public CodelistFiltered(Map<String, CodeValue> values) {
		this.values = values;
	}

	/**
	 * Getter method for values.
	 *
	 * @return the values
	 */
	public Map<String, CodeValue> getValues() {
		return values;
	}

	/**
	 * Setter method for values.
	 *
	 * @param values
	 *            the values to set
	 */
	public void setValues(Map<String, CodeValue> values) {
		this.values = values;
	}
}
