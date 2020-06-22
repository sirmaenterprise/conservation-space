package com.sirma.itt.seip.definition.label;

import java.util.Map;
import java.util.Set;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.domain.Identity;

/**
 * Definition of a single label. The definition has unique identifier used to get the label definition and mapping of
 * properties. The properties keys are the language codes and the value is the label for that language.
 *
 * @author BBonev
 */
public interface LabelDefinition extends Entity<Long>, Identity {

	/**
	 * Gets the labels mapping. The keys are the supported label languages.
	 *
	 * @return the labels mapping.
	 */
	Map<String, String> getLabels();

	/**
	 * Set of identifiers where the current label definition could be found in.
	 *
	 * @return the set of definition identifiers where the current label is defined in.
	 */
	Set<String> getDefinedIn();
}
