package com.sirma.itt.seip.definition.compile;

import java.util.List;

import com.sirma.itt.seip.Message;

/**
 * Defines methods for definition loading. The service provides methods for synchronous and asynchronous definition
 * update.
 *
 * @author BBonev
 */
public interface DefinitionLoader {

	/**
	 * Load definitions.
	 *
	 * @return the list of verification messages
	 */
	List<Message> loadDefinitions();
}
