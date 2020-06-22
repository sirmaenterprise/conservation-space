package com.sirma.itt.seip.resources.mails;

import java.util.Collection;

import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Defines logic for users mails extraction.
 *
 * @author A. Kunchev
 */
public interface UsersMailExtractor {

	/**
	 * Extracts mails from passed collection of values, where they can be direct mails, users, groups, roles or
	 * properties for given instance. Every value is checked for its type, and then it is passed to specific method that
	 * can handle the extraction of the mail. For that purpose is used BiComputationChain.
	 *
	 * @param values
	 *            the values from which will be extracted mails
	 * @param instance
	 *            the instance from which will be extracted roles or properties, when it is needed
	 * @return collection of extracted mails
	 */
	Collection<String> extractMails(Collection<String> values, Instance instance);

}
