package com.sirma.itt.seip.domain.semantic.persistence;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterImpl;
import com.sirma.itt.seip.security.UserPreferences;

/**
 * Tests for the {@link MultiLanguageValueConverter};
 *
 * @author nvelkov
 */
public class MultiLanguageValueConverterTest {

	@Mock
	private UserPreferences userPreferences;

	@InjectMocks
	private MultiLanguageValueConverter converterProvider = new MultiLanguageValueConverter();

	/**
	 * Init annotations.
	 */
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test the conversion with an english value.
	 */
	@Test
	public void testConversion() {
		Mockito.when(userPreferences.getLanguage()).thenReturn("en");

		MultiLanguageValue value = new MultiLanguageValue();
		value.addValue("en", "value");

		TypeConverter converter = new TypeConverterImpl();
		converterProvider.register(converter);

		Assert.assertEquals("value", converter.convert(String.class, value));
	}

	/**
	 * Test the conversion with a missing value for the given language. In that case the first value from the map should
	 * be returned.
	 */
	@Test
	public void testConversionNoLabelForGivenLanguage() {
		Mockito.when(userPreferences.getLanguage()).thenReturn("ger");

		MultiLanguageValue value = new MultiLanguageValue();
		value.addValue("en", "value");
		value.addValue("bg", "value2");

		TypeConverter converter = new TypeConverterImpl();
		converterProvider.register(converter);

		Assert.assertEquals("value", converter.convert(String.class, value));
	}

	/**
	 * Test the conversion with a multivalued value. In that case all the values separated by comma should be returned.
	 */
	@Test
	public void testConversionMultiValuedValue() {
		Mockito.when(userPreferences.getLanguage()).thenReturn("en");
		TypeConverter converter = new TypeConverterImpl();
		converterProvider.register(converter);

		MultiLanguageValue value = new MultiLanguageValue();
		value.addValue("en", "value");
		Assert.assertEquals("value", converter.convert(String.class, value));

		value.addValue("en", "value2");
		Assert.assertEquals("value2,value", converter.convert(String.class, value));
	}
}
