package com.sirma.sep.email.patch;

import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.sep.email.address.resolver.EmailAddress;
import com.sirma.sep.email.address.resolver.EmailAddressResolver;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.model.account.EmailAccountInformation;
import com.sirma.sep.email.service.EmailAccountAdministrationService;

import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;

/**
 * Test for {@link EnableSharedAddressBookPatch}.
 *
 * @author S.Djulgerova
 */
public class EnableSharedAddressBookPatchTest {

	@InjectMocks
	private EnableSharedAddressBookPatch enableSharedAddressBookPatch;

	@Mock
	private EmailAccountAdministrationService emailAccountAdministrationService;

	@Mock
	private SecurityContext securityContext;

	@Mock
	private EmailAddressResolver emailAddressResolver;

	@Before
	public void setUp() throws SetupException {
		enableSharedAddressBookPatch = new EnableSharedAddressBookPatch();
		MockitoAnnotations.initMocks(this);
		when(securityContext.getCurrentTenantId()).thenReturn("test-tenant.com");
	}

	@Test
	public void testUserAccountsUpdate() throws CustomChangeException, EmailIntegrationException {
		List<EmailAddress> tenantEmails = new LinkedList<>();
		tenantEmails.add(new EmailAddress(null, null, "test-email@domain.com", null));
		when(emailAddressResolver.getAllEmailsByTenant("test-tenant.com")).thenReturn(tenantEmails);
		when(emailAccountAdministrationService.getAccount("test-email@domain.com"))
				.thenReturn(Mockito.mock(EmailAccountInformation.class));
		enableSharedAddressBookPatch.execute(null);
		verify(emailAccountAdministrationService, times(1)).modifyAccount(anyObject(), anyList());
	}

	@Test
	public void testExecute_NoMailboxAddresses() throws CustomChangeException, EmailIntegrationException {
		when(emailAddressResolver.getAllEmailsByTenant("test-tenant.com")).thenReturn(Collections.emptyList());
		enableSharedAddressBookPatch.execute(null);
		verify(emailAccountAdministrationService, times(0)).modifyAccount(anyObject(), anyList());
	}

}