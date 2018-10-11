package com.sirma.sep.account.administration;

import static com.sirma.email.ZimbraEmailIntegrationConstants.EMAILED_CONTACTS_FOLDER_NAME;
import static com.sirma.email.ZimbraEmailIntegrationConstants.ZIMBRA_ACCOUNT_STATUS;
import static com.sirma.sep.email.EmailIntegrationConstants.DISPLAY_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.FIRST_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.FULL_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.GIVEN_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.LAST_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.SN;
import static com.sirma.sep.email.EmailIntegrationConstants.TENANT_ADMIN_ACCOUNT_PREF;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.sep.email.EmailIntegrationConstants.EmailAccountStatus;
import com.sirma.sep.email.address.resolver.EmailAddress;
import com.sirma.sep.email.address.resolver.EmailAddressResolver;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.model.account.ContactInformation;
import com.sirma.sep.email.model.account.EmailAccountInformation;
import com.sirma.sep.email.model.account.FolderInformation;
import com.sirma.sep.email.model.account.GenericAttribute;
import com.sirma.sep.email.service.ContactsAdministrationService;
import com.sirma.sep.email.service.FolderAdministrationService;
import com.sirma.sep.email.service.ShareFolderAdministrationService;

/**
 * Zimbra SOAP share folder administration service.
 *
 * @author g.tsankov
 */
public class ShareFolderAdministrationServiceImpl implements ShareFolderAdministrationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ShareFolderAdministrationServiceImpl.class);

	@Inject
	private EmailAccountAdministrationServiceImpl accountAdministrationService;

	@Inject
	private FolderAdministrationService folderAdministrationService;

	@Inject
	private ContactsAdministrationService contactsAdministrationService;

	@Inject
	private EmailAddressResolver resolver;

	@Inject
	private SecurityContext securityContext;

	@Override
	public void createTenantShareFolder() throws EmailIntegrationException {
		String folderName = securityContext.getCurrentTenantId();
		try {
			folderAdministrationService.createFolder(getTenantEmail(), folderName, "contact");
			LOGGER.info("Successfully created tenant share folder {}.", folderName);
		} catch (EmailIntegrationException e) {
			throw new EmailIntegrationException("Error during share folder creation.", e);
		}
	}

	@Override
	public void shareFolderWithUser(String targetMail) throws EmailIntegrationException {
		try {
			addContactToShareFolder(targetMail);
			mountShareFolderToUser(targetMail);
			LOGGER.info("Successfully added and shared {} to the tenant share folder.", targetMail);
		} catch (EmailIntegrationException e) {
			throw new EmailIntegrationException("Error during share folder administration with user:" + targetMail, e);
		}
	}

	@Override
	public void addContactToShareFolder(String targetMail) throws EmailIntegrationException {
		try {
			String accountStatus = null;
			// create and add contact
			Map<String, String> contactAttrs = new HashMap<>();
			EmailAccountInformation info = accountAdministrationService.getAccount(targetMail,
					Arrays.asList(GIVEN_NAME, SN, DISPLAY_NAME, ZIMBRA_ACCOUNT_STATUS));
			for (GenericAttribute attr : info.getAttributes()) {
				switch (attr.getAttributeName()) {
				case GIVEN_NAME:
					contactAttrs.put(FIRST_NAME, attr.getValue());
					break;
				case SN:
					contactAttrs.put(LAST_NAME, attr.getValue());
					break;
				case DISPLAY_NAME:
					contactAttrs.put(FULL_NAME, attr.getValue());
					break;
				case ZIMBRA_ACCOUNT_STATUS:
					accountStatus = attr.getValue();
					break;
				default:
					break;
				}
			}
			String tenantAdminEmail = getTenantEmail();
			FolderInformation sharedFolder = getSharedTenantFolder();

			contactAttrs.put("email", targetMail);
			if (isShareFolderEligible(targetMail, accountStatus)) {
				contactsAdministrationService.createContact(contactAttrs, sharedFolder.getId(), tenantAdminEmail);
			} else {
				LOGGER.info("{} is not eligible to be added to the share folder.", targetMail);
			}
		} catch (EmailIntegrationException e) {
			throw new EmailIntegrationException("Error during contact creation for user:" + targetMail, e);
		}
	}

	@Override
	public void mountShareFolderToUser(String targetMail) throws EmailIntegrationException {
		String tenantAdminEmail = getTenantEmail();
		FolderInformation sharedFolder = getSharedTenantFolder();
		String accountStatus = accountAdministrationService.getAccount(targetMail, Arrays.asList(ZIMBRA_ACCOUNT_STATUS))
				.getAttributes().get(0).getValue();

		try {
			if (isShareFolderEligible(targetMail, accountStatus)) {
				folderAdministrationService.giveShareRightsToFolder(sharedFolder.getId(), targetMail, tenantAdminEmail);
				folderAdministrationService
						.createMountPointForFolder(
								targetMail, accountAdministrationService
										.getAccount(tenantAdminEmail, Collections.emptyList()).getAccountId(),
								sharedFolder.getName(), "contact", sharedFolder.getId());
				filterEmailedContactsFolder(targetMail);
			} else {
				LOGGER.info("{} is not eligible to have the share folder mounted.", targetMail);
			}
		} catch (EmailIntegrationException e) {
			throw new EmailIntegrationException("Error during mounting share folder with user:" + targetMail, e);
		}
	}

	@Override
	public void removeContactFromShareFolder(String targetMail) throws EmailIntegrationException {
		String tenantEmail = getTenantEmail();
		FolderInformation sharedFolder = getSharedTenantFolder();
		ContactInformation user;
		try {
			user = contactsAdministrationService.getContact(tenantEmail, targetMail, sharedFolder.getId());
			contactsAdministrationService.removeContactFromFolder(user.getId(), tenantEmail);
			LOGGER.info("Successfully removed user {} from the tenant share folder {}.", targetMail,
					sharedFolder.getName());
		} catch (EmailIntegrationException e) {
			throw new EmailIntegrationException("Error during removing user:" + targetMail + " from shared folder.", e);
		}
	}

	@Override
	public void deleteTenantShareFolder() throws EmailIntegrationException {
		FolderInformation tenantShareFolder = getSharedTenantFolder();
		String shareFolderId = tenantShareFolder.getId();
		if (shareFolderId != null) {
			String tenantEmail = getTenantEmail();
			try {
				String accountStatus = accountAdministrationService
						.getAccount(tenantEmail, Arrays.asList(ZIMBRA_ACCOUNT_STATUS)).getAttributes().get(0)
						.getValue();

				if (EmailAccountStatus.ACTIVE.getStatus().equalsIgnoreCase(accountStatus)) {
					folderAdministrationService.deleteFolder(tenantEmail, shareFolderId);
					LOGGER.info("Tenant share folder: {} deleted successfully!", tenantShareFolder.getName());
				} else {
					LOGGER.warn(
							"Skipping deletion of tenant share folder:{} , because tenant admin account is not in ACTIVE status",
							tenantShareFolder.getName());
				}
			} catch (EmailIntegrationException e) {
				throw new EmailIntegrationException("Error during share folder deletion", e);
			}
		}
	}

	@Override
	public boolean isShareFolderMounted(String targetMail) throws EmailIntegrationException {
		return folderAdministrationService.getFolder(targetMail, securityContext.getCurrentTenantId()).getId() != null;
	}

	/**
	 * Checks if account can be shared to share contacts folder. A contact can't be shared if it's the tenant admin or
	 * it's in inactive status.
	 *
	 * @param targetMail
	 *            contact mail address
	 * @param accountStatus
	 *            status if the contact.
	 * @return true if eligible.
	 */
	private boolean isShareFolderEligible(String targetMail, String accountStatus) {
		return !(getTenantEmail().equals(targetMail)
				|| !EmailAccountStatus.ACTIVE.getStatus().equalsIgnoreCase(accountStatus));
	}

	/**
	 * Constructs tenant email from configuration.
	 *
	 * @return tenant email address.
	 */
	private String getTenantEmail() {
		String tenantId = securityContext.getCurrentTenantId();
		return resolver.getEmailAddress(TENANT_ADMIN_ACCOUNT_PREF + tenantId, tenantId).getEmailAddress();
	}

	/**
	 * Retrieves the shared tenant contacts folder and caches it for future use if not already in the cache.
	 *
	 * @return Generic {@link FolderInformation} for shared folder instance.
	 * @throws EmailIntegrationException
	 *             if folder retrieval fails
	 */
	private FolderInformation getSharedTenantFolder() throws EmailIntegrationException {
		return folderAdministrationService.getFolder(getTenantEmail(), securityContext.getCurrentTenantId());
	}

	/**
	 * Filters the "Emailed Contacts" folder for specified account name, so that there are no duplicated suggested
	 * contacts.
	 *
	 * @param accountName
	 *            emailed contacts folder owner account name.
	 * @throws EmailIntegrationException
	 *             thrown if a problem occurs while working with the folder.
	 */
	private void filterEmailedContactsFolder(String accountName) throws EmailIntegrationException {
		FolderInformation emailedContactsFolder = folderAdministrationService.getFolder(accountName,
				EMAILED_CONTACTS_FOLDER_NAME);
		// get the list of current tenant email addresses, so its not queried for every iteration later.
		if (emailedContactsFolder.getId() != null) {
			List<EmailAddress> tenantEmailAddresses = resolver
					.getAllEmailsByTenant(securityContext.getCurrentTenantId());
			List<ContactInformation> emailedContacts = contactsAdministrationService.getAllFolderContacts(accountName,
					emailedContactsFolder.getId());
			for (ContactInformation contactInfo : emailedContacts) {
				for (GenericAttribute contactAttribute : contactInfo.getAttributes()) {
					if ("email".equals(contactAttribute.getAttributeName())
							&& isInTenantDomain(contactAttribute.getValue(), tenantEmailAddresses)) {
						contactsAdministrationService.removeContactFromFolder(contactInfo.getId(), accountName);
					}
				}
			}
		}
	}

	/**
	 * Checks if the email address is internal to the list of tenant domain addresses.
	 *
	 * @param emailAddress
	 *            emailAddress to be compared to the internal structure
	 * @param tenantEmailAddresses
	 *            supplied tenant domain email addresses.
	 *
	 * @return true if email is contained within the list.
	 */
	private static boolean isInTenantDomain(String emailAddress, List<EmailAddress> tenantEmailAddresses) {
		for (EmailAddress email : tenantEmailAddresses) {
			if (emailAddress.equals(email.getEmailAddress())) {
				return true;
			}
		}

		return false;
	}
}
