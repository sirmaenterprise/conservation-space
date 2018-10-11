package com.sirma.sep.email.patch;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.sep.email.address.resolver.EmailAddress;
import com.sirma.sep.email.address.resolver.EmailAddressResolver;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.service.ShareFolderAdministrationService;

import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;

/**
 * Test for {@link CreateSharedContactsFolderPatch}.
 *
 * @author S.Djulgerova
 */
public class CreateSharedContactsFolderPatchTest {

	@InjectMocks
	private CreateSharedContactsFolderPatch createSharedContactsFolderPatch;

	@Mock
	private ShareFolderAdministrationService shareFolderAdministrationService;

	@Mock
	private EmailAddressResolver emailAddressResolver;

	@Mock
	private SecurityContext securityContext;

	@Mock
	private DbDao dbDao;

	@Before
	public void setUp() throws SetupException {
		createSharedContactsFolderPatch = new CreateSharedContactsFolderPatch();
		MockitoAnnotations.initMocks(this);
		when(securityContext.getCurrentTenantId()).thenReturn("tenant.com");
	}

	@Test
	public void testUserAccountsUpdate() throws CustomChangeException, EmailIntegrationException {
		EmailAddress email = new EmailAddress();
		email.setEmailAddress("email@test.com");
		List<EmailAddress> emailAddress = new LinkedList<>();
		emailAddress.add(email);
		when(emailAddressResolver.getAllEmailsByTenant("tenant.com")).thenReturn(emailAddress);

		createSharedContactsFolderPatch.execute(null);
		verify(shareFolderAdministrationService, times(1)).createTenantShareFolder();
		verify(shareFolderAdministrationService, times(1)).addContactToShareFolder("email@test.com");
	}

	@Test
	public void testExecute_NoMailboxSupportable() throws CustomChangeException, EmailIntegrationException {
		when(emailAddressResolver.getAllEmailsByTenant("tenant.com")).thenReturn(new LinkedList<>());

		createSharedContactsFolderPatch.execute(null);
		verify(shareFolderAdministrationService, times(0)).createTenantShareFolder();
	}

}