package com.sirma.sep.account.administration;

import static com.sirma.sep.email.EmailIntegrationConstants.EMAIL;
import static com.sirma.sep.email.EmailIntegrationConstants.FIRST_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.FULL_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.LAST_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.model.account.ContactInformation;
import com.zimbra.wsdl.zimbraservice.ZcsPortType;

import zimbra.ContactAttr;
import zimbramail.ContactActionRequest;
import zimbramail.ContactActionSelector;
import zimbramail.ContactInfo;
import zimbramail.ContactSpec;
import zimbramail.CreateContactRequest;
import zimbramail.GetContactsRequest;
import zimbramail.GetContactsResponse;
import zimbramail.NewContactAttr;

public class ContactsAdministrationServiceImplTest {

	private static final String TEST_EMAIL = "test-mail@mail.com";
	private static final String TEST_ID = "test-mail-id";
	private static final String TEST_TARGET_EMAIL = "test-target-mail@mail.com";
	private static final String TARGET_FOLDER_ID = "target-folder-id";

	@InjectMocks
	private ContactsAdministrationServiceImpl service;

	@Mock
	private FolderAdministrationServiceImpl folderService;

	@Mock
	private AccountAuthenticationService authenticationService;

	private ZcsPortType clientPortMock;

	@Before
	public void init() throws EmailIntegrationException {
		clientPortMock = Mockito.mock(ZcsPortType.class);

		service = mock(ContactsAdministrationServiceImpl.class);
		MockitoAnnotations.initMocks(this);
		when(authenticationService.getClientPort(TEST_TARGET_EMAIL)).thenReturn(clientPortMock);

	}

	@Test
	public void createContactTest() throws EmailIntegrationException {
		String fullName = "Full Name";
		Map<String, String> sentAttributes = new HashMap<>();
		sentAttributes.put(FIRST_NAME, "Full");
		sentAttributes.put(LAST_NAME, "Name");
		sentAttributes.put(FULL_NAME, fullName);
		sentAttributes.put(EMAIL, TEST_EMAIL);
		Map<String, String> expectedAttributes = new HashMap<>();
		expectedAttributes.put(FULL_NAME, fullName);
		expectedAttributes.put(FIRST_NAME, "Full");
		expectedAttributes.put(LAST_NAME, "Name");
		expectedAttributes.put(EMAIL, TEST_EMAIL);

		ArgumentCaptor<CreateContactRequest> captor = ArgumentCaptor.forClass(CreateContactRequest.class);
		doCallRealMethod().when(service).createContact(sentAttributes, TARGET_FOLDER_ID, TEST_TARGET_EMAIL);

		service.createContact(sentAttributes, TARGET_FOLDER_ID, TEST_TARGET_EMAIL);
		verify(clientPortMock).createContactRequest(captor.capture());
		CreateContactRequest req = captor.getValue();
		ContactSpec spec = req.getCn();
		assertEquals(TARGET_FOLDER_ID, spec.getL());
		for (NewContactAttr attr : spec.getA()) {
			assertEquals(expectedAttributes.get(attr.getN()), attr.getValue());
		}
	}

	@Test
	public void removeContactTest() throws EmailIntegrationException {
		doCallRealMethod().when(service).removeContactFromFolder(TEST_ID, TEST_TARGET_EMAIL);
		service.removeContactFromFolder(TEST_ID, TEST_TARGET_EMAIL);
		verify(service).executeContactOperation(TEST_ID, "delete", TEST_TARGET_EMAIL);

		ArgumentCaptor<ContactActionRequest> captor = ArgumentCaptor.forClass(ContactActionRequest.class);
		doCallRealMethod().when(service).executeContactOperation(TEST_ID, "delete", TEST_TARGET_EMAIL);
		service.removeContactFromFolder(TEST_ID, TEST_TARGET_EMAIL);
		verify(clientPortMock).contactActionRequest(captor.capture());
		ContactActionSelector req = captor.getValue().getAction();
		assertEquals(TEST_ID, req.getId());
		assertEquals("delete", req.getOp());
	}

	@Test
	public void getContactTest() throws EmailIntegrationException {
		String targetId = "test-target-id";
		when(service.getContact(TEST_TARGET_EMAIL, TEST_EMAIL, TARGET_FOLDER_ID)).thenCallRealMethod();
		GetContactsResponse resp = new GetContactsResponse();
		ContactInfo fakeInfo = new ContactInfo();
		fakeInfo.getA().add(new ContactAttr());
		ContactInfo actualInfo = new ContactInfo();
		ContactAttr attr = new ContactAttr();
		attr.setN(EMAIL);
		attr.setValue(TEST_EMAIL);
		actualInfo.getA().add(attr);
		actualInfo.setId(targetId);
		resp.getCn().addAll(Arrays.asList(fakeInfo, actualInfo));
		when(clientPortMock.getContactsRequest(any(GetContactsRequest.class))).thenReturn(resp);
		ArgumentCaptor<GetContactsRequest> captor = ArgumentCaptor.forClass(GetContactsRequest.class);
		ContactInformation returnedInfo = service.getContact(TEST_TARGET_EMAIL, TEST_EMAIL, TARGET_FOLDER_ID);
		verify(clientPortMock, times(1)).getContactsRequest(captor.capture());
		GetContactsRequest sentRequest = captor.getValue();
		// request must be send to the specified folder Id
		assertEquals(TARGET_FOLDER_ID, sentRequest.getL());
		assertEquals(targetId, returnedInfo.getId());
	}

	@Test(expected = EmailIntegrationException.class)
	public void getContactFailTest() throws EmailIntegrationException {
		when(clientPortMock.getContactsRequest(any(GetContactsRequest.class)))
				.thenThrow(EmailIntegrationException.class);
		when(service.getContact(anyString(), anyString(), anyString())).thenCallRealMethod();
		service.getContact(TEST_TARGET_EMAIL, TEST_TARGET_EMAIL, TARGET_FOLDER_ID);
	}

	@Test
	public void getContactNoResultTest() throws EmailIntegrationException {
		String targetId = "test-target-id";
		when(service.getContact(TEST_TARGET_EMAIL, "shouldnt-find-mail@domain.com", TARGET_FOLDER_ID))
				.thenCallRealMethod();
		GetContactsResponse resp = new GetContactsResponse();
		ContactInfo fakeInfo = new ContactInfo();
		fakeInfo.getA().add(new ContactAttr());
		ContactInfo actualInfo = new ContactInfo();
		ContactAttr attr = new ContactAttr();
		attr.setN(EMAIL);
		attr.setValue(TEST_EMAIL);
		actualInfo.getA().add(attr);
		actualInfo.setId(targetId);
		resp.getCn().addAll(Arrays.asList(fakeInfo, actualInfo));
		when(clientPortMock.getContactsRequest(any(GetContactsRequest.class))).thenReturn(resp);
		ArgumentCaptor<GetContactsRequest> captor = ArgumentCaptor.forClass(GetContactsRequest.class);
		ContactInformation returnedInfo = service.getContact(TEST_TARGET_EMAIL, "shouldnt-find-mail@domain.com",
				TARGET_FOLDER_ID);
		verify(clientPortMock, times(1)).getContactsRequest(captor.capture());
		GetContactsRequest sentRequest = captor.getValue();
		// request must be send to the specified folder Id
		assertEquals(TARGET_FOLDER_ID, sentRequest.getL());
		assertNull(returnedInfo.getId());
	}

	@Test
	public void getAllFolderContactsTest() throws EmailIntegrationException {
		when(service.getAllFolderContacts(TEST_TARGET_EMAIL, TARGET_FOLDER_ID)).thenCallRealMethod();
		GetContactsResponse resp = new GetContactsResponse();
		List<ContactInfo> responseList = Arrays.asList(new ContactInfo(), new ContactInfo(), new ContactInfo(),
				new ContactInfo(), new ContactInfo(), new ContactInfo(), new ContactInfo());
		resp.getCn().addAll(responseList);
		when(clientPortMock.getContactsRequest(any(GetContactsRequest.class))).thenReturn(resp);
		List<ContactInformation> returnedList = service.getAllFolderContacts(TEST_TARGET_EMAIL, TARGET_FOLDER_ID);
		assertEquals(responseList.size(), returnedList.size());
	}

	@Test(expected = EmailIntegrationException.class)
	public void getAllFolderFailTest() throws EmailIntegrationException {
		when(service.getAllFolderContacts(TEST_TARGET_EMAIL, TARGET_FOLDER_ID)).thenCallRealMethod();
		when(clientPortMock.getContactsRequest(any(GetContactsRequest.class)))
				.thenThrow(EmailIntegrationException.class);
		service.getAllFolderContacts(TEST_TARGET_EMAIL, TARGET_FOLDER_ID);

	}
}
