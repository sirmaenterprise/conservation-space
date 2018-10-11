package com.sirma.sep.account.administration;

import static com.sirma.sep.email.EmailIntegrationConstants.EMAIL;
import static com.sirma.sep.email.EmailIntegrationConstants.FIRST_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.FULL_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.LAST_NAME;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.xml.ws.soap.SOAPFaultException;

import com.sirma.email.ZimbraEmailIntegrationHelper;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.model.account.ContactInformation;
import com.sirma.sep.email.service.ContactsAdministrationService;

import zimbra.AttributeName;
import zimbramail.ContactActionRequest;
import zimbramail.ContactActionSelector;
import zimbramail.ContactInfo;
import zimbramail.ContactSpec;
import zimbramail.CreateContactRequest;
import zimbramail.GetContactsRequest;
import zimbramail.GetContactsResponse;
import zimbramail.NewContactAttr;

/**
 *
 * Zimbra SOAP contacts administration service.
 *
 * @author g.tsankov
 */
public class ContactsAdministrationServiceImpl implements ContactsAdministrationService {

	@Inject
	private AccountAuthenticationService authenticationService;

	@Override
	public void createContact(Map<String, String> contactAttributes, String folderId, String folderOwnerEmail)
			throws EmailIntegrationException {
		CreateContactRequest req = new CreateContactRequest();
		ContactSpec spec = new ContactSpec();
		spec.setL(folderId);
		List<NewContactAttr> attrs = spec.getA();
		attrs.add(createContactAttr(FULL_NAME, contactAttributes.getOrDefault(FULL_NAME, "")));
		attrs.add(createContactAttr(FIRST_NAME, contactAttributes.getOrDefault(FIRST_NAME, "")));
		attrs.add(createContactAttr(LAST_NAME, contactAttributes.getOrDefault(LAST_NAME, "")));
		attrs.add(createContactAttr(EMAIL, contactAttributes.get(EMAIL)));
		req.setCn(spec);

		try {
			authenticationService.getClientPort(folderOwnerEmail).createContactRequest(req);
		} catch (SOAPFaultException e) {
			throw new EmailIntegrationException("Contact creation failed for email:" + contactAttributes.get("email"),
					e);
		}
	}

	@Override
	public void removeContactFromFolder(String contactId, String contactOwnerMail) throws EmailIntegrationException {
		executeContactOperation(contactId, "delete", contactOwnerMail);
	}

	@Override
	public void executeContactOperation(String contactId, String operation, String contactOwnerMail)
			throws EmailIntegrationException {
		ContactActionRequest req = new ContactActionRequest();
		ContactActionSelector sel = new ContactActionSelector();
		sel.setId(contactId);
		sel.setOp(operation);
		req.setAction(sel);

		try {
			authenticationService.getClientPort(contactOwnerMail).contactActionRequest(req);
		} catch (SOAPFaultException e) {
			throw new EmailIntegrationException(operation + " operation on contact with id: " + contactId + " failed",
					e);
		}
	}

	@Override
	public ContactInformation getContact(String folderOwnerEmail, String searchedEmail, String folderId)
			throws EmailIntegrationException {
		GetContactsRequest req = new GetContactsRequest();
		req.setL(folderId);
		AttributeName emailAttr = new AttributeName();
		emailAttr.setN(EMAIL);
		req.getA().add(emailAttr);

		try {
			GetContactsResponse resp = authenticationService.getClientPort(folderOwnerEmail).getContactsRequest(req);
			return extractContact(searchedEmail, resp.getCn());
		} catch (SOAPFaultException e) {
			throw new EmailIntegrationException(
					"Search operation failed for target " + searchedEmail + " in the folder with id:" + folderId, e);
		}
	}

	@Override
	public List<ContactInformation> getAllFolderContacts(String folderOwnerEmail, String folderId)
			throws EmailIntegrationException {
		GetContactsRequest req = new GetContactsRequest();
		req.setL(folderId);
		try {
			GetContactsResponse resp = authenticationService.getClientPort(folderOwnerEmail).getContactsRequest(req);
			List<ContactInformation> contactsInFolder = new ArrayList<>(resp.getCn().size());

			for (ContactInfo info : resp.getCn()) {
				contactsInFolder.add(createGenericContact(info));
			}
			return contactsInFolder;
		} catch (SOAPFaultException e) {
			throw new EmailIntegrationException(
					"Search operation for all contacts failed for folder with id: " + folderId, e);
		}
	}

	/**
	 * Searches and returns {@link ContactInformation} for the specific target email or returns an empty
	 * {@link ContactInformation} if not found. It is asumed that only the "email" attribute is queried beforehand and
	 * only it will be in the attribute list for that contact.
	 *
	 * @param targetEmail
	 *            email address which is searched for.
	 * @param contactsList
	 *            list of {@link ContactInfo} to be searched.
	 * @return
	 */
	private static ContactInformation extractContact(String targetEmail, List<ContactInfo> contactsList) {
		Optional<ContactInfo> contact = contactsList.stream()
				.filter(contactInfo -> targetEmail.equals(contactInfo.getA().get(0).getValue())).findFirst();
		if (contact.isPresent()) {
			return createGenericContact(contact.get());
		}

		return new ContactInformation();
	}

	private static ContactInformation createGenericContact(ContactInfo zimbraContactInfo) {
		ContactInformation contactInfo = new ContactInformation();
		contactInfo.setId(zimbraContactInfo.getId());
		contactInfo.setAttributes(ZimbraEmailIntegrationHelper.toGenericAttributeList(zimbraContactInfo.getA()));
		return contactInfo;
	}

	/**
	 * Creates a {@link NewContactAttr} which is used in contact creation.
	 *
	 * @param n
	 *            attribute name
	 * @param val
	 *            attribute value
	 * @return {@link NewContactAttr} ready to be added for account creation.
	 */
	private static NewContactAttr createContactAttr(String n, String val) {
		NewContactAttr attr = new NewContactAttr();
		attr.setN(n);
		attr.setValue(val);
		return attr;
	}
}
