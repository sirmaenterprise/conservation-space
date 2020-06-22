package com.sirma.itt.seip.definition;

import java.util.Collection;

import com.sirma.itt.seip.domain.Ordinal;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.DefaultProperties;

/**
 * Defines common methods used in definitions rest services.
 *
 * @author A. Kunchev
 */
public interface DefinitionHelper {

	/**
	 * Retrieves definition label from codelist.
	 *
	 * @param definition
	 *            the definition for which will be get label
	 * @return label or identifier if label is not found. May return null if the passed definition is null or if there
	 *         is no label specified for the codelist
	 */
	String getDefinitionLabel(DefinitionModel definition);

	/**
	 * Extracts the definition fields, sorting them by order attribute. Some of the fields are filtered, primary system.
	 *
	 * @see DefaultProperties#NON_REPRESENTABLE_FIELDS
	 * @param model
	 *            the definition model, which fields will be extracted
	 * @return definition fields sorted by order or empty collection if all the fields are filtered or the definition is
	 *         null
	 */
	Collection<Ordinal> collectAllFields(DefinitionModel model);

}
