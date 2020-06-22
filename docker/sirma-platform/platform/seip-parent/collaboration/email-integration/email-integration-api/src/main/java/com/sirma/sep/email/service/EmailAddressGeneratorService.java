package com.sirma.sep.email.service;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.sep.email.exception.EmailIntegrationException;

/**
 * Generates email addresses for instances.
 * 
 * @author svelikov
 */
public interface EmailAddressGeneratorService {

	/**
	 * Uses the definition of the provided instance to generate valid unique email address in the domain of the current
	 * tenant.
	 * 
	 * @param instance
	 *            The instance for which to be generated the email address.
	 * @return The generated email address.
	 * @throws EmailIntegrationException
	 *             If error occurs during email address generation.
	 */
	public String generateEmailAddress(Instance instance) throws EmailIntegrationException;

}
