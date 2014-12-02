package com.sirma.itt.pm.services;

import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * The Interface UUIDService is responsible for generating id needed by projects
 * and any other schedule elements.
 */
public interface UUIDService {

	/**
	 * Generate project id by the specification.
	 *
	 * @param projectInstance
	 *            the project instance to generate for
	 * @return the id generated
	 */
	String generateProjectId(ProjectInstance projectInstance);

}
