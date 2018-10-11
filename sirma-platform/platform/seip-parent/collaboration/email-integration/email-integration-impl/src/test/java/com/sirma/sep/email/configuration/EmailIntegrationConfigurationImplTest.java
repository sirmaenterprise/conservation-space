package com.sirma.sep.email.configuration;

import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.convert.GroupConverterContext;
import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.sep.account.administration.AccountAuthenticationService;
import com.sirma.sep.email.address.resolver.EmailAddressResolver;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.service.EmailAccountAdministrationService;
import com.sirma.sep.email.service.MailboxSupportableService;
import com.sirma.sep.email.service.ShareFolderAdministrationService;

/**
 * Tests for EmailIntegrationConfigurationImpl.
 *
 * @author svelikov
 */
public class EmailIntegrationConfigurationImplTest {

	private static final String TENANT_ADMIN_NAME = "tenant-admin-tenant.com";
	private static final String TENANT_DOMAIN = "tenant.com";
	private static final String GENERATED_PASSWORD = "generated-password";

	@InjectMocks
	private EmailIntegrationConfigurationImpl emailIntegrationConfigurationImpl;

	@Mock
	private ConfigurationManagement configurationManagement;

	@Mock
	private ConfigurationProperty<String> tenantDomainAddress;

	@Mock
	private ConfigurationProperty<String> testEmailPrefix;

	@Mock
	private EmailAddressResolver emailAddressResolver;

	@Mock
	private MailboxSupportableService mailboxSupportableService;

	@Mock
	private EmailAccountAdministrationService emailAccountAdministrationService;

	@Mock
	private AccountAuthenticationService accountAuthenticationService;

	@Mock
	private ShareFolderAdministrationService shareFolderAdministrationService;

	@SuppressWarnings("serial")
	@Before
	public void setup() {
		emailIntegrationConfigurationImpl = new EmailIntegrationConfigurationImpl() {
			@Override
			String createPassword() {
				return GENERATED_PASSWORD;
			}
		};
		MockitoAnnotations.initMocks(this);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test_createTenantAdminAccount_account_is_created() throws EmailIntegrationException {
		GroupConverterContext context = mock(GroupConverterContext.class);
		ConfigurationPropertyMock<Object> name = new ConfigurationPropertyMock<>();
		ConfigurationPropertyMock<Object> pass = new ConfigurationPropertyMock<>();
		when(context.getValue("subsystem.emailintegration.email.tenant.admin.name")).thenReturn(name);
		when(context.getValue("subsystem.emailintegration.email.tenant.admin.password")).thenReturn(pass);

		SecurityContext securityContext = mock(SecurityContext.class);
		when(securityContext.getCurrentTenantId()).thenReturn(TENANT_DOMAIN);

		when(tenantDomainAddress.get()).thenReturn(TENANT_DOMAIN);
		when(testEmailPrefix.get()).thenReturn("stage");

		when(emailAddressResolver.getEmailAddress(anyString())).thenReturn(null);
		when(mailboxSupportableService.isMailboxSupportable(anyString())).thenReturn(true);

		User tenantAdminUser = emailIntegrationConfigurationImpl.createTenantAdminAccount(context, securityContext);

		// Then: configurations should be created
		verify(configurationManagement, times(1)).addConfigurations(anyCollection());
		// Then: account should be created
		verify(emailAccountAdministrationService, times(1))
				.createTenantAdminAccount("tenant-admin-tenant.com-stage@tenant.com", GENERATED_PASSWORD);
		verify(shareFolderAdministrationService, times(1)).createTenantShareFolder();
		// Then: port should be reset
		verify(accountAuthenticationService, times(1)).resetTenantAdminPort(TENANT_ADMIN_NAME, GENERATED_PASSWORD);
		// Then: should return User with name and password populated
		Assert.assertEquals(TENANT_ADMIN_NAME, tenantAdminUser.getName());
		Assert.assertEquals(GENERATED_PASSWORD, tenantAdminUser.getCredentials());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void test_createTenantAdminAccount_account_exists() throws EmailIntegrationException {
		GroupConverterContext context = mock(GroupConverterContext.class);
		ConfigurationPropertyMock<Object> name = new ConfigurationPropertyMock<>(TENANT_ADMIN_NAME, true);
		ConfigurationPropertyMock<Object> pass = new ConfigurationPropertyMock<>(GENERATED_PASSWORD, true);
		when(context.getValue("subsystem.emailintegration.email.tenant.admin.name")).thenReturn(name);
		when(context.getValue("subsystem.emailintegration.email.tenant.admin.password")).thenReturn(pass);

		SecurityContext securityContext = mock(SecurityContext.class);

		User tenantAdminUser = emailIntegrationConfigurationImpl.createTenantAdminAccount(context, securityContext);

		// Then: should return User with name and password populated
		Assert.assertEquals(TENANT_ADMIN_NAME, tenantAdminUser.getName());
		Assert.assertEquals(GENERATED_PASSWORD, tenantAdminUser.getCredentials());
		// Then: configurations should not be updated
		verify(configurationManagement, never()).addConfigurations(anyCollection());
		// Then: account should not be recreated
		verify(emailAccountAdministrationService, never()).createTenantAdminAccount(anyString(), anyString());
		verify(shareFolderAdministrationService, never()).createTenantShareFolder();
	}

}
