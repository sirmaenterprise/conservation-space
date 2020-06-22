package com.sirma.sep.account.administration;
import static com.sirma.email.ZimbraEmailIntegrationConstants.EMAILED_CONTACTS_FOLDER_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.DISPLAY_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.EMAIL;
import static com.sirma.sep.email.EmailIntegrationConstants.FIRST_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.FULL_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.GIVEN_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.LAST_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.SN;
import static com.sirma.sep.email.EmailIntegrationConstants.TENANT_ADMIN_ACCOUNT_PREF;
import static com.sirma.sep.email.EmailIntegrationConstants.ZIMBRA_ACCOUNT_STATUS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.sep.email.address.resolver.EmailAddress;
import com.sirma.sep.email.address.resolver.EmailAddressResolver;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.model.account.ContactInformation;
import com.sirma.sep.email.model.account.EmailAccountInformation;
import com.sirma.sep.email.model.account.FolderInformation;
import com.sirma.sep.email.model.account.GenericAttribute;

public class ShareFolderAdministrationServiceImplTest {

	private static final String DOMAIN_NAME = "tenant-domain.com";
	private static final String TENANT_ADMIN_ID = "tenant-admin-email-id";
	private static final String TENANT_ADMIN_EMAIL = "tenant-admin@" + DOMAIN_NAME;
	private static final String TEST_USER_ID = "test-user-id";
	private static final String TEST_USER_NAME = "test-user";
	private static final String TEST_USER_MAIL = TEST_USER_NAME + "@" + DOMAIN_NAME;
	private static final String SHARED_FOLDER_ID = "shared-folder-id";
	private static final String SHARED_FOLDER_NAME = "shared folder";
	private static final String CONTACTS_FOLDER_NAME = "contact";

	@InjectMocks
	private ShareFolderAdministrationServiceImpl service;

	@Mock
	private EmailAccountAdministrationServiceImpl accountAdministrationService;

	@Mock
	private EmailAddressResolver resolver;

	@Mock
	private FolderAdministrationServiceImpl folderAdministrationService;

	@Mock
	private ContactsAdministrationServiceImpl contactsAdministrationService;

	@Mock
	private SecurityContext securityContext;

	@Before
	public void setup() throws EmailIntegrationException {
		FolderInformation sharedFolder = new FolderInformation();
		sharedFolder.setId(SHARED_FOLDER_ID);
		sharedFolder.setName(SHARED_FOLDER_NAME);

		service = mock(ShareFolderAdministrationServiceImpl.class);
		MockitoAnnotations.initMocks(this);

		when(securityContext.getCurrentTenantId()).thenReturn(TENANT_ADMIN_ID);
		when(resolver.getEmailAddress(TENANT_ADMIN_ACCOUNT_PREF + TENANT_ADMIN_ID, TENANT_ADMIN_ID))
				.thenReturn(new EmailAddress(TENANT_ADMIN_ID, TENANT_ADMIN_ACCOUNT_PREF + TENANT_ADMIN_ID,
						TENANT_ADMIN_EMAIL, "tenant-domain.com"));
		when(accountAdministrationService.getAccount(TENANT_ADMIN_EMAIL, Collections.emptyList()))
				.thenReturn(createAccountInformation(TENANT_ADMIN_ID, TENANT_ADMIN_EMAIL,
						Lists.newArrayList(new GenericAttribute(DISPLAY_NAME, "Tenant Admin"))));
		when(folderAdministrationService.getFolder(TENANT_ADMIN_EMAIL, TENANT_ADMIN_ID)).thenReturn(sharedFolder);
	}

	@Test
	public void shouldCreateProperContactShareFolder() throws EmailIntegrationException {
		ArgumentCaptor<String> createFolderCaptor = ArgumentCaptor.forClass(String.class);
		doCallRealMethod().when(service).createTenantShareFolder();
		service.createTenantShareFolder();
		verify(folderAdministrationService, times(1)).createFolder(createFolderCaptor.capture(),
				createFolderCaptor.capture(), createFolderCaptor.capture());
		List<String> capturedArgs = createFolderCaptor.getAllValues();
		assertEquals(TENANT_ADMIN_EMAIL, capturedArgs.get(0));
		assertEquals(TENANT_ADMIN_ID, capturedArgs.get(1));
		assertEquals(CONTACTS_FOLDER_NAME, capturedArgs.get(2));
	}

	/**
	 * A new contact should be created to the shared folder, share rights should be given and mountpoint created to the
	 * user account.
	 *
	 * @throws EmailIntegrationException
	 */
	@Test
	public void shouldShareFolderWithUser() throws EmailIntegrationException {
		String mailToRemove = "test-email@domain.com";
		String userEmailedFolderId = "folder-id";
		doCallRealMethod().when(service).addContactToShareFolder(TEST_USER_MAIL);
		doCallRealMethod().when(service).mountShareFolderToUser(TEST_USER_MAIL);
		when(accountAdministrationService.getAccount(TEST_USER_MAIL,
				Arrays.asList(GIVEN_NAME, SN, DISPLAY_NAME, ZIMBRA_ACCOUNT_STATUS)))
						.thenReturn(createAccountInformation(TEST_USER_ID, TEST_USER_MAIL,
								Lists.newArrayList(new GenericAttribute(GIVEN_NAME, "Test User"),
										new GenericAttribute("notUsedAttr", "notUsedVal"),
										new GenericAttribute(ZIMBRA_ACCOUNT_STATUS, "active"),
										new GenericAttribute("notUsedAttr", "notUsedVal"),
										new GenericAttribute(SN, "Display Name"),
										new GenericAttribute(DISPLAY_NAME, "Test User Display Name"),
										new GenericAttribute("notUsedAttr#2", "notUsedVal#2"))));
		when(accountAdministrationService.getAccount(TEST_USER_MAIL, Arrays.asList(ZIMBRA_ACCOUNT_STATUS)))
				.thenReturn(createAccountInformation(TEST_USER_ID, TEST_USER_MAIL,
						Lists.newArrayList(new GenericAttribute(ZIMBRA_ACCOUNT_STATUS, "active"))));
		when(resolver.getAllEmailsByTenant(TENANT_ADMIN_ID))
				.thenReturn(Arrays.asList(new EmailAddress(null, null, mailToRemove, null),
						new EmailAddress(null, null, "test-email2@domain.com", null)));
		ContactInformation returnedContact = new ContactInformation("contact-info",
				Arrays.asList(new GenericAttribute("email", mailToRemove)));
		when(contactsAdministrationService.getAllFolderContacts(TEST_USER_MAIL, userEmailedFolderId))
				.thenReturn(Arrays.asList(returnedContact));
		when(folderAdministrationService.getFolder(TEST_USER_MAIL,EMAILED_CONTACTS_FOLDER_NAME))
				.thenReturn(new FolderInformation("folder-id", EMAILED_CONTACTS_FOLDER_NAME, 2, Collections.emptyList()));
		doCallRealMethod().when(service).shareFolderWithUser(TEST_USER_MAIL);

		service.shareFolderWithUser(TEST_USER_MAIL);
		Map<String, String> expectedMap = new HashMap<>();
		expectedMap.put(FIRST_NAME, "Test User");
		expectedMap.put(LAST_NAME, "Display Name");
		expectedMap.put(EMAIL, TEST_USER_MAIL);
		expectedMap.put(FULL_NAME, "Test User Display Name");
		/* contact should be created using the shared folder id, target mail address and tenant mail address. */
		verify(contactsAdministrationService, times(1)).createContact(expectedMap, SHARED_FOLDER_ID,
				TENANT_ADMIN_EMAIL);
		/* share rights must be given to the share folder id, to the user mail */
		verify(folderAdministrationService, times(1)).giveShareRightsToFolder(SHARED_FOLDER_ID, TEST_USER_MAIL,
				TENANT_ADMIN_EMAIL);
		/*
		 * client mountpoint should be created using the targetEmail, targetId, folderName, contact view, shared folder
		 * and folderId
		 */
		verify(folderAdministrationService, times(1)).createMountPointForFolder(TEST_USER_MAIL, TENANT_ADMIN_ID,
				SHARED_FOLDER_NAME, CONTACTS_FOLDER_NAME, SHARED_FOLDER_ID);
		// duplicated emailed contact should be removed
		verify(contactsAdministrationService, times(1)).removeContactFromFolder("contact-info", TEST_USER_MAIL);
	}

	@Test
	public void shouldShareFolderWithUserTwoMethods() throws EmailIntegrationException {

		when(accountAdministrationService.getAccount(TEST_USER_MAIL,
				Arrays.asList(GIVEN_NAME, SN, DISPLAY_NAME, ZIMBRA_ACCOUNT_STATUS)))
						.thenReturn(createAccountInformation(TEST_USER_ID, TEST_USER_MAIL,
								Lists.newArrayList(new GenericAttribute(GIVEN_NAME, "Test User"),
										new GenericAttribute("notUsedAttr", "notUsedVal"),
										new GenericAttribute(ZIMBRA_ACCOUNT_STATUS, "active"),
										new GenericAttribute("notUsedAttr", "notUsedVal"),
										new GenericAttribute(SN, "Display Name"),
										new GenericAttribute(DISPLAY_NAME, "Test User Display Name"),
										new GenericAttribute("notUsedAttr#2", "notUsedVal#2"))));
		when(accountAdministrationService.getAccount(TEST_USER_MAIL, Arrays.asList(ZIMBRA_ACCOUNT_STATUS)))
				.thenReturn(createAccountInformation(TEST_USER_ID, TEST_USER_MAIL,
						Lists.newArrayList(new GenericAttribute(ZIMBRA_ACCOUNT_STATUS, "active"))));
		when(resolver.getAllEmailsByTenant(TENANT_ADMIN_ID))
				.thenReturn(Arrays.asList(new EmailAddress(null, null, "test-email2@domain.com", null),
						new EmailAddress(null, null, "test-email1@domain.com", null)));
		ContactInformation returnedContact = new ContactInformation("contact-info",
				Arrays.asList(new GenericAttribute(DISPLAY_NAME, "Test User Display Name"),
						new GenericAttribute("email", "test-email@domain.com")));
		when(contactsAdministrationService.getAllFolderContacts(TEST_USER_MAIL, "folder-id"))
				.thenReturn(Arrays.asList(returnedContact));
		when(folderAdministrationService.getFolder(TEST_USER_MAIL, EMAILED_CONTACTS_FOLDER_NAME))
				.thenReturn(new FolderInformation("folder-id", EMAILED_CONTACTS_FOLDER_NAME, 2, Collections.emptyList()));
		doCallRealMethod().when(service).shareFolderWithUser(TEST_USER_MAIL);

		doCallRealMethod().when(service).addContactToShareFolder(TEST_USER_MAIL);
		doCallRealMethod().when(service).mountShareFolderToUser(TEST_USER_MAIL);
		service.addContactToShareFolder(TEST_USER_MAIL);
		service.mountShareFolderToUser(TEST_USER_MAIL);
		Map<String, String> expectedMap = new HashMap<>();
		expectedMap.put(FIRST_NAME, "Test User");
		expectedMap.put(LAST_NAME, "Display Name");
		expectedMap.put(EMAIL, TEST_USER_MAIL);
		expectedMap.put(FULL_NAME, "Test User Display Name");
		/* contact should be created using the shared folder id, target mail address and tenant mail address. */
		verify(contactsAdministrationService, times(1)).createContact(expectedMap, SHARED_FOLDER_ID,
				TENANT_ADMIN_EMAIL);
		/* share rights must be given to the share folder id, to the user mail */
		verify(folderAdministrationService, times(1)).giveShareRightsToFolder(SHARED_FOLDER_ID, TEST_USER_MAIL,
				TENANT_ADMIN_EMAIL);
		/*
		 * client mountpoint should be created using the targetEmail, targetId, folderName, contact view, shared folder
		 * and folderId
		 */
		verify(folderAdministrationService, times(1)).createMountPointForFolder(TEST_USER_MAIL, TENANT_ADMIN_ID,
				SHARED_FOLDER_NAME, CONTACTS_FOLDER_NAME, SHARED_FOLDER_ID);
	}

	@Test
	public void invalidUserShareFolderRequest() throws EmailIntegrationException {
		when(accountAdministrationService.getAccount(anyString(), any(List.class))).thenReturn(createAccountInformation(
				TEST_USER_ID, TEST_USER_MAIL,
				Lists.newArrayList(new GenericAttribute(GIVEN_NAME, "Test User"),
						new GenericAttribute("notUsedAttr", "notUsedVal"), new GenericAttribute(SN, "Display Name"),
						new GenericAttribute(DISPLAY_NAME, "Test User Display Name"),
						new GenericAttribute("notUsedAttr", "notUsedVal"))));
		doCallRealMethod().when(service).shareFolderWithUser(TEST_USER_MAIL);
		service.shareFolderWithUser(TEST_USER_MAIL);
		/* contact should be created using the shared folder id, target mail address and tenant mail address. */
		verify(contactsAdministrationService, times(0)).createContact(any(Map.class), anyString(), anyString());
		/* share rights must be given to the share folder id, to the user mail */
		verify(folderAdministrationService, times(0)).giveShareRightsToFolder(anyString(), anyString(), anyString());
		/*
		 * client mountpoint should be created using the targetEmail, targetId, folderName, contact view, shared folder
		 * and folderId
		 */
		verify(folderAdministrationService, times(0)).createMountPointForFolder(anyString(), anyString(), anyString(),
				anyString(), anyString());
	}

	@Test
	public void removeMailboxFromFolderTest() throws EmailIntegrationException {
		when(contactsAdministrationService.getContact(TENANT_ADMIN_EMAIL, TEST_USER_MAIL, SHARED_FOLDER_ID))
				.thenReturn(new ContactInformation(TEST_USER_ID, Collections.EMPTY_LIST));
		doCallRealMethod().when(service).removeContactFromShareFolder(TEST_USER_MAIL);
		service.removeContactFromShareFolder(TEST_USER_MAIL);
		/* Should remove correct user contact using the correct id of the user and contact owner email name. */
		verify(contactsAdministrationService, times(1)).removeContactFromFolder(TEST_USER_ID, TENANT_ADMIN_EMAIL);
	}

	@Test
	public void deleteNonExistentShareFolder() throws EmailIntegrationException {
		doCallRealMethod().when(service).deleteTenantShareFolder();
		when(accountAdministrationService.getAccount(anyString(), any(List.class)))
				.thenReturn(createAccountInformation(TENANT_ADMIN_ID, TENANT_ADMIN_EMAIL,
						Arrays.asList(new GenericAttribute(ZIMBRA_ACCOUNT_STATUS, "DISABLED"))));
		service.deleteTenantShareFolder();
		verify(folderAdministrationService, times(0)).deleteFolder(TENANT_ADMIN_EMAIL, SHARED_FOLDER_ID);
	}

	@Test
	public void deleteSharedFolderTest() throws EmailIntegrationException {
		doCallRealMethod().when(service).deleteTenantShareFolder();
		FolderInformation info = new FolderInformation();
		info.setId("test-folder-id");
		when(folderAdministrationService.getFolder(TENANT_ADMIN_EMAIL, SHARED_FOLDER_NAME)).thenReturn(info);
		when(accountAdministrationService.getAccount(anyString(), any(List.class)))
				.thenReturn(createAccountInformation(TENANT_ADMIN_ID, TENANT_ADMIN_EMAIL,
						Arrays.asList(new GenericAttribute(ZIMBRA_ACCOUNT_STATUS, "ACTIVE"))));
		service.deleteTenantShareFolder();
		verify(folderAdministrationService, times(1)).deleteFolder(anyString(), anyString());
	}

	@Test
	public void deleteSharedFolderUserInactiveTest() throws EmailIntegrationException {
		doCallRealMethod().when(service).deleteTenantShareFolder();
		FolderInformation info = new FolderInformation();
		info.setId("test-folder-id");
		when(folderAdministrationService.getFolder(TENANT_ADMIN_EMAIL, SHARED_FOLDER_NAME)).thenReturn(info);
		when(accountAdministrationService.getAccount(anyString(), any(List.class)))
				.thenReturn(createAccountInformation(TENANT_ADMIN_ID, TENANT_ADMIN_EMAIL,
						Arrays.asList(new GenericAttribute(ZIMBRA_ACCOUNT_STATUS, "DISABLED"))));
		service.deleteTenantShareFolder();
		verify(folderAdministrationService, times(0)).deleteFolder(anyString(), anyString());
	}

	@Test
	public void shouldBeEligibleForMountingTest() throws EmailIntegrationException {
		when(folderAdministrationService.getFolder(TEST_USER_MAIL, TENANT_ADMIN_ID))
				.thenReturn(new FolderInformation());
		when(service.isShareFolderMounted(TEST_USER_MAIL)).thenCallRealMethod();
		assertFalse(service.isShareFolderMounted(TEST_USER_MAIL));
	}

	@Test
	public void shouldNotBeEligibleForMountingTest() throws EmailIntegrationException {
		when(folderAdministrationService.getFolder(TEST_USER_MAIL, TENANT_ADMIN_ID))
				.thenReturn(new FolderInformation("folder-id", null, null, null));
		when(service.isShareFolderMounted(TEST_USER_MAIL)).thenCallRealMethod();
		assertTrue(service.isShareFolderMounted(TEST_USER_MAIL));

	}

	private static EmailAccountInformation createAccountInformation(String accountId, String accountName,
			List<GenericAttribute> attributes) {
		EmailAccountInformation accountInformation = new EmailAccountInformation();
		accountInformation.setAccountId(accountId);
		accountInformation.setAccountName(accountName);
		accountInformation.setAttributes(attributes);

		return accountInformation;
	}

}
