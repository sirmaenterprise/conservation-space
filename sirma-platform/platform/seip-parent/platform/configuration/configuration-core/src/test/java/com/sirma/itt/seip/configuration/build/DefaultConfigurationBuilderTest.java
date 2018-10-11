/**
 *
 */
package com.sirma.itt.seip.configuration.build;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;

import java.util.Optional;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.convert.ConfigurationValueConverter;
import com.sirma.itt.seip.configuration.convert.PropertyConverterProvider;

/**
 * @author BBonev
 */
@Test
public class DefaultConfigurationBuilderTest {

	@Mock
	ConfigurationInstance configuration;
	@Mock
	RawConfigurationAccessor configurationValueProvider;
	@Mock
	PropertyConverterProvider converterProvider;
	@Mock
	ConfigurationProvider configurationProvider;
	@Mock
	ConfigurationInstanceProvider configurationInstanceProvider;
	@Mock
	ConfigurationValueConverter<String> valueConverter;

	@InjectMocks
	DefaultConfigurationBuilder builder;

	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void test_builder() {

		when(configuration.getName()).thenReturn("name");
		when(configuration.getType()).then(a -> String.class);
		when(valueConverter.getType()).then(a -> String.class);
		when(converterProvider.getConverter(String.class)).thenReturn(valueConverter);
		when(converterProvider.getConverter(any(String.class))).thenReturn(Optional.empty());
		when(valueConverter.convert(any())).thenReturn("value");

		ConfigurationProperty<Object> property = builder
				.buildProperty(configuration, configurationValueProvider, converterProvider, configurationProvider,
						configurationInstanceProvider);
		assertNotNull(property);
		assertEquals(property.getName(), "name");
		assertEquals(property.get(), "value");
	}

	@Test
	public void test_builder_customConverter() {

		when(configuration.getName()).thenReturn("name");
		when(configuration.getType()).then(a -> String.class);
		when(configuration.getConverter()).thenReturn("customConverter");
		when(valueConverter.getType()).then(a -> String.class);
		when(converterProvider.getConverter("customConverter")).thenReturn(Optional.of(valueConverter));

		when(valueConverter.convert(any())).thenReturn("value");

		ConfigurationProperty<Object> property = builder
				.buildProperty(configuration, configurationValueProvider, converterProvider, configurationProvider,
						configurationInstanceProvider);
		assertNotNull(property);
		assertEquals(property.getName(), "name");
		assertEquals(property.get(), "value");
	}

	@Test
	public void test_builder_customConverter_noTypeMatch() {

		when(configuration.getName()).thenReturn("name");
		when(configuration.getType()).then(a -> String.class);
		when(configuration.getConverter()).thenReturn("customConverter");
		// the named converter will report type different than the one expected
		when(valueConverter.getType()).then(a -> Long.class);

		when(converterProvider.getConverter(String.class)).thenReturn(valueConverter);
		when(converterProvider.getConverter("customConverter")).thenReturn(Optional.of(valueConverter));

		when(valueConverter.convert(any())).thenReturn("value");

		ConfigurationProperty<Object> property = builder
				.buildProperty(configuration, configurationValueProvider, converterProvider, configurationProvider,
						configurationInstanceProvider);
		assertNotNull(property);
		assertEquals(property.getName(), "name");
		assertEquals(property.get(), "value");
	}

	@Test
	public void resolveConverter_Should_GetConverterFrom_ConverterConfig() {
		when(converterProvider.getConverter(configuration.getConverter())).thenReturn(Optional.of(valueConverter));
		when(configuration.getType()).then(t -> String.class);
		when(valueConverter.getType()).then(t -> String.class);

		ConfigurationValueConverter<Object> actualValueConverter = DefaultConfigurationBuilder
				.resolveConverter(configuration, converterProvider);

		verify(converterProvider, never()).getConverter(String.class);
		assertEquals(actualValueConverter, valueConverter);
	}

	@Test
	public void resolveConverter_Should_GetConverterFrom_Provider_When_MissingInConfig() {
		when(converterProvider.getConverter(configuration.getConverter())).thenReturn(Optional.empty());
		when(configuration.getType()).then(t -> String.class);
		when(valueConverter.getType()).then(t -> String.class);

		ConfigurationValueConverter<Object> actualValueConverter = DefaultConfigurationBuilder
				.resolveConverter(configuration, converterProvider);

		verify(converterProvider).getConverter(String.class);
		assertNotEquals(actualValueConverter, valueConverter);
	}

	@Test
	public void resolveConverter_Should_WorkWithEnum() {
		when(converterProvider.getConverter(configuration.getConverter())).thenReturn(Optional.of(valueConverter));
		when(configuration.getType()).then(t -> TestEnum.class);
		when(valueConverter.getType()).then(t -> TestEnum.class);

		ConfigurationValueConverter<Object> actualValueConverter = DefaultConfigurationBuilder
				.resolveConverter(configuration, converterProvider);

		verify(converterProvider, never()).getConverter(String.class);
		assertEquals(actualValueConverter, valueConverter);
	}

	private enum TestEnum {
		WRITABLE, READ_ONLY
	}

}
