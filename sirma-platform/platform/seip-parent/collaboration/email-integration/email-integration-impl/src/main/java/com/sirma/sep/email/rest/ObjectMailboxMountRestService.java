package com.sirma.sep.email.rest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.service.ShareFolderAdministrationService;

/**
 * Rest service used object mailbox share folder mounting. Called when the object mailbox is initialized.
 *
 * @author g.tsankov
 */
@Path("/mailbox")
@ApplicationScoped
public class ObjectMailboxMountRestService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ObjectMailboxMountRestService.class);

	@Inject
	private ShareFolderAdministrationService shareFolderService;

	/**
	 * Checks if supplied email address is eligible to have the share folder mounted to it's mailbox.
	 *
	 * @param emailAddress
	 *            email address to be checked
	 * @throws EmailIntegrationException
	 *             if a error occurs while checking or mounting the shared folder.
	 */
	@POST
	@Path("{emailAddress}/mount")
	public void mountShareFolderIfNeeded(@PathParam("emailAddress") String emailAddress)
			throws EmailIntegrationException {
		if (!shareFolderService.isShareFolderMounted(emailAddress)) {
			shareFolderService.mountShareFolderToUser(emailAddress);
			LOGGER.info("Successfully shared tenant share folder with object with email address: {}", emailAddress);
		}
	}
}
