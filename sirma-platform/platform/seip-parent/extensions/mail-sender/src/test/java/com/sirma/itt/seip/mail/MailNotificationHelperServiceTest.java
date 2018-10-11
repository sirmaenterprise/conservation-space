package com.sirma.itt.seip.mail;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.Instance;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.instance.util.LinkProviderService;
import com.sirma.itt.seip.mail.MailNotificationHelperService;

/**
 * Tests for {@link MailNotificationHelperService}.
 *
 * @author A. Kunchev
 */
public class MailNotificationHelperServiceTest {

	@InjectMocks
	private MailNotificationHelperService service;

	@Mock
	private Instance<LinkProviderService> linkProviderServiceInstance;

	@Mock
	private LinkProviderService linkProviderService;
	@Mock
	private SystemConfiguration systemConfiguration;

	@Before
	@SuppressWarnings("rawtypes")
	public void setup() {
		service = new MailNotificationHelperService();
		MockitoAnnotations.initMocks(this);
		ConfigurationProperty property = mock(ConfigurationProperty.class);
		when(property.get()).thenReturn("http://localhost/");
		when(systemConfiguration.getUi2Url()).thenReturn(property);

	}

	@Test
	public void buildFullURI_unsatisfiedService() {
		Mockito.when(linkProviderServiceInstance.isUnsatisfied()).thenReturn(true);
		assertEquals("", service.buildFullURI(new EmfInstance()));
	}

	@Test
	public void buildFullURI_nullInstance() {
		Mockito.when(linkProviderServiceInstance.isUnsatisfied()).thenReturn(false);
		assertEquals("", service.buildFullURI(null));
	}

	@Test
	public void buildFullURI_nullInstanceAndUnsatisfiedService() {
		Mockito.when(linkProviderServiceInstance.isUnsatisfied()).thenReturn(true);
		assertEquals("", service.buildFullURI(null));
	}

	@Test
	public void buildFullURI_returnsLinkToInstance() {
		EmfInstance instance = new EmfInstance();
		Mockito.when(linkProviderServiceInstance.isUnsatisfied()).thenReturn(false);
		Mockito.when(linkProviderService.buildLink(instance)).thenReturn("instance-link");
		Mockito.when(linkProviderServiceInstance.get()).thenReturn(linkProviderService);
		assertEquals("http://localhost/instance-link", service.buildFullURI(instance));
	}

}
