package com.sirma.sep.email.patch;

import static com.sirma.email.ZimbraEmailIntegrationConstants.CLASS_OF_SERVICE_CONFIG;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.configuration.db.ConfigurationManagement;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.sep.email.address.resolver.EmailAddress;
import com.sirma.sep.email.address.resolver.EmailAddressResolver;
import com.sirma.sep.email.configuration.EmailIntegrationConfiguration;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.model.account.GenericAttribute;
import com.sirma.sep.email.model.domain.ClassOfServiceInformation;
import com.sirma.sep.email.model.domain.DomainInformation;
import com.sirma.sep.email.service.DomainAdministrationService;

import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;

/**
 * Test for {@link SetCOSToExistingTenantsPatch}.
 *
 * @author S.Djulgerova
 */
public class SetCOSToExistingTenantsPatchTest {

	@InjectMocks
	private SetCOSToExistingTenantsPatch setCOSToExistingTenantsPatch;

	@Mock
	private DomainAdministrationService domainAdministrationService;

	@Mock
	private EmailIntegrationConfiguration emailIntegrationConfiguration;

	@Mock
	private SecurityContext securityContext;

	@Mock
	private EmailAddressResolver emailAddressResolver;

	@Mock
	protected ConfigurationManagement configurationManagement;

	@Before
	public void setUp() throws SetupException {
		setCOSToExistingTenantsPatch = new SetCOSToExistingTenantsPatch();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testCosUpdate() throws CustomChangeException, EmailIntegrationException {
		mockData(CLASS_OF_SERVICE_CONFIG);
		setCOSToExistingTenantsPatch.execute(null);
		verify(configurationManagement, times(1)).updateConfiguration(anyObject());
	}

	@Test
	public void testCosUpdate_noMailbox() throws CustomChangeException, EmailIntegrationException {
		when(emailAddressResolver.getAllEmailsByTenant("tenant.com")).thenReturn(new LinkedList<>());
		setCOSToExistingTenantsPatch.execute(null);
		verify(configurationManagement, times(0)).updateConfiguration(anyObject());
	}

	private void mockData(String attributeName) throws EmailIntegrationException {
		when(securityContext.getCurrentTenantId()).thenReturn("tenant.com");

		EmailAddress email = new EmailAddress();
		email.setMailDomain("test_domain.com");
		List<EmailAddress> emailAddress = new LinkedList<>();
		emailAddress.add(email);
		when(emailAddressResolver.getAllEmailsByTenant("tenant.com")).thenReturn(emailAddress);

		List<GenericAttribute> attributes = new LinkedList<>();
		GenericAttribute attr = new GenericAttribute();
		attr.setAttributeName(attributeName);
		attr.setValue("123456");
		attributes.add(attr);
		when(domainAdministrationService.getDomain("test_domain.com"))
				.thenReturn(Optional.of(new DomainInformation("test-id", "test-name", attributes)));

		ClassOfServiceInformation cos = new ClassOfServiceInformation();
		cos.setCosName("default_cos");
		when(domainAdministrationService.getCosById("123456")).thenReturn(cos);

		ConfigurationPropertyMock<String> classOfServiceMock = new ConfigurationPropertyMock<>();
		classOfServiceMock.setName("subsystem.emailintegration.classofservice");
		classOfServiceMock.setValue("default_cos");
		when(emailIntegrationConfiguration.getTenantClassOfService()).thenReturn(classOfServiceMock);
	}

}