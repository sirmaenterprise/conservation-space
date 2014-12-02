package com.sirma.itt.emf.definition.load;

import java.util.List;

import com.sirma.itt.emf.domain.VerificationMessage;

/**
 * Defines methods for definition loading. The service provides methods for
 * synchronous and asynchronous definition update.
 *
 * @author BBonev
 */
public interface DefinitionLoader {

	/**
	 * Load template definitions.
	 * 
	 * @return the list of verification messages
	 */
	List<VerificationMessage> loadTemplateDefinitions();

	/**
	 * Load definitions.
	 * 
	 * @return the list of verification messages
	 */
	List<VerificationMessage> loadDefinitions();
}
