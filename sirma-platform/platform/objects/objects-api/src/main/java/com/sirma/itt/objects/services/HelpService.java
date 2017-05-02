package com.sirma.itt.objects.services;

import java.util.Map;

/**
 * Provides services for the contextual help logic.
 *
 * @author nvelkov
 */
public interface HelpService {

	/**
	 * Get the help instance id to target mapping. The target could be any type of object in the system or some specific
	 * ui component like widgets and search pages.
	 *
	 * @return the help instance id to target mapping
	 */
	Map<String, String> getHelpIdToTargetMapping();
}
