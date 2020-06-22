package com.sirma.sep.email.service;

import static com.sirma.email.ZimbraEmailIntegrationConstants.MAIL_DELIVERY_ADDRESS;
import static com.sirma.email.ZimbraEmailIntegrationConstants.PREF_FROM_ADDRESS;
import static com.sirma.email.ZimbraEmailIntegrationConstants.PREF_FROM_DISPLAY;
import static com.sirma.sep.email.EmailIntegrationConstants.DISPLAY_NAME;
import static com.sirma.sep.email.EmailIntegrationConstants.EMAIL_ADDRESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.permissions.InstanceAccessEvaluator;
import com.sirma.sep.email.exception.EmailIntegrationException;
import com.sirma.sep.email.model.account.EmailAccountInformation;
import com.sirma.sep.email.model.account.GenericAttribute;

public class EmailAccountDelegationServiceTest {
	@InjectMocks
	private EmailAccountDelegationService accountDelegationService;
	@Mock
	private EmailAccountAdministrationService administrationService;
	@Mock
	private InstanceAccessEvaluator accessEvaluator;
	@Mock
	private InstanceTypeResolver instanceTypeResolver;
	@Mock
	private DomainInstanceService domainInstanceService;

	@Before
	public void setup() {
		accountDelegationService = new EmailAccountDelegationServiceImpl();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void modifyAccountTest() throws EmailIntegrationException {
		prepareMocks(true, true);
		ArgumentCaptor<String> target = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> granteeCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Boolean> flagCaptor = ArgumentCaptor.forClass(boolean.class);
		accountDelegationService.modifyAccountDelegationPermission("testTarget@domain.com", "testGranteeId", true);
		verify(administrationService).modifyDelegatePermission(target.capture(), granteeCaptor.capture(),
				flagCaptor.capture());

		assertEquals("testTarget@domain.com", target.getValue());
		assertEquals("testMail@domain.com", granteeCaptor.getValue());
		assertTrue(flagCaptor.getValue());
	}

	@Test(expected = EmailIntegrationException.class)
	public void failModifyDelegateTest() throws EmailIntegrationException {
		prepareMocks(true, true);
		doThrow(EmailIntegrationException.class).when(administrationService).modifyDelegatePermission(anyString(),
				anyString(), any(boolean.class));
		accountDelegationService.modifyAccountDelegationPermission("testTarget@domain.com", "testGranteeId", true);
	}

	@Test(expected = EmailIntegrationException.class)
	public void missingEmalAddressTest() throws EmailIntegrationException {
		prepareMocks(true, false);
		accountDelegationService.modifyAccountDelegationPermission("testTarget@domain.com", "testGranteeId", true);
	}

	@Test
	public void getEmailAddressAttributesTest_External() throws EmailIntegrationException {
		EmailAccountInformation emailInfo = mockEmailAccountInformation(PREF_FROM_ADDRESS, "external@domain.com",
				PREF_FROM_DISPLAY, "External");
		when(administrationService.getAccount("testTarget@domain.com")).thenReturn(emailInfo);

		Map<String, String> result = accountDelegationService.getEmailAccountAttributes("testTarget@domain.com");
		assertEquals(result.get(EMAIL_ADDRESS), "external@domain.com");
		assertEquals(result.get(DISPLAY_NAME), "External");
	}

	@Test
	public void getEmailAddressAttributesTest_NoExternal() throws EmailIntegrationException {
		EmailAccountInformation emailInfo = mockEmailAccountInformation(MAIL_DELIVERY_ADDRESS, "testTarget@domain.com",
				DISPLAY_NAME, "testTarget");
		when(administrationService.getAccount("testTarget@domain.com")).thenReturn(emailInfo);

		Map<String, String> result = accountDelegationService.getEmailAccountAttributes("testTarget@domain.com");
		assertEquals(result.get(EMAIL_ADDRESS), "testTarget@domain.com");
		assertEquals(result.get(DISPLAY_NAME), "testTarget");
	}

	private void prepareMocks(boolean canRead, boolean hasEmail) {
		InstanceReference instanceReference = mock(InstanceReference.class);
		Optional<InstanceReference> instanceReferenceOptional = Optional.of(instanceReference);
		when(instanceTypeResolver.resolveReference(any(Serializable.class))).thenReturn(instanceReferenceOptional);
		Instance mockInstance = mockInstance(hasEmail);
		when(accessEvaluator.canRead(instanceReference, mockInstance)).thenReturn(canRead);
		when(domainInstanceService.loadInstance(anyString())).thenReturn(mockInstance);
	}

	private static Instance mockInstance(boolean hasEmail) {
		Instance instance = new EmfInstance();
		if (hasEmail) {
			instance.add("emailAddress", "testMail@domain.com");
		}
		instance.add("id", "testGranteeId");
		return instance;
	}

	private EmailAccountInformation mockEmailAccountInformation(String from, String fromValue, String displayName,
			String displayNameValue) {
		EmailAccountInformation emailInfo = new EmailAccountInformation();
		List<GenericAttribute> attributes = new LinkedList<>();
		GenericAttribute attr = new GenericAttribute();
		attr.setAttributeName(from);
		attr.setValue(fromValue);
		attributes.add(attr);
		attr = new GenericAttribute();
		attr.setAttributeName(displayName);
		attr.setValue(displayNameValue);
		attributes.add(attr);
		emailInfo.setAttributes(attributes);
		return emailInfo;
	}
}
