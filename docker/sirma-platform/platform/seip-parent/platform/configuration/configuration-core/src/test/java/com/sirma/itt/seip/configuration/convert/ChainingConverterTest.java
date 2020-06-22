package com.sirma.itt.seip.configuration.convert;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.configuration.build.ConfigurationInstance;

/**
 * Tests for {@link ChainingConverter}.
 *
 * @author BBonev
 */
public class ChainingConverterTest {

	@Mock
	private ConfigurationValueConverter converter1;
	@Mock
	private ConfigurationValueConverter converter2;
	@Mock
	private TypeConverterContext converterContext;
	@Mock
	private ConfigurationInstance configurationInstance;

	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(converterContext.getConfiguration()).thenReturn(configurationInstance);
		when(configurationInstance.getName()).thenReturn("test");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void test_invalidType() {
		when(converter1.getType()).thenReturn(Number.class);

		new ChainingConverter<>(String.class, converter1);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void test_invalidType_onAdd() {
		when(converter1.getType()).thenReturn(String.class);
		when(converter2.getType()).thenReturn(Number.class);

		ChainingConverter<String> converter = new ChainingConverter<>(String.class, converter1);
		converter.addConverter(converter2);
	}

	@Test
	public void test_validTypes() {
		when(converter1.getType()).thenReturn(String.class);
		when(converter2.getType()).thenReturn(String.class);

		ChainingConverter<String> converter = new ChainingConverter<>(String.class, converter1);
		converter.addConverter(converter2);
	}

	@Test
	public void test_convert() {
		when(converter1.getType()).thenReturn(String.class);
		when(converter2.getType()).thenReturn(String.class);

		ChainingConverter<String> converter = new ChainingConverter<>(String.class, converter1);
		converter.addConverter(converter2);

		when(converter1.convert(any())).thenReturn("value1", (String) null);
		when(converter2.convert(any())).thenReturn("value2");

		assertEquals(converter.convert(converterContext), "value1");

		assertEquals(converter.convert(converterContext), "value2");
	}

	@Test
	public void test_convert_CustomNamed() {
		when(converter1.getType()).thenReturn(String.class);
		when(converter2.getType()).thenReturn(String.class);
		when(converter1.getName()).thenReturn("converterName");
		when(configurationInstance.getConverter()).thenReturn("converterName");

		ChainingConverter<String> converter = new ChainingConverter<>(String.class, converter1);
		converter.addConverter(converter2);

		when(converter1.convert(any())).thenReturn("value1", (String) null);
		when(converter2.convert(any())).thenReturn("value2");

		assertEquals(converter.convert(converterContext), "value1");

		assertEquals(converter.convert(converterContext), "value2");
	}

	@Test(expectedExceptions = ConverterException.class)
	public void test_convert_converterExceptionWhileConverting() {
		when(converter1.getType()).thenReturn(String.class);
		when(converter1.getName()).thenReturn("converterName");
		when(configurationInstance.getConverter()).thenReturn("converterName");
		ChainingConverter<String> converter = new ChainingConverter<>(String.class, converter1);
		when(converter1.convert(any())).thenThrow(new ConverterException(""));
		converter.convert(converterContext);
	}

	@Test(expectedExceptions = ConverterException.class, expectedExceptionsMessageRegExp = "Failed to execute converter converter1")
	public void test_convert_exceptionWhileConverting() {
		when(converter1.getType()).thenReturn(String.class);
		when(converter1.getName()).thenReturn("converterName");
		when(configurationInstance.getConverter()).thenReturn("converterName");
		ChainingConverter<String> converter = new ChainingConverter<>(String.class, converter1);
		when(converter1.convert(any())).thenThrow(new RuntimeException());
		converter.convert(converterContext);
	}

	@Test
	public void test_convert_configurationNamed() {
		when(converter1.getType()).thenReturn(String.class);
		when(converter2.getType()).thenReturn(String.class);
		when(converter1.getName()).thenReturn("test");

		ChainingConverter<String> converter = new ChainingConverter<>(String.class, converter1);
		converter.addConverter(converter2);

		when(converter1.convert(any())).thenReturn("value1", (String) null);
		when(converter2.convert(any())).thenReturn("value2");

		assertEquals(converter.convert(converterContext), "value1");

		assertEquals(converter.convert(converterContext), "value2");
	}

	@Test
	public void test_not_convert() {
		when(converter1.getType()).thenReturn(String.class);
		when(converter2.getType()).thenReturn(String.class);

		ChainingConverter<String> converter = new ChainingConverter<>(String.class, converter1);

		assertEquals(converter.convert(converterContext), null);
	}

}
