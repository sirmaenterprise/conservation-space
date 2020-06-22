package com.sirma.itt.seip.convert;

import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.AdditionalClasspaths;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Tests the default type converters in {@link DefaultTypeConverter} through the converter API.
 *
 * @author Mihail Radkov
 */
@RunWith(CdiRunner.class)
@AdditionalClasses({ TypeConverterImpl.class, DefaultTypeConverter.class })
@AdditionalClasspaths({ TypeConverterProviders.class })
public class DefaultTypeConvertersComponentTest {

	@Inject
	private TypeConverter typeConverter;

	@Inject
	private TypeConverterProviders typeConverterProviders;

	/**
	 * Simulate {@link com.sirma.itt.seip.runtime.boot.StartupPhase}
	 */
	@Before
	public void beforeEach() {
		TypeConverterImpl.initializeConverter(typeConverterProviders);
	}

	@Test
	public void shouldConvertDifferentCollectionImplementationsToString() {
		List<String> strings = Arrays.asList("1", "2");

		Set<String> stringsSet = new HashSet<>(strings);
		String converted = typeConverter.convert(String.class, (Object) stringsSet);
		// HashSet is with unpredictable order of the elements
		Assert.assertTrue(converted.equals("1,2") || converted.equals("2,1"));

		Set<String> stringsLinkedHashSet = new LinkedHashSet<>(strings);
		converted = typeConverter.convert(String.class, (Object) stringsLinkedHashSet);
		Assert.assertEquals("1,2", converted);

		ArrayList<String> stringsArrayList = new ArrayList<>(strings);
		converted = typeConverter.convert(String.class, (Object) stringsArrayList);
		Assert.assertEquals("1,2", converted);

		LinkedList<String> stringsLinkedList = new LinkedList<>(strings);
		converted = typeConverter.convert(String.class, (Object) stringsLinkedList);
		Assert.assertEquals("1,2", converted);
	}
}
