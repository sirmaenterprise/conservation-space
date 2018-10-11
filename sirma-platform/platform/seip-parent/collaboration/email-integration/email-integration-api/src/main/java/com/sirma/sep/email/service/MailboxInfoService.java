package com.sirma.sep.email.service;

import com.sirma.sep.email.exception.EmailIntegrationException;

/**
 * Service used to extract specific information about given mailbox
 * 
 * @author S.Djulgerova
 */
public interface MailboxInfoService {

	/**
	 * Get count of unread messages in given mailbox (including external accounts)
	 * 
	 * @param accountName
	 *            - mailbox account name
	 * @return count of unread messages
	 * @throws EmailIntegrationException
	 */
	int getUnreadMessagesCount(String accountName) throws EmailIntegrationException;

}