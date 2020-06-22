package com.sirma.sep.account.administration;

import static com.sirma.email.ZimbraEmailIntegrationConstants.ADMIN_UI_COMPONENTS;
import static com.sirma.email.ZimbraEmailIntegrationConstants.DELEGATED_ADMIN_ACCOUNT;
import static com.sirma.email.ZimbraEmailIntegrationConstants.IS_ADMIN_ACCOUNT;
import static com.sirma.email.ZimbraEmailIntegrationConstants.MAX_MAIL_QUOTA;
import static com.sirma.sep.email.EmailIntegrationConstants.DISPLAY_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.GIVEN_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.SN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.soap.SOAPFaultException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.sep.email.configuration.EmailIntegrationConfiguration;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.model.account.EmailAccountInformation;
import com.sirma.sep.email.model.account.GenericAttribute;
import com.zimbra.wsdl.zimbraservice.ZcsAdminPortType;
import com.zimbra.wsdl.zimbraservice.ZcsPortType;

import zimbra.OpValue;
import zimbraaccount.GetWhiteBlackListRequest;
import zimbraaccount.GetWhiteBlackListResponse;
import zimbraaccount.ModifyWhiteBlackListRequest;
import zimbraadmin.AccountInfo;
import zimbraadmin.Attr;
import zimbraadmin.CreateAccountRequest;
import zimbraadmin.CreateAccountResponse;
import zimbraadmin.DeleteAccountRequest;
import zimbraadmin.GetAccountRequest;
import zimbraadmin.GetAccountResponse;
import zimbraadmin.GetAllAccountsRequest;
import zimbraadmin.GetAllAccountsResponse;
import zimbraadmin.GrantRightRequest;
import zimbraadmin.ModifyAccountRequest;
import zimbraadmin.ModifyAccountResponse;
import zimbraadmin.RenameAccountRequest;
import zimbraadmin.RenameAccountResponse;
import zimbraadmin.RevokeRightRequest;

/**
 * Test for {@link EmailAccountAdministrationServiceImpl}. Mostly tests whether correct requests are sent and properly
 * formatted responses are returned by the service methods.
 *
 * @author g.tsankov
 */
public class EmailAccountAdministrationServiceImplTest {

	@InjectMocks
	private EmailAccountAdministrationServiceImpl service;

	@Mock
	private EmailIntegrationConfiguration emailIntegrationConfiguration;

	@Mock
	private AccountAuthenticationService authenticationService;

	private ZcsAdminPortType stubbedAdminPort;
	private ZcsPortType stubbedClientPort;

	private static final String TEST_ID = "test-account-id-12345";
	private static final String TEST_ACCOUNT_NAME = "testAcc@test-domain.com";
	private static final String TEST_ACCOUNT_PASSWORD = "123456";

	@Before
	public void setup() throws Exception {
		stubbedAdminPort = mock(ZcsAdminPortType.class);
		stubbedClientPort = mock(ZcsPortType.class);
		service = mock(EmailAccountAdministrationServiceImpl.class);
		MockitoAnnotations.initMocks(this);

		when(emailIntegrationConfiguration.getAdminAccount())
				.thenReturn(new ConfigurationPropertyMock(new EmfUser("admin", "123456")));
		when(emailIntegrationConfiguration.getTenantAdminAccount())
				.thenReturn(new ConfigurationPropertyMock(new EmfUser("admin", "123456")));

		when(authenticationService.getTenantAdminPort()).thenReturn(stubbedAdminPort);
		when(authenticationService.getAdminPort()).thenReturn(stubbedAdminPort);
		when(authenticationService.getClientPort(anyString(), anyString())).thenReturn(stubbedClientPort);
	}

	@Test
	public void testCreateAccount() throws EmailIntegrationException {
		when(service.createAccount(any(String.class), any(String.class), any(Map.class))).thenCallRealMethod();
		ArgumentCaptor<CreateAccountRequest> createCaptor = ArgumentCaptor.forClass(CreateAccountRequest.class);

		CreateAccountResponse expectedResponse = new CreateAccountResponse();
		AccountInfo expectedAccount = new AccountInfo();
		expectedAccount.setId(TEST_ID);
		expectedAccount.setName("testAcc@test-domain.com");
		expectedResponse.setAccount(expectedAccount);

		when(stubbedAdminPort.createAccountRequest(any(CreateAccountRequest.class))).thenReturn(expectedResponse);
		Map<String, String> attributes = new HashMap<>();
		attributes.put(GIVEN_NAME, "firstName");
		attributes.put(SN, "lastName");
		attributes.put(DISPLAY_NAME, "displayName");

		EmailAccountInformation actualResult = service.createAccount(TEST_ACCOUNT_NAME, TEST_ACCOUNT_PASSWORD,
				attributes);
		verify(stubbedAdminPort).createAccountRequest(createCaptor.capture());
		CreateAccountRequest expectedRequest = createCaptor.getValue();
		List<GenericAttribute> actualAttributes = createEmailAccountListFromZimbraAttr(expectedRequest.getA());

		// assert correct request creation
		assertEquals(TEST_ACCOUNT_NAME, expectedRequest.getName());
		assertEquals(TEST_ACCOUNT_PASSWORD, expectedRequest.getPassword());
		// assert successful creation response
		assertEquals(TEST_ID, actualResult.getAccountId());
		assertEquals(TEST_ACCOUNT_NAME, actualResult.getAccountName());
		List<GenericAttribute> expected = new ArrayList<>();
		expected.add(new GenericAttribute(DISPLAY_NAME, "displayName"));
		expected.add(new GenericAttribute(GIVEN_NAME, "firstName"));
		expected.add(new GenericAttribute(SN, "lastName"));

		int i = 0;
		Iterator<GenericAttribute> itr = actualAttributes.iterator();
		while (itr.hasNext()) {
			GenericAttribute actualAttr = itr.next();
			assertEquals(actualAttr.getAttributeName(), expected.get(i).getAttributeName());
			i++;
		}
	}

	@Test
	public void testCreateAdminAccount() throws EmailIntegrationException {
		when(service.createTenantAdminAccount(any(String.class), any(String.class))).thenCallRealMethod();
		ArgumentCaptor<CreateAccountRequest> createCaptor = ArgumentCaptor.forClass(CreateAccountRequest.class);
		ArgumentCaptor.forClass(List.class);

		CreateAccountResponse expectedResponse = new CreateAccountResponse();
		AccountInfo expectedAccount = new AccountInfo();
		expectedAccount.setId(TEST_ID);
		expectedAccount.setName("testAcc@test-domain.com");
		expectedResponse.setAccount(expectedAccount);

		when(stubbedAdminPort.createAccountRequest(any(CreateAccountRequest.class))).thenReturn(expectedResponse);
		EmailAccountInformation actualResult = service.createTenantAdminAccount(TEST_ACCOUNT_NAME,
				TEST_ACCOUNT_PASSWORD);
		verify(stubbedAdminPort).createAccountRequest(createCaptor.capture());
		CreateAccountRequest expectedRequest = createCaptor.getValue();
		List<GenericAttribute> actualAttributes = actualResult.getAttributes();

		// assert correct request creation
		assertEquals(TEST_ACCOUNT_NAME, expectedRequest.getName());
		assertEquals(TEST_ACCOUNT_PASSWORD, expectedRequest.getPassword());
		// assert successful creation response
		assertEquals(TEST_ID, actualResult.getAccountId());
		assertEquals(TEST_ACCOUNT_NAME, actualResult.getAccountName());
		List<GenericAttribute> expected = new ArrayList<>();
		expected.add(new GenericAttribute(IS_ADMIN_ACCOUNT, "TRUE"));
		expected.add(new GenericAttribute(DELEGATED_ADMIN_ACCOUNT, "TRUE"));
		int i = 0;
		Iterator<GenericAttribute> itr = actualAttributes.iterator();

		while (itr.hasNext()) {
			GenericAttribute actualAttr = itr.next();
			assertEquals(expected.get(i).getAttributeName(), actualAttr.getAttributeName());
			assertEquals(expected.get(i).getAttributeName(), actualAttr.getAttributeName());
			i++;
		}
	}

	@Test(expected = EmailIntegrationException.class)
	public void testCreateWithError() throws EmailIntegrationException {
		when(service.createAccount(any(String.class), any(String.class), any(Map.class))).thenCallRealMethod();
		when(stubbedAdminPort.createAccountRequest(any(CreateAccountRequest.class)))
				.thenThrow(SOAPFaultException.class);
		service.createAccount("", "", new HashMap<>());
		fail("shouldn't reach here");
	}

	@Test(expected = EmailIntegrationException.class)
	public void testCreateTenantAdminWithError() throws EmailIntegrationException {
		when(service.createTenantAdminAccount(any(String.class), any(String.class))).thenCallRealMethod();
		when(stubbedAdminPort.createAccountRequest(any(CreateAccountRequest.class)))
				.thenThrow(SOAPFaultException.class);
		service.createTenantAdminAccount("", "");
		fail("shouldn't reach here");
	}

	@Test
	public void testDeleteAccount() throws JAXBException, ParserConfigurationException, EmailIntegrationException {
		doCallRealMethod().when(service).deleteAccount(any(String.class));
		ArgumentCaptor<DeleteAccountRequest> captor = ArgumentCaptor.forClass(DeleteAccountRequest.class);
		service.deleteAccount(TEST_ID);
		verify(stubbedAdminPort).deleteAccountRequest(captor.capture());
		DeleteAccountRequest expectedRequest = captor.getValue();

		assertEquals(TEST_ID, expectedRequest.getId());
	}

	@Test(expected = EmailIntegrationException.class)
	public void testDeleteAccountFail() throws EmailIntegrationException {
		doCallRealMethod().when(service).deleteAccount(any(String.class));
		when(stubbedAdminPort.deleteAccountRequest(any(DeleteAccountRequest.class)))
				.thenThrow(SOAPFaultException.class);
		service.deleteAccount(TEST_ID);
		fail("shouldn't reach here");
	}

	@Test
	public void testRenameAccount() throws EmailIntegrationException {
		doCallRealMethod().when(service).renameAccount(any(String.class), any(String.class));
		RenameAccountResponse returned = new RenameAccountResponse();
		returned.setAccount(new AccountInfo());
		when(stubbedAdminPort.renameAccountRequest(any(RenameAccountRequest.class))).thenReturn(returned);
		ArgumentCaptor<RenameAccountRequest> captor = ArgumentCaptor.forClass(RenameAccountRequest.class);
		String expectedRename = "test-rename@domain.com";
		service.renameAccount(TEST_ID, expectedRename);
		verify(stubbedAdminPort).renameAccountRequest(captor.capture());
		RenameAccountRequest actualRename = captor.getValue();
		assertEquals(TEST_ID, actualRename.getId());
		assertEquals(expectedRename, actualRename.getNewName());
	}

	@Test(expected = EmailIntegrationException.class)
	public void testRenameAccountFail() throws EmailIntegrationException {
		doCallRealMethod().when(service).renameAccount(any(String.class), any(String.class));
		when(stubbedAdminPort.renameAccountRequest(any(RenameAccountRequest.class)))
				.thenThrow(SOAPFaultException.class);
		service.renameAccount(TEST_ID, "");
		fail("shouldn't reach here");
	}

	@Test
	public void addDelegatePermissionTest() throws EmailIntegrationException {
		doCallRealMethod().when(service).modifyDelegatePermission(anyString(), anyString(), any(boolean.class));
		ArgumentCaptor<GrantRightRequest> captor = ArgumentCaptor.forClass(GrantRightRequest.class);
		String expectedTarget = "testTarget@test.com";
		String expectedGrantee = "testGrantee@test.com";
		service.modifyDelegatePermission(expectedTarget, expectedGrantee, true);
		verify(stubbedAdminPort).grantRightRequest(captor.capture());
		GrantRightRequest actual = captor.getValue();
		assertEquals(expectedTarget, actual.getTarget().getValue());
		assertEquals(expectedGrantee, actual.getGrantee().getValue());
		assertEquals("sendAs", actual.getRight().getValue());
	}

	@Test
	public void removeDelegatePermissionTest() throws EmailIntegrationException {
		doCallRealMethod().when(service).modifyDelegatePermission(anyString(), anyString(), any(boolean.class));
		ArgumentCaptor<RevokeRightRequest> captor = ArgumentCaptor.forClass(RevokeRightRequest.class);
		String expectedTarget = "testTarget@test.com";
		String expectedGrantee = "testGrantee@test.com";
		service.modifyDelegatePermission(expectedTarget, expectedGrantee, false);
		verify(stubbedAdminPort).revokeRightRequest(captor.capture());
		RevokeRightRequest actual = captor.getValue();
		assertEquals(expectedTarget, actual.getTarget().getValue());
		assertEquals(expectedGrantee, actual.getGrantee().getValue());
		assertEquals("sendAs", actual.getRight().getValue());
	}

	@Test(expected = EmailIntegrationException.class)
	public void failPermissionDelegationTest() throws EmailIntegrationException {
		doCallRealMethod().when(service).modifyDelegatePermission(anyString(), anyString(), any(boolean.class));
		when(stubbedAdminPort.grantRightRequest(any(GrantRightRequest.class))).thenThrow(SOAPFaultException.class);

		service.modifyDelegatePermission("testTarget", "testGrantee", true);
	}

	@Test
	public void testModifyAccount() throws EmailIntegrationException {
		when(service.modifyAccount(any(String.class), any())).thenCallRealMethod();
		ArgumentCaptor<ModifyAccountRequest> captor = ArgumentCaptor.forClass(ModifyAccountRequest.class);

		GenericAttribute testAttr = new GenericAttribute("testAttribute1", "testValue1");
		GenericAttribute testAttr2 = new GenericAttribute("testAttribute2", "testValue2");
		GenericAttribute testAttr3 = new GenericAttribute("testAttribute3", "testValue3");

		ModifyAccountResponse expectedResponse = new ModifyAccountResponse();
		AccountInfo expectedAccount = new AccountInfo();
		expectedAccount.setId(TEST_ID);
		expectedAccount.setName(TEST_ACCOUNT_NAME);
		expectedAccount.getA().add(createZimbraAttr(testAttr.getAttributeName(), testAttr.getValue()));
		expectedAccount.getA().add(createZimbraAttr(testAttr2.getAttributeName(), testAttr2.getValue()));
		expectedAccount.getA().add(createZimbraAttr(testAttr3.getAttributeName(), testAttr3.getValue()));
		expectedResponse.setAccount(expectedAccount);

		when(stubbedAdminPort.modifyAccountRequest(any(ModifyAccountRequest.class))).thenReturn(expectedResponse);

		EmailAccountInformation actualReturned = service.modifyAccount(TEST_ID,
				Arrays.asList(testAttr, testAttr2, testAttr3));

		verify(stubbedAdminPort).modifyAccountRequest(captor.capture());
		ModifyAccountRequest actualRequest = captor.getValue();

		// verify request is sent
		assertEquals(TEST_ID, actualRequest.getId());
		int i = 0;
		Iterator attrIterator = actualRequest.getA().iterator();
		while (attrIterator.hasNext()) {
			i++;
			Attr attr = (Attr) attrIterator.next();
			assertEquals(attr.getN(), "testAttribute" + i);
			assertEquals(attr.getValue(), "testValue" + i);
		}

		i = 0;
		// verify returned value
		assertEquals(TEST_ID, actualReturned.getAccountId());
		Iterator returnedAttrIterator = actualReturned.getAttributes().iterator();
		while (returnedAttrIterator.hasNext()) {
			i++;
			GenericAttribute returnedAttr = (GenericAttribute) returnedAttrIterator.next();
			assertEquals(returnedAttr.getAttributeName(), "testAttribute" + i);
			assertEquals(returnedAttr.getValue(), "testValue" + i);
		}
	}

	@Test(expected = SOAPFaultException.class)
	public void testModifyAccountFail() throws EmailIntegrationException {
		when(service.modifyAccount(any(String.class), any())).thenCallRealMethod();
		when(stubbedAdminPort.modifyAccountRequest(any(ModifyAccountRequest.class)))
				.thenThrow(SOAPFaultException.class);
		service.modifyAccount("", Arrays.asList());
		fail("shouldn't reach here");
	}

	@Test
	public void testDisableAccount() throws EmailIntegrationException {
		doCallRealMethod().when(service).disableAccount(any(String.class));
		ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<List> attributeCaptor = ArgumentCaptor.forClass(List.class);
		service.disableAccount(TEST_ID);
		verify(service).modifyAccount(idCaptor.capture(), attributeCaptor.capture());
		assertEquals(TEST_ID, idCaptor.getValue());
		List<GenericAttribute> list = attributeCaptor.getValue();
		assertEquals("zimbraAccountStatus", list.get(0).getAttributeName());
		assertEquals("closed", list.get(0).getValue());
	}

	@Test(expected = EmailIntegrationException.class)
	public void testDisableAccountFail() throws EmailIntegrationException {
		doCallRealMethod().when(service).disableAccount(any(String.class));
		doCallRealMethod().when(service).modifyAccount(anyString(), any(List.class));
		when(stubbedAdminPort.modifyAccountRequest(any(ModifyAccountRequest.class)))
				.thenThrow(SOAPFaultException.class);
		service.disableAccount("");
		fail("shouldn't reach here");
	}

	@Test
	public void testGetAccount() throws EmailIntegrationException {
		when(service.getAccount(any(String.class))).thenCallRealMethod();
		when(service.getAccount(anyString(), any(List.class))).thenCallRealMethod();

		GetAccountResponse response = new GetAccountResponse();
		AccountInfo expectedInfo = new AccountInfo();
		expectedInfo.setId(TEST_ID);
		expectedInfo.setName(TEST_ACCOUNT_NAME);
		response.setAccount(expectedInfo);

		when(stubbedAdminPort.getAccountRequest(any(GetAccountRequest.class))).thenReturn(response);
		EmailAccountInformation actual = service.getAccount(TEST_ACCOUNT_NAME);
		assertEquals(TEST_ID, actual.getAccountId());
		assertEquals(TEST_ACCOUNT_NAME, actual.getAccountName());
	}

	@Test
	public void testGetAccountSpecificAttributes() throws EmailIntegrationException {
		when(service.getAccount(anyString(), any(List.class))).thenCallRealMethod();
		ArgumentCaptor<GetAccountRequest> captor = ArgumentCaptor.forClass(GetAccountRequest.class);
		GetAccountResponse response = new GetAccountResponse();
		AccountInfo expectedInfo = new AccountInfo();
		expectedInfo.setId(TEST_ID);
		expectedInfo.setName(TEST_ACCOUNT_NAME);
		response.setAccount(expectedInfo);

		when(stubbedAdminPort.getAccountRequest(any(GetAccountRequest.class))).thenReturn(response);
		EmailAccountInformation actual = service.getAccount(TEST_ACCOUNT_NAME,
				Arrays.asList("prop1", "prop2", "prop3", "prop4", "prop5", "prop6"));
		verify(stubbedAdminPort, times(1)).getAccountRequest(captor.capture());
		GetAccountRequest sentRequest = captor.getValue();
		assertEquals("prop1,prop2,prop3,prop4,prop5,prop6", sentRequest.getAttrs());
		assertEquals(TEST_ID, actual.getAccountId());
		assertEquals(TEST_ACCOUNT_NAME, actual.getAccountName());

	}

	@Test(expected = EmailIntegrationException.class)
	public void testGetAccountFail() throws EmailIntegrationException {
		when(service.getAccount(anyString())).thenCallRealMethod();
		when(service.getAccount(anyString(), any(List.class))).thenCallRealMethod();
		when(stubbedAdminPort.getAccountRequest(any(GetAccountRequest.class))).thenThrow(SOAPFaultException.class);
		service.getAccount("");
		fail("shouldn't reach here");
	}

	@Test
	public void testGetAccounts() throws EmailIntegrationException {
		when(service.getAllAccounts()).thenCallRealMethod();
		ArgumentCaptor.forClass(GetAllAccountsRequest.class);

		AccountInfo testAcc = createZimbraAccountInfo("testId1", "testName1", new ArrayList<Attr>());
		AccountInfo testAcc2 = createZimbraAccountInfo("testId2", "testName2", new ArrayList<Attr>());
		AccountInfo testAcc3 = createZimbraAccountInfo("testId3", "testName3", new ArrayList<Attr>());

		GetAllAccountsResponse response = new GetAllAccountsResponse();
		response.getAccount().addAll(Arrays.asList(testAcc, testAcc2, testAcc3));
		when(stubbedAdminPort.getAllAccountsRequest(any(GetAllAccountsRequest.class))).thenReturn(response);
		List<EmailAccountInformation> actualReturned = service.getAllAccounts();
		int i = 0;
		Iterator returnedIterator = actualReturned.iterator();
		while (returnedIterator.hasNext()) {
			i++;
			EmailAccountInformation returned = (EmailAccountInformation) returnedIterator.next();
			assertEquals(returned.getAccountId(), "testId" + i);
			assertEquals(returned.getAccountName(), "testName" + i);
		}
	}

	@Test(expected = EmailIntegrationException.class)
	public void testGetAccountsFail() throws EmailIntegrationException {
		when(service.getAllAccounts()).thenCallRealMethod();
		when(stubbedAdminPort.getAllAccountsRequest(any(GetAllAccountsRequest.class)))
				.thenThrow(SOAPFaultException.class);
		service.getAllAccounts();
		fail("shouldn't reach here");
	}

	@Test
	public void testGetWhiteList() throws EmailIntegrationException {
		String[] expectedAddr = { "expected-addr1@domain.com", "expected-addr2@domain.com",
				"expected-addr3@domain.com" };
		when(service.getWhiteList(any(String.class), any(String.class))).thenCallRealMethod();
		GetWhiteBlackListResponse response = new GetWhiteBlackListResponse();
		zimbraaccount.GetWhiteBlackListResponse.WhiteList responseList = new zimbraaccount.GetWhiteBlackListResponse.WhiteList();
		responseList.getAddr().addAll(Arrays.asList(expectedAddr));
		response.setWhiteList(responseList);

		when(stubbedClientPort.getWhiteBlackListRequest(any(GetWhiteBlackListRequest.class))).thenReturn(response);
		List<String> actual = service.getWhiteList(TEST_ACCOUNT_NAME, TEST_ACCOUNT_PASSWORD);
		Iterator actualIterator = actual.iterator();
		int i = 0;
		while (actualIterator.hasNext()) {
			assertEquals(expectedAddr[i], actualIterator.next());
			i++;
		}
	}

	@Test(expected = EmailIntegrationException.class)
	public void getWhiteListFail() throws EmailIntegrationException {
		when(service.getWhiteList(any(String.class), any(String.class))).thenCallRealMethod();
		when(stubbedClientPort.getWhiteBlackListRequest(any(GetWhiteBlackListRequest.class)))
				.thenThrow(SOAPFaultException.class);
		service.getWhiteList("", "");
		fail("shouldn't reach here");
	}

	@Test
	public void testModifyWhiteList() throws EmailIntegrationException {
		doCallRealMethod().when(service).modifyWhiteList(any(String.class), any(String.class), any(List.class),
				any(String.class));
		ArgumentCaptor<ModifyWhiteBlackListRequest> captor = ArgumentCaptor.forClass(ModifyWhiteBlackListRequest.class);
		service.modifyWhiteList(TEST_ACCOUNT_NAME, TEST_ACCOUNT_PASSWORD, Arrays.asList("test-addition@domain.com"),
				"+");
		verify(stubbedClientPort).modifyWhiteBlackListRequest(captor.capture());
		ModifyWhiteBlackListRequest capturedRequest = captor.getValue();
		OpValue capturedOpValue = capturedRequest.getWhiteList().getAddr().get(0);
		assertEquals("test-addition@domain.com", capturedOpValue.getValue());
		assertEquals("+", capturedOpValue.getOp());
	}

	@Test(expected = EmailIntegrationException.class)
	public void testModifyWhiteListFail() throws EmailIntegrationException {
		doCallRealMethod().when(service).modifyWhiteList(any(String.class), any(String.class), any(List.class),
				any(String.class));
		when(stubbedClientPort.modifyWhiteBlackListRequest(any(ModifyWhiteBlackListRequest.class)))
				.thenThrow(SOAPFaultException.class);
		service.modifyWhiteList("", "", Arrays.asList(), "");
		fail("shouldn't reach here");
	}

	@Test
	public void testModifyAdminAccount() throws EmailIntegrationException {
		List<GenericAttribute> expected = new LinkedList<>();
		expected.add(new GenericAttribute(DELEGATED_ADMIN_ACCOUNT, "TRUE"));
		expected.add(new GenericAttribute(ADMIN_UI_COMPONENTS, "cartBlancheUI"));
		expected.add(new GenericAttribute(ADMIN_UI_COMPONENTS, "domainListView"));
		expected.add(new GenericAttribute(ADMIN_UI_COMPONENTS, "accountListView"));
		expected.add(new GenericAttribute(ADMIN_UI_COMPONENTS, "DLListView"));
		expected.add(new GenericAttribute(MAX_MAIL_QUOTA, "0"));

		doCallRealMethod().when(service).modifyAdminAccount(any(String.class));
		EmailAccountInformation account = Mockito.mock(EmailAccountInformation.class);
		when(account.getAccountId()).thenReturn("123456");
		when(service.getAccount(anyString())).thenReturn(account);
		service.modifyAdminAccount("admin@zimbra.com");

		ArgumentCaptor<List<GenericAttribute>> captor = ArgumentCaptor.forClass(List.class);

		verify(service).modifyAccount(anyString(), captor.capture());
		int i = 0;
		for (GenericAttribute element : captor.getValue()) {
			assertEquals(element.getAttributeName(), expected.get(i).getAttributeName());
			assertEquals(element.getValue(), expected.get(i).getValue());
			i++;
		}
	}

	@Test
	public void testGrantAdminDomainRights() throws EmailIntegrationException {
		when(emailIntegrationConfiguration.getTenantDomainAddress())
				.thenReturn(new ConfigurationPropertyMock("sirmaplatform.com"));
		doCallRealMethod().when(service).grantAdminDomainRights(any(String.class));
		service.grantAdminDomainRights("admin@zimbra.com");
		verify(stubbedAdminPort, times(8)).grantRightRequest(anyObject());
	}

	@Test
	public void testGrantAdminAccountRights() throws EmailIntegrationException {
		when(emailIntegrationConfiguration.getTenantDomainAddress())
				.thenReturn(new ConfigurationPropertyMock("sirmaplatform.com"));
		doCallRealMethod().when(service).grantAdminAccountRights(any(String.class));
		service.grantAdminAccountRights("admin@zimbra.com");
		verify(stubbedAdminPort, times(9)).grantRightRequest(anyObject());
	}

	private Attr createZimbraAttr(String name, String value) {
		Attr attr = new Attr();
		attr.setN(name);
		attr.setValue(value);
		return attr;
	}

	private static List<GenericAttribute> createEmailAccountListFromZimbraAttr(List<Attr> attrList) {
		List<GenericAttribute> attributeList = new ArrayList<>();
		attrList.forEach(attribute -> {
			attributeList.add(new GenericAttribute(attribute.getN(), attribute.getValue()));
		});

		return attributeList;
	}

	private static AccountInfo createZimbraAccountInfo(String id, String name, List<Attr> attributes) {
		AccountInfo accInfo = new AccountInfo();
		accInfo.setId(id);
		accInfo.setName(name);
		accInfo.getA().addAll(attributes);
		return accInfo;
	}
}
