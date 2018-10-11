package com.sirma.sep.email.service;

/**
 * Services for checking and addition of mailbox supportable flags.
 * 
 * @author svelikov
 */
public interface MailboxSupportableService {

	/**
	 * Checks if given class supports mailbox (mailboxSupportable) by resolving it from the emf_mailboxsupportable
	 * table.
	 * 
	 * @param className
	 *            The class type.
	 * 
	 * @return if the class is mailboxSupportable=true
	 */
	boolean isMailboxSupportable(String className);

}