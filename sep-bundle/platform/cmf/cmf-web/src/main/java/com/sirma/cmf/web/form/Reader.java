package com.sirma.cmf.web.form;

import java.util.List;
import java.util.Set;

import javax.faces.component.UIComponent;

import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.Sortable;
import com.sirma.itt.emf.properties.model.PropertyModel;

/**
 * Reader interface that should be implemented by the definition reader classes.
 * 
 * @author svelikov
 */
public interface Reader {

	/**
	 * Read property definitions from provided list to build a form. Provided list may contain as
	 * PropertyDefinition's as RegionDefinition's. PropertyDefinition's and RegionDefinition's are
	 * rendered on the root. The RegionDefinition's are rendered as panels and the fields from them
	 * are rendered as children of the panel.
	 * 
	 * @param sortables
	 *            the sortables
	 * @param definitionModel
	 *            the definition model
	 * @param propertyModel
	 *            the property model
	 * @param container
	 *            the container
	 * @param formViewMode
	 *            the form view mode
	 * @param rootInstanceName
	 *            the root instance name
	 * @param requiredFields
	 *            the list of required fields. If the set is empty then the only required fields
	 *            that will be on the form will be those marked as required in the each property
	 *            definition.
	 */
	void readSortables(List<Sortable> sortables, DefinitionModel definitionModel,
			PropertyModel propertyModel, UIComponent container, FormViewMode formViewMode,
			String rootInstanceName, Set<String> requiredFields);

}
