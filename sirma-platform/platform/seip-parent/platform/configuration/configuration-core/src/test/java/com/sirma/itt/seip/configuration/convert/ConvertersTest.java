/**
 *
 */
package com.sirma.itt.seip.configuration.convert;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.IntegerPair;
import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.configuration.ConfigurationException;
import com.sirma.itt.seip.configuration.build.ConfigurationInstance;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.time.ISO8601DateFormat;

/**
 * @author BBonev
 */
public class ConvertersTest {

	private static final String TEST_TXT = "test.txt";
	@Mock
	private ConverterContext context;
	@Mock
	private ConfigurationInstance configurationInstance;

	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(context.getConfiguration()).thenReturn(configurationInstance);
	}

	@Test
	public void test_convertString() {
		when(context.getRawValue()).thenReturn("test", "test2", null);
		assertEquals(Converters.convertToString(context), "test");
		assertEquals(Converters.convertToString(context), "test2");
		assertEquals(Converters.convertToString(context), null);
	}

	@Test
	public void test_convertBoolean() {
		when(context.getRawValue()).thenReturn("true", "false", "test", null);
		assertEquals(Converters.convertToBoolean(context), Boolean.TRUE);
		assertEquals(Converters.convertToBoolean(context), Boolean.FALSE);
		assertEquals(Converters.convertToBoolean(context), Boolean.FALSE);
		assertEquals(Converters.convertToBoolean(context), null);
	}

	@Test
	public void test_convertToGeneralFile() throws IOException {
		when(context.getRawValue()).thenReturn(TEST_TXT, TEST_TXT, null);
		File notExit = Converters.convertToGeneralFile(context);
		assertNotNull(notExit);
		assertFalse(notExit.exists());
		try {
			assertTrue(new File(TEST_TXT).createNewFile());
			File file = Converters.convertToGeneralFile(context);
			assertNotNull(file);
			assertEquals(file.getName(), TEST_TXT);
		} finally {
			new File(TEST_TXT).delete();
		}
		assertEquals(Converters.convertToGeneralFile(context), null);
	}

	@Test
	public void test_convertToDirectory_itExists() throws IOException {
		when(context.getRawValue()).thenReturn("readOnly");
		try {
			File readOnly = new File("readOnly");
			assertTrue(readOnly.mkdir());
			File file = Converters.convertToDirectory(context);
			assertNotNull(file);
		} finally {
			assertTrue(new File("readOnly").delete());
		}
	}

	@Test
	public void test_convertToDirectory_itDoesNotExists() throws IOException {
		when(context.getRawValue()).thenReturn("readOnly");
		try {
			File file = Converters.convertToDirectory(context);
			assertNotNull(file);
			assertTrue(file.exists());
			assertTrue(file.isDirectory());
		} finally {
			assertTrue(new File("readOnly").delete());
		}
	}

	@Test(expectedExceptions = ConfigurationException.class)
	public void test_convertToDirectory_butFoundFile() throws IOException {
		when(context.getRawValue()).thenReturn("readOnly.txt");
		try {
			File readOnly = new File("readOnly.txt");
			assertTrue(readOnly.createNewFile());
			Converters.convertToDirectory(context);
		} finally {
			assertTrue(new File("readOnly.txt").delete());
		}
	}

	@Test(expectedExceptions = ConfigurationException.class)
	public void test_convertToFile_butFoundDirectory() throws IOException {
		when(context.getRawValue()).thenReturn("readOnly.txt");
		try {
			File readOnly = new File("readOnly.txt");
			assertTrue(readOnly.mkdir());
			Converters.convertToFile(context);
		} finally {
			assertTrue(new File("readOnly.txt").delete());
		}
	}

	@Test
	public void test_convertToFile_fileDoesNotExists() throws IOException {
		when(context.getRawValue()).thenReturn("readOnly.txt");
		File file = Converters.convertToFile(context);
		assertNull(file);
	}

	@Test
	public void test_convertToFile() throws IOException {
		when(context.getRawValue()).thenReturn("readOnly.txt");
		try {
			File readOnly = new File("readOnly.txt");
			assertTrue(readOnly.createNewFile());
			File file = Converters.convertToFile(context);
			assertNotNull(file);
			assertEquals(file.getName(), "readOnly.txt");
		} finally {
			assertTrue(new File("readOnly.txt").delete());
		}
	}

	@Test
	public void test_convertToReadOnlyFile() throws IOException {
		when(context.getRawValue()).thenReturn("readOnly.txt");
		try {
			File readOnly = new File("readOnly.txt");
			assertTrue(readOnly.createNewFile());
			assertTrue(readOnly.setReadOnly());
			File file = Converters.convertToGeneralFile(context);
			assertNotNull(file);
			assertEquals(file.getName(), "readOnly.txt");
		} finally {
			assertTrue(new File("readOnly.txt").delete());
		}
	}

	@Test
	public void test_convertToFile_withDirectory() throws IOException {
		when(context.getRawValue()).thenReturn(".");
		assertNotNull(Converters.convertToGeneralFile(context));
	}

	@Test
	public void test_convertToInteger() {
		when(context.getRawValue()).thenReturn("1", "2", null);
		assertEquals(Converters.convertToInteger(context), Integer.valueOf(1));
		assertEquals(Converters.convertToInteger(context), Integer.valueOf(2));
		assertEquals(Converters.convertToInteger(context), null);
	}

	@Test
	public void test_convertToInteger_overflowToDefault() {
		when(context.getRawValue()).thenReturn(Integer.MAX_VALUE + "1");
		when(context.getDefaultValue()).thenReturn("1");
		when(context.getConfiguration()).thenReturn(mock(ConfigurationInstance.class));
		assertEquals(Converters.convertToInteger(context), Integer.valueOf(1));
	}

	@Test(expectedExceptions = ConverterException.class)
	public void test_convertToInteger_fail() {
		when(context.getRawValue()).thenReturn("1est");
		Converters.convertToInteger(context);
	}

	@Test
	public void test_convertToLong() {
		when(context.getRawValue()).thenReturn("1", "2", null);
		assertEquals(Converters.convertToLong(context), Long.valueOf(1));
		assertEquals(Converters.convertToLong(context), Long.valueOf(2));
		assertEquals(Converters.convertToLong(context), null);
	}

	@Test
	public void test_convertFromMB() {
		when(context.getRawValue()).thenReturn("1", "565", null);
		assertEquals(Converters.convertMBToBytes(context), Long.valueOf(1048576));
		assertEquals(Converters.convertMBToBytes(context), Long.valueOf(592445440));
		assertEquals(Converters.convertMBToBytes(context), null);
	}

	@Test
	public void test_convertFromKB() {
		when(context.getRawValue()).thenReturn("1", "565", null);
		assertEquals(Converters.convertKBToBytes(context), Long.valueOf(1024));
		assertEquals(Converters.convertKBToBytes(context), Long.valueOf(578560));
		assertEquals(Converters.convertKBToBytes(context), null);
	}

	@Test
	public void test_convertFromGB() {
		when(context.getRawValue()).thenReturn("1", "565", null);
		assertEquals(Converters.convertGBToBytes(context), Long.valueOf(1073741824));
		assertEquals(Converters.convertGBToBytes(context), Long.valueOf(606664130560L));
		assertEquals(Converters.convertGBToBytes(context), null);
	}

	@Test(expectedExceptions = ConverterException.class)
	public void test_convertToLong_fail() {
		when(context.getRawValue()).thenReturn("1est");
		Converters.convertToLong(context);
	}

	@Test
	public void test_convertToDouble() {
		when(context.getRawValue()).thenReturn("1", "2.232323", null);
		assertEquals(Converters.convertToDouble(context), Double.valueOf(1));
		assertEquals(Converters.convertToDouble(context), Double.valueOf(2.232323));
		assertEquals(Converters.convertToDouble(context), null);
	}

	@Test(expectedExceptions = ConverterException.class)
	public void test_convertToDouble_fail() {
		when(context.getRawValue()).thenReturn("1est");
		Converters.convertToDouble(context);
	}

	@Test
	public void test_convertToFloat() {
		when(context.getRawValue()).thenReturn("1", "2.2323", null);
		assertEquals(Converters.convertToFloat(context), Float.valueOf(1f));
		assertEquals(Converters.convertToFloat(context), Float.valueOf(2.2323f));
		assertEquals(Converters.convertToFloat(context), null);
	}

	@Test(expectedExceptions = ConverterException.class)
	public void test_convertToFloat_fail() {
		when(context.getRawValue()).thenReturn("1est");
		Converters.convertToFloat(context);
	}

	@Test
	public void test_convertToShort() {
		when(context.getRawValue()).thenReturn("1", "2", null);
		assertEquals(Converters.convertToShort(context), Short.valueOf((short) 1));
		assertEquals(Converters.convertToShort(context), Short.valueOf((short) 2));
		assertEquals(Converters.convertToShort(context), null);
	}

	@Test(expectedExceptions = ConverterException.class)
	public void test_convertToShort_fail() {
		when(context.getRawValue()).thenReturn("1est");
		Converters.convertToShort(context);
	}

	@Test
	public void test_convertToStringSet() {
		when(context.getRawValue()).thenReturn("value1,value2;value3;;test", "test", null);
		Set<String> set = Converters.convertToStringSet(context);
		assertNotNull(set);
		assertEquals(set.size(), 4);
		assertEquals(set, new LinkedHashSet<>(Arrays.asList("value1", "value2", "value3", "test")));

		set = Converters.convertToStringSet(context);
		assertNotNull(set);
		assertEquals(set.size(), 1);
		assertEquals(set, new LinkedHashSet<>(Arrays.asList("test")));

		set = Converters.convertToStringSet(context);
		assertNull(set);
	}

	@Test
	public void test_convertToStringList() {
		when(context.getRawValue()).thenReturn("value1,value2;value3;;test", "test", null);
		List<String> list = Converters.convertToStringList(context);
		assertNotNull(list);
		assertEquals(list.size(), 4);
		assertEquals(list, Arrays.asList("value1", "value2", "value3", "test"));

		list = Converters.convertToStringList(context);
		assertNotNull(list);
		assertEquals(list.size(), 1);
		assertEquals(list, Arrays.asList("test"));

		list = Converters.convertToStringList(context);
		assertNull(list);
	}

	@Test
	public void test_convertToPattern() {
		when(context.getRawValue()).thenReturn("test", "[\\w]+", null);
		assertNotNull(Converters.convertToPattern(context));
		assertNotNull(Converters.convertToPattern(context));
		assertNull(Converters.convertToPattern(context));
	}

	@Test(expectedExceptions = ConfigurationException.class)
	public void test_convertToPattern_fail() {
		when(context.getRawValue()).thenReturn("$$(");
		Converters.convertToPattern(context);
	}

	@Test
	public void test_convertToUri_valid() {
		when(context.getRawValue())
				.thenReturn("http://localhost:8080", "http://localhost", "https://localhost:8080", null);
		assertNotNull(Converters.convertToURI(context));
		assertNotNull(Converters.convertToURI(context));
		assertNotNull(Converters.convertToURI(context));
		assertNull(Converters.convertToURI(context));
	}

	@Test(expectedExceptions = ConfigurationException.class)
	public void test_convertToUri_invalid() {
		when(context.getRawValue()).thenReturn("://localhost");
		when(context.getConfiguration()).thenReturn(mock(ConfigurationInstance.class));
		assertNotNull(Converters.convertToURI(context));
	}

	@Test
	public void convertToPair() throws Exception {
		when(context.getRawValue()).thenReturn("first-second", "first", "first:second", null);
		when(context.getDefaultValue()).thenReturn("1-2");

		assertEquals(Converters.convertToPair(context), new StringPair("first", "second"));
		assertEquals(Converters.convertToPair(context), new StringPair("first", null));
		assertEquals(Converters.convertToPair(context), new StringPair("first:second", null));
		assertEquals(Converters.convertToPair(context), new StringPair("1", "2"));
	}

	@Test
	public void convertToRange() throws Exception {
		when(context.getRawValue()).thenReturn("1-2", "1", "1-a", "a-2", "1:2", null);
		when(context.getDefaultValue()).thenReturn("2-3");

		assertEquals(Converters.convertToRange(context), new IntegerPair(1, 2));
		assertEquals(Converters.convertToRange(context), new IntegerPair(2, 3));
		assertEquals(Converters.convertToRange(context), new IntegerPair(2, 3));
		assertEquals(Converters.convertToRange(context), new IntegerPair(2, 3));
		assertEquals(Converters.convertToRange(context), new IntegerPair(2, 3));
		assertEquals(Converters.convertToRange(context), new IntegerPair(2, 3));
	}

	@Test
	public void testConvertToUri() {
		final String VALUE = "emf:Document";
		final Uri RESULT = new ShortUri("result");
		when(context.getRawValue()).thenReturn(VALUE);
		TypeConverter converter = mock(TypeConverter.class);
		when(converter.convert(Uri.class, VALUE)).thenReturn(RESULT);

		assertEquals(Converters.convertToURI(context, converter), RESULT);
	}

	@Test
	public void testConvertStringToDate() {
		Date date = new Date();
		final String VALUE = ISO8601DateFormat.format(date);
		final Date RESULT = date;
		when(context.getRawValue()).thenReturn(VALUE);
		assertEquals(Converters.convertStringToDate(context), RESULT);
	}

	@Test(expectedExceptions = ConverterException.class)
	public void convertToEnum_Should_ThrowException_When_TypeIsNotEnum() {
		when(configurationInstance.getType()).then(t -> String.class);
		when(context.getConfiguration()).thenReturn(configurationInstance);

		Converters.convertToEnum(context);
	}

	@Test
	public void convertToEnum_Should_UseRawValue() {
		when(configurationInstance.getType()).then(t -> TestEnum.class);
		when(context.getConfiguration()).thenReturn(configurationInstance);
		when(context.getRawValue()).thenReturn(TestEnum.WRITABLE.toString());

		assertEquals(Converters.convertToEnum(context), TestEnum.WRITABLE);
	}

	@Test
	public void convertToEnum_Should_ReturnNull_When_Value() {
		when(configurationInstance.getType()).then(t -> TestEnum.class);
		when(context.getConfiguration()).thenReturn(configurationInstance);

		assertNull(Converters.convertToEnum(context));
	}

	private enum TestEnum {
		WRITABLE, READ_ONLY
	}

}
