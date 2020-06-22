package com.sirma.sep.instance.relations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterUtil;

/**
 * Test for {@link PropertiesSerializationUtil}.
 *
 * @author A. Kunchev
 */
public class PropertiesSerializationUtilTest {

	@Mock
	private TypeConverter typeConverter;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void convertObjectProperty_notSupportedPropertyType() {
		TypeConverterUtil.setTypeConverter(typeConverter);
		Stream<Serializable> property = PropertiesSerializationUtil.convertObjectProperty(new Integer(11));
		assertFalse(property.findFirst().isPresent());
	}

	@Test
	public void convertObjectProperty_singleValueProperty_notConverted() {
		when(typeConverter.tryConvert(ShortUri.class, "emf:singleValue")).thenReturn(null);
		TypeConverterUtil.setTypeConverter(typeConverter);
		Stream<Serializable> property = PropertiesSerializationUtil.convertObjectProperty("emf:singleValue");
		assertFalse(property.findFirst().isPresent());
	}

	@Test
	public void convertObjectProperty_singleValuePropertyString() {
		when(typeConverter.tryConvert(ShortUri.class, "emf:singleValue")).thenReturn(new ShortUri("emf:singleValue"));
		TypeConverterUtil.setTypeConverter(typeConverter);
		Stream<Serializable> property = PropertiesSerializationUtil.convertObjectProperty("emf:singleValue");
		assertTrue(property.findFirst().isPresent());
	}

	@Test
	public void convertObjectProperty_singleValuePropertyUri() {
		when(typeConverter.tryConvert(eq(ShortUri.class), any(ShortUri.class)))
				.thenReturn(new ShortUri("emf:singleValue"));
		TypeConverterUtil.setTypeConverter(typeConverter);
		Stream<Serializable> property = PropertiesSerializationUtil
				.convertObjectProperty(new ShortUri("emf:singleValue"));
		assertTrue(property.findFirst().isPresent());
	}

	@Test
	public void convertObjectProperty_multiValuePropertyString() {
		when(typeConverter.tryConvert(eq(ShortUri.class), anyString())).thenReturn(new ShortUri("emf:value1"),
				new ShortUri("emf:value2"));
		TypeConverterUtil.setTypeConverter(typeConverter);
		Stream<Serializable> property = PropertiesSerializationUtil
				.convertObjectProperty((Serializable) Arrays.asList("emf:value1", "emf:value2"));
		assertEquals(2, property.count());
	}
}
