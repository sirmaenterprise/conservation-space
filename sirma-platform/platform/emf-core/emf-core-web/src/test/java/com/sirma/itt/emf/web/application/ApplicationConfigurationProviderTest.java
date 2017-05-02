package com.sirma.itt.emf.web.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.SystemConfiguration;

/**
 * Test the {@link ApplicationConfigurationProvider}.
 * 
 * @author Sinan, Georgi, cdimitrov
 */
public class ApplicationConfigurationProviderTest {

	private static final String HOST = "HOST";
	private static final String SEARCH_PATH = "HOST#/search";

	@InjectMocks
	private ApplicationConfigurationProvider testProvider;

	@Mock
	private SystemConfiguration systemConfiguration;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void isBase64Test() {
		String base64 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACIAAAAiCAQAAACQTsNJAAAFEElEQVRIx43Wa2zV9R3H8X+yxW0uy8iWLLPJHmwPlpXtgZE5UBaGgFbrkNuYyM1IOWUDmYrIpWOrpVBulk6oUqBSTtHOQlsOFmx7Slfuo0gPF9tCuVhAEAsnShmUwJm89oCixoDj8/T3zTv5/vLJ9/MJglvqetKJUHM4FtsUr0xsSZTEt8YOh9tC7UnBnUrKnshODdrQH4zBWYtErY+0p/xfwJXkpkil5Sj0e6SCHrhigBUue9Z7kdPJX4M4E6pKHDXTVeSbbIY8+WLuxWEhHQqFDRJNVIRug2jNrsanfoW/ewEHvSHXFvu8bLqI//qFt02zGkuyb4FozP63G9pooL0qZErTQ5XHPC3qoHRPqvO6sx6Q4WOlX8UcC+12U1EVRomY4UUFcrBYiYHOoNQUBVq7Jku+vFRHcnXiJiJDJTaboFSuJJBvhcHINd0lKT7pmj2ZePeLL26JzLQAVWZrctxJY6TbYZF8xAwSkafECE/p7zWvudCFeS/ShTiXchjVHpJutWn6K9YkS5E1OO2MelMtUm6cMdYa5dfG4W9+abG3b/imNlLueU9ZJE+Fbhb4g8Pm6452B2T7jV5e8pGV0qXKNFKzdIWI2hQJguBa0iEHDcZMh4w0yFyP+qlUhSZbo8G7pqNDlvtlmSFNhZl6ynHUSBW2JgWHQm8o0yLDXEPN9D2LVeirJ86iw3qbZfuO3qImStPTcMPk2+QbekhVHwoaw2zTS7El1nrIeHeZZp+eroB25wzxmGIRkzzgFe226IeXUOm8+eGgPdbbMOyzTonj/uktY3U39HPXVFngZUUG6KdKkR+b74gqvXHWZ07Fgg/iZUYbaq1PrLRUN7WiprtbbhcmLtNwgXL1Sj1uvdd9yy59u94b48G2RJrzauQYaJJxJrlfgaOWeNYqnXZapcJgK9RpMd/vrHbaOI2e6YI0J4LtiTJkKrZHq3lynfSwJ9Tqp9x2u4UNMsgQuSZ63CFFTsjDlC7I0UTQGo8KyVejwSZzNDkkxzDdjDBVtm8qRqm7POqCLE2KbLfHcYtlimN/PDgdm2Ws56yzW5l1jlplo3QjhDHYMrudcsCfjPeOvfKcN8NVdba56B7znIoFLeElWOfP5rjsTdma7DbKz611zQQPCvvYVKelyXDBQvuV22yHvxpikTrHwkEiVO0euzR6zl675IvJtNFY1JprvHRMcp+nZejwiixbtclRrADUhAJJZba4og9CWGmpw8b5i0JrVOvnBcvEzFLjH97UZLbjSn1gg2Kpoj5KCoKgM9LshxZ4S4PJLsmy0yzl3pFvnt/6rpCLxig0Q5F5tjlimf3qDbVc5Y1jcD2lwSnnzPGEA5arsMYOP7NfuWP+aI/7bLdGpVHmWOmAeS7orlW1Op/ejJD2SKNMG+SpUKmHPgZ40GoLXZOqVoE0D3vRQhONMBtc00uO5sgXcZV8KtFmvj7iuOwR+S7oq8oEwxVo8X1FenrEt9V0WazTq+oSvpxAQkdkuPr5sS4z1xQ/0N8Sz5tqtuVmKfGMg14Fl23nq+kj+32tPsNSAWjQZp2FphrtXhsdBCP8BO02unqr5JF9XMR17xvtQ/xI1IdOqnfMNdztXzr9x3V71JB9uygPSWzV7BIuG22tkzqN9iTOKzRem3YbnE8IfV0jSBbpcEKtfaIaXXdRhXJ0qrRVgxYiku+gWohAoxqbRO1Sr1a1OESk3HlHSRISFhOXkBAXExZym5LzPxb9/3/Q7kdaAAAAAElFTkSuQmCC";
		assertTrue(ApplicationConfigurationProvider.isBase64Encoded(base64));
	}

	@Test
	public void base64URLTest() {
		String url = "http://testLink.test/testLogo.png";
		assertFalse(ApplicationConfigurationProvider.isBase64Encoded(url));
	}

	@Test
	public void relativePathCheck() {
		String relPath = "images/logo.png";
		assertFalse(ApplicationConfigurationProvider.isBase64Encoded(relPath));
	}

	@Test
	public void testGetUi2Url_verifySystemConfigurationInvoked() {
		ConfigurationProperty<String> configProperty = mock(ConfigurationProperty.class);
		when(systemConfiguration.getUi2Url()).thenReturn(configProperty);
		when(configProperty.requireConfigured(anyString())).thenReturn(configProperty);

		testProvider.getUi2Url();

		verify(systemConfiguration).getUi2Url();
	}

	@Test
	public void testUi2SearchOpenUrl() {
		ConfigurationProperty<String> configProperty = mock(ConfigurationProperty.class);
		when(systemConfiguration.getUi2Url()).thenReturn(configProperty);
		when(configProperty.requireConfigured(anyString())).thenReturn(configProperty);
		when(configProperty.get()).thenReturn(HOST);
		assertEquals(testProvider.getUi2SearchOpenUrl(), SEARCH_PATH);
	}
}
