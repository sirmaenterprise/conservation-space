package com.sirma.sep.email.rest;

import java.lang.invoke.MethodHandles;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.rest.utils.Versions;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.service.MailboxInfoService;

/**
 * Rest service for mailbox info extraction.
 * 
 * @author S.Djulgerova
 */
@Path("/mailbox")
@Produces(Versions.V2_JSON)
@Consumes(Versions.V2_JSON)
@ApplicationScoped
public class MailboxInfoRestService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private MailboxInfoService mailboxInfoService;

	/**
	 * Retrieve number of unread messages in given mailbox
	 * 
	 * @param accountName
	 *            - mailbox account name
	 * @return number of unread messages in given mailbox (including external accounts)
	 * @throws EmailIntegrationException
	 *             thrown if there's error in mailbox access
	 */
	@GET
	@Path("{name}/unread")
	public int getUnreadMessagesCount(@PathParam("name") String accountName) throws EmailIntegrationException {
		try {
			return mailboxInfoService.getUnreadMessagesCount(accountName);
		} catch (EmailIntegrationException e) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Could not fetch unread message count for {}", accountName, e);
			} else {
				LOGGER.warn("Could not fetch unread message count for {} due to: {}", accountName, e.getMessage());
			}
			// could not read the message count
			return -1;
		}
	}

}
