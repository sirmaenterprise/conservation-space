package com.sirma.sep.ocr.configuration;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sirma.sep.ocr.service.TesseractOCRProperties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

/**
 * Tests for {@link ConfigurationVerifier}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 20/10/2017
 */
public class ConfigurationVerifierTest {

	@InjectMocks
	private ConfigurationVerifier verifier;
	@Mock
	private TesseractOCRProperties properties;
	// this mock is needed because in the tested class through it we shut down the application and if it is null it
	// will throw a NPE.
	@SuppressWarnings("unused")
	@Mock
	private ApplicationContext applicationContext;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test(expected = ConfigurationException.class)
	public void verify_datapath_isNull() throws Exception {
		when(properties.getDatapath()).thenReturn(null);
		verifier.verify();
	}

	@Test(expected = ConfigurationException.class)
	public void verify_datapath_isEmpty() throws Exception {
		when(properties.getDatapath()).thenReturn("");
		verifier.verify();
	}

	@Test(expected = ConfigurationException.class)
	public void verify_language_isEmpty() throws Exception {
		when(properties.getDatapath()).thenReturn("tessdata");
		when(properties.getLanguage()).thenReturn("");
		verifier.verify();
		verify(properties).setLanguage(any());
	}

	@Test(expected = ConfigurationException.class)
	public void verify_language_isNull() throws Exception {
		when(properties.getDatapath()).thenReturn("tessdata");
		when(properties.getLanguage()).thenReturn(null);
		verifier.verify();
		verify(properties).setLanguage(any());
	}

	@Test(expected = ConfigurationException.class)
	public void verify_mimetype_isNull() throws Exception {
		when(properties.getDatapath()).thenReturn("tessdata");
		when(properties.getLanguage()).thenReturn("");
		when(properties.getMimetype()).thenReturn(null);
		verifier.verify();
	}
}