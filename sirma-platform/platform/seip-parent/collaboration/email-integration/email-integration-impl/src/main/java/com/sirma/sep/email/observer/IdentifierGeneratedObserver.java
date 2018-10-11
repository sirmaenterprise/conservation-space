package com.sirma.sep.email.observer;

import static com.sirma.sep.email.EmailIntegrationConstants.EMAIL_ADDRESS;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.event.IdentifierGeneratedEvent;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.service.EmailAddressGeneratorService;

/**
 * Listener for event fired after instance identifier is generated. Identifier might not be generated but the event is
 * fired in any case.
 * 
 * @author svelikov
 */
@Singleton
public class IdentifierGeneratedObserver {

	@Inject
	private EmailAddressGeneratorService emailAddressGeneratorService;


	/**
	 * After instance identifier is generated, then other properties that rely on it to be present can be generated as
	 * well.
	 * 
	 * @param event
	 *            The handled event
	 * @throws EmailIntegrationException
	 */
	public void onIdentifierGenerated(@Observes IdentifierGeneratedEvent event) throws EmailIntegrationException {
		Instance instance = event.getInstance();
		if (instance.type().isMailboxSupportable()) {
			String emailAddress = emailAddressGeneratorService.generateEmailAddress(instance);
			instance.add(EMAIL_ADDRESS, emailAddress);
		}
	}

}
