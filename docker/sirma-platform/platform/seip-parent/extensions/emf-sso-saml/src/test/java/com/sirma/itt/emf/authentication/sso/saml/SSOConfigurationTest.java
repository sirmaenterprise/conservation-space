package com.sirma.itt.emf.authentication.sso.saml;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.configuration.convert.ConverterContext;

@RunWith(MockitoJUnitRunner.class)
public class SSOConfigurationTest {

	@InjectMocks
	private SSOConfiguration configuration;

	@Mock
	private SystemConfiguration systemConfiguration;

	@Mock
	private ConfigurationProperty<String> proto;

	@Mock
	private ConfigurationProperty<String> host;

	@Mock
	private ConfigurationProperty<Integer> port;

	@Mock
	private ConfigurationProperty<String> contextPath;

	@Before
	public void init() {
		Mockito.when(systemConfiguration.getDefaultProtocol()).thenReturn(proto);
		Mockito.when(systemConfiguration.getDefaultHost()).thenReturn(host);
		Mockito.when(systemConfiguration.getDefaultPort()).thenReturn(port);
		Mockito.when(systemConfiguration.getDefaultContextPath()).thenReturn(contextPath);

		Mockito.when(proto.get()).thenReturn("http");
		Mockito.when(host.get()).thenReturn("localhost");
		Mockito.when(port.get()).thenReturn(8080);
		Mockito.when(contextPath.get()).thenReturn("/emf");
	}

	@Test
	public void testBuildAssertionUrl_raw_value() {
		ConverterContext context = Mockito.mock(ConverterContext.class);
		Mockito.when(context.getRawValue()).thenReturn("http://localhost/emf/ServiceLogin");

		Assert.assertEquals("http://localhost/emf/ServiceLogin", configuration.buildAssertionUrl(context));
	}

	@Test
	public void testBuildAssertionUrl_from_configurations() {
		ConverterContext context = Mockito.mock(ConverterContext.class);

		final String[] rawValues = new String[]{ null, "", "     ", "\t", "\n" };
		for (String raw : rawValues) {
			Mockito.when(context.getRawValue()).thenReturn(raw);
			Assert.assertEquals("http://localhost:8080/emf/ServiceLogin", configuration.buildAssertionUrl(context));
		}
	}

	@Test
	public void testBuildAssertionUrl_normalization() {
		ConverterContext context = Mockito.mock(ConverterContext.class);
		Mockito.when(context.getRawValue()).thenReturn(null);

		final String[] contextValues = new String[]{ "/emf", "emf", "/emf/" };
		for (String value : contextValues) {
			Mockito.when(contextPath.get()).thenReturn(value);
			Assert.assertEquals("http://localhost:8080/emf/ServiceLogin", configuration.buildAssertionUrl(context));
		}
	}
}
