package com.sirma.sep.email.tenant;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.exception.RollbackedException;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.wizard.TenantDeletionContext;
import com.sirma.itt.seip.tenant.wizard.TenantInitializationContext;
import com.sirma.itt.seip.tenant.wizard.TenantStepData;
import com.sirma.itt.seip.testutil.fakes.SecurityContextManagerFake;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.sep.account.administration.EmailAccountAdministrationServiceImpl;
import com.sirma.sep.email.address.resolver.EmailAddress;
import com.sirma.sep.email.address.resolver.EmailAddressResolver;
import com.sirma.sep.email.configuration.EmailIntegrationConfiguration;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.model.account.EmailAccountInformation;
import com.sirma.sep.email.model.domain.DomainInformation;
import com.sirma.sep.email.service.DomainAdministrationService;

/**
 * Test for for {@link TenantEmailIntegrationStep}.
 *
 * @author S.Djulgerova
 */
public class TenantEmailIntegrationStepTest {

	@Mock
	protected ConfigurationManagement configurationManagement;

	@Spy
	protected SecurityContextManager securityContextManager = new SecurityContextManagerFake();

	@Mock
	private EmailIntegrationConfiguration emailIntegrationConfiguration;

	@Mock
	private DomainAdministrationService domainAdministartionService;

	@Mock
	private EmailAddressResolver emailAddressResolver;

	@Mock
	private DomainInformation DomainInformation;

	@Mock
	private EmailAccountAdministrationServiceImpl accountAdministrationService;

	@InjectMocks
	private TenantEmailIntegrationStep step = new TenantEmailIntegrationStep();

	@Before
	public void init() throws EmailIntegrationException {
		MockitoAnnotations.initMocks(this);
		ConfigurationPropertyMock<String> tenantDomainAddressMock = new ConfigurationPropertyMock<>();
		tenantDomainAddressMock.setName("subsystem.emailintegration.email.tenant.domain.address");
		tenantDomainAddressMock.setValue("test-domain.com");
		when(emailIntegrationConfiguration.getTenantDomainAddress()).thenReturn(tenantDomainAddressMock);

		ConfigurationPropertyMock<String> webmailUrlMock = new ConfigurationPropertyMock<>();
		webmailUrlMock.setName("subsystem.emailintegration.webmail.url");
		when(emailIntegrationConfiguration.getWebmailUrl()).thenReturn(webmailUrlMock);

		ConfigurationPropertyMock<String> webmailPortMock = new ConfigurationPropertyMock<>();
		webmailPortMock.setName("subsystem.emailintegration.webmail.port");
		when(emailIntegrationConfiguration.getWebmailPort()).thenReturn(webmailPortMock);

		EmailAccountInformation tenantAccountInfo = new EmailAccountInformation();
		tenantAccountInfo.setAccountId("test-tenant-id");
		when(accountAdministrationService.getAccount(any(String.class))).thenReturn(tenantAccountInfo);
		when(emailIntegrationConfiguration.getTenantAdminAccount())
		.thenReturn(new ConfigurationPropertyMock(new EmfUser("admin", "123456")));
		ConfigurationPropertyMock<String> cosConfig = new ConfigurationPropertyMock<String>();
		cosConfig.setName("subsystem.emailintegration.email.tenant.domain.classofservice");
		cosConfig.setValue("test-cos");
		when(emailIntegrationConfiguration.getTenantClassOfService()).thenReturn(cosConfig);
	}

	@Test
	public void testExecute() throws RollbackedException, JSONException, EmailIntegrationException {
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

		TenantStepData data = new TenantStepData("tenantdomainaddress", new JSONObject(
				"{properties:[{id:'tenantdomainaddress', value:'test-domain.com'},{id:'webmailurl',value:'test.com'},{id:'webmailport',value:'1234'}]}"));
		TenantInitializationContext context = new TenantInitializationContext();
		TenantInfo info = new TenantInfo("test.id");
		context.setTenantInfo(info);
		when(domainAdministartionService.getDomain(any(String.class))).thenReturn(Optional.empty());

		step.execute(data, context);
		verify(configurationManagement, times(2)).addConfigurations(any(Collection.class));
		verify(domainAdministartionService).createDomain(captor.capture());
		String actualDomain = captor.getValue();
		assertEquals("test-domain.com", actualDomain);
	}

	@Test
	public void testExecuteDomainExist() throws RollbackedException, JSONException, EmailIntegrationException {
		TenantStepData data = new TenantStepData("tenantdomainaddress", new JSONObject(
				"{properties:[{id:'tenantdomainaddress', value:'test-domain.com'},{id:'webmailurl',value:'test.com'},{id:'webmailport',value:'1234'}]}"));
		TenantInitializationContext context = new TenantInitializationContext();
		TenantInfo info = new TenantInfo("test.id");
		context.setTenantInfo(info);
		when(domainAdministartionService.getDomain(any(String.class))).thenReturn(Optional.of(DomainInformation));

		step.execute(data, context);
		verify(domainAdministartionService, times(0)).createDomain(any(String.class));
	}

	@Test
	public void testExecuteWithoutData() throws RollbackedException, JSONException, EmailIntegrationException {
		TenantStepData data = new TenantStepData("tenantdomainAddress", new JSONObject(
				"{properties:[{id:'tenantdomainaddress', value:''},{id:'webmailurl',value:''},{id:'webmailport',value:''}]}"));
		TenantInitializationContext context = new TenantInitializationContext();
		TenantInfo info = new TenantInfo("test.id");
		context.setTenantInfo(info);
		// step should be skipped
		step.execute(data, context);
		verify(domainAdministartionService, times(0)).getDomain(anyString());
		verify(domainAdministartionService, times(0)).createDomain(anyString());
		verify(configurationManagement, times(0)).addConfigurations(any(Collection.class));
	}

	@Test(expected = Exception.class)
	public void testExecute_withIncorrectData() throws RollbackedException, JSONException {
		TenantStepData data = new TenantStepData("tenantdomainAddress", new JSONObject(
				"{properties:[{id:'tenantdomainaddress', value:'test-domain.com'},{id:'webmailurl',value:''},{id:'webmailport',value:''}]}"));
		TenantInitializationContext context = new TenantInitializationContext();
		TenantInfo info = new TenantInfo("test.id");
		context.setTenantInfo(info);
		// exception should be thrown for missing configuration
		step.execute(data, context);
	}

	@Test
	public void testExecuteWithoutDomainData() throws RollbackedException, JSONException, EmailIntegrationException {
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

		TenantStepData data = new TenantStepData("tenantdomainAddress", new JSONObject(
				"{properties:[{id:'tenantdomainaddress', value:''},{id:'webmailurl',value:'test.com'},{id:'webmailport',value:'1234'}]}"));
		TenantInitializationContext context = new TenantInitializationContext();
		TenantInfo info = new TenantInfo("test.id");
		context.setTenantInfo(info);

		when(domainAdministartionService.getDomain(any(String.class))).thenReturn(Optional.empty());
		step.execute(data, context);

		verify(configurationManagement, times(2)).addConfigurations(any(Collection.class));
		verify(domainAdministartionService).createDomain(captor.capture());
		String actualDomain = captor.getValue();
		assertEquals("test.id", actualDomain);
	}

	@Test
	public void testRollback() throws RollbackedException, JSONException, EmailIntegrationException {
		DomainInformation mockInfo = Mockito.mock(DomainInformation.class);
		List<String> tenantsInDomainReturn = new LinkedList<>();
		tenantsInDomainReturn.add("test-domain");
		List<EmailAddress> getAllEmailsReturn = new LinkedList<>();
		when(accountAdministrationService.getAccount(anyString())).thenReturn(new EmailAccountInformation());
		when(mockInfo.getDomainId()).thenReturn("test-id");
		when(mockInfo.getDomainName()).thenReturn("test-domain");
		when(domainAdministartionService.getDomain(anyString())).thenReturn(Optional.of(mockInfo));
		when(emailAddressResolver.getAllEmailsByTenant(anyString())).thenReturn(getAllEmailsReturn);
		when(emailAddressResolver.getAllTenantsInDomain(anyString())).thenReturn(tenantsInDomainReturn);
		ArgumentCaptor<DomainInformation> captor = ArgumentCaptor.forClass(DomainInformation.class);
		TenantStepData data = new TenantStepData("tenantdomainAddress",
				new JSONObject("{properties:[{id:'tenantdomainaddress', value:'test-domain.com'}]}"));

		TenantInitializationContext context = new TenantInitializationContext();
		TenantInfo info = new TenantInfo("test.id");
		context.setTenantInfo(info);
		step.delete(data, new TenantDeletionContext(info, true));
		verify(configurationManagement, times(1))
		.removeConfiguration("subsystem.emailintegration.email.tenant.domain.address");
		verify(domainAdministartionService).deleteDomain(captor.capture());
		DomainInformation actualDomain = captor.getValue();
		assertEquals("test-id", actualDomain.getDomainId());
	}

	@Test
	public void testRollbackMoreThanOneTenantsOnDomain() throws EmailIntegrationException, JSONException {
		DomainInformation mockInfo = Mockito.mock(DomainInformation.class);
		List<String> tenantsInDomainReturn = new LinkedList<>();
		tenantsInDomainReturn.add("test-domain");
		tenantsInDomainReturn.add("test2-domain");
		List<EmailAddress> getAllEmailsReturn = new LinkedList<>();
		when(accountAdministrationService.getAccount(anyString())).thenReturn(new EmailAccountInformation());
		when(mockInfo.getDomainId()).thenReturn("test-id");
		when(mockInfo.getDomainName()).thenReturn("test-domain");
		when(domainAdministartionService.getDomain(anyString())).thenReturn(Optional.of(mockInfo));
		when(emailAddressResolver.getAllEmailsByTenant(anyString())).thenReturn(getAllEmailsReturn);
		when(emailAddressResolver.getAllTenantsInDomain(anyString())).thenReturn(tenantsInDomainReturn);
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		TenantStepData data = new TenantStepData("tenantdomainAddress",
				new JSONObject("{properties:[{id:'tenantdomainaddress', value:'test-domain.com'}]}"));

		TenantInitializationContext context = new TenantInitializationContext();
		TenantInfo info = new TenantInfo("test.id");
		context.setTenantInfo(info);
		step.delete(data, new TenantDeletionContext(info, true));
		verify(configurationManagement, times(1))
		.removeConfiguration("subsystem.emailintegration.email.tenant.domain.address");
		verify(domainAdministartionService, times(0)).deleteDomain(any(DomainInformation.class));
	}

	@Test
	public void testRollbackWithCreatedAccounts() throws EmailIntegrationException, JSONException {
		DomainInformation mockInfo = Mockito.mock(DomainInformation.class);
		String[] expectedMails = { "test-account1@domain.com", "test-account2@domain.com", "test-account3@domain.com" };
		String[] expectedIds = { "test-account-1-id", "test-account-2-id", "test-account-3-id" };
		List<String> tenantsInDomainReturn = new LinkedList<>();
		tenantsInDomainReturn.add("test-domain");
		List<EmailAddress> getAllEmailsReturn = new LinkedList<>();
		getAllEmailsReturn.add(new EmailAddress(null, null, expectedMails[0], null));
		getAllEmailsReturn.add(new EmailAddress(null, null, expectedMails[1], null));
		getAllEmailsReturn.add(new EmailAddress(null, null, expectedMails[2], null));
		EmailAccountInformation testAcc1 = new EmailAccountInformation();
		testAcc1.setAccountId(expectedIds[0]);
		EmailAccountInformation testAcc2 = new EmailAccountInformation();
		testAcc2.setAccountId(expectedIds[1]);
		EmailAccountInformation testAcc3 = new EmailAccountInformation();
		testAcc3.setAccountId(expectedIds[2]);
		Mockito.reset(accountAdministrationService);
		when(accountAdministrationService.getAccount("test-account1@domain.com")).thenReturn(testAcc1);
		when(accountAdministrationService.getAccount("test-account2@domain.com")).thenReturn(testAcc2);
		when(accountAdministrationService.getAccount("test-account3@domain.com")).thenReturn(testAcc3);
		when(mockInfo.getDomainId()).thenReturn("test-id");
		when(mockInfo.getDomainName()).thenReturn("test-domain");
		when(domainAdministartionService.getDomain(anyString())).thenReturn(Optional.of(mockInfo));
		when(emailAddressResolver.getAllEmailsByTenant(anyString())).thenReturn(getAllEmailsReturn);
		when(emailAddressResolver.getAllTenantsInDomain(anyString())).thenReturn(tenantsInDomainReturn);
		ArgumentCaptor<String> accountAdministrationCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> emailResolverCaptor = ArgumentCaptor.forClass(String.class);

		TenantStepData data = new TenantStepData("tenantdomainAddress",
				new JSONObject("{properties:[{id:'tenantdomainaddress', value:''}]}"));

		TenantInitializationContext context = new TenantInitializationContext();
		TenantInfo info = new TenantInfo("test.id");
		context.setTenantInfo(info);

		step.delete(data, new TenantDeletionContext(info, true));
		verify(accountAdministrationService, times(3)).deleteAccount(accountAdministrationCaptor.capture());
		verify(emailAddressResolver, times(3)).deleteEmailAddress(emailResolverCaptor.capture());
		List<String> administrationServiceCaptures = accountAdministrationCaptor.getAllValues();
		List<String> resolverCaptures = emailResolverCaptor.getAllValues();

		for (int i = 0; i < 3; i++) {
			assertEquals(expectedIds[i], administrationServiceCaptures.get(i));
			assertEquals(expectedMails[i], resolverCaptures.get(i));
		}
	}

	@Test
	public void testRollbackNoProperty() throws RollbackedException, JSONException, EmailIntegrationException {
		DomainInformation mockInfo = Mockito.mock(DomainInformation.class);
		List<String> tenantsInDomainReturn = new LinkedList<>();
		tenantsInDomainReturn.add("test-domain");
		List<EmailAddress> getAllEmailsReturn = new LinkedList<>();
		when(accountAdministrationService.getAccount(anyString())).thenReturn(new EmailAccountInformation());
		when(mockInfo.getDomainId()).thenReturn("test-id");
		when(mockInfo.getDomainName()).thenReturn("test-domain");
		when(domainAdministartionService.getDomain(anyString())).thenReturn(Optional.of(mockInfo));
		when(emailAddressResolver.getAllEmailsByTenant(anyString())).thenReturn(getAllEmailsReturn);
		when(emailAddressResolver.getAllTenantsInDomain(anyString())).thenReturn(tenantsInDomainReturn);
		ArgumentCaptor<DomainInformation> captor = ArgumentCaptor.forClass(DomainInformation.class);

		TenantStepData data = new TenantStepData("tenantdomainAddress",
				new JSONObject("{properties:[{id:'tenantdomainaddress', value:''}]}"));

		TenantInitializationContext context = new TenantInitializationContext();
		TenantInfo info = new TenantInfo("test.id");
		context.setTenantInfo(info);

		step.delete(data, new TenantDeletionContext(info, true));
		verify(configurationManagement, times(1))
		.removeConfiguration("subsystem.emailintegration.email.tenant.domain.address");
		verify(domainAdministartionService).deleteDomain(captor.capture());
		DomainInformation actualDomain = captor.getValue();
		assertEquals("test-id", actualDomain.getDomainId());
	}

}
