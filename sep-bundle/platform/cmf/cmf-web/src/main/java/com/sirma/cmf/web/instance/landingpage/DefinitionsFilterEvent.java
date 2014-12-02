package com.sirma.cmf.web.instance.landingpage;

import java.util.List;

import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Fired after all defintions are loaded and before to be displayed in combo box on the page. Allows
 * defintions to be filtered.
 * 
 * @author svelikov
 */
@Documentation("Fired after all defintions are loaded and before to be displayed in combo box on the page. Allows defintions to be filtered.")
public class DefinitionsFilterEvent implements EmfEvent {

	/** The definitions. */
	private List<?> definitions;

	/**
	 * Instantiates a new definitions filter event.
	 * 
	 * @param <D>
	 *            the generic type
	 * @param definitions
	 *            the definitions
	 */
	public <D extends DefinitionModel> DefinitionsFilterEvent(List<D> definitions) {
		this.definitions = definitions;
	}

	/**
	 * Gets the definitions.
	 * 
	 * @return the definitions
	 */
	public List<?> getDefinitions() {
		return definitions;
	}

	/**
	 * Sets the definitions.
	 * 
	 * @param <D>
	 *            the generic type
	 * @param definitions
	 *            the new definitions
	 */
	public <D extends DefinitionModel> void setDefinitions(List<D> definitions) {
		this.definitions = definitions;
	}
}
