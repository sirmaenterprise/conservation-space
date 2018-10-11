package com.sirma.sep.export.converter;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Date;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterImpl;
import com.sirma.itt.seip.domain.util.DateConverter;
import com.sirma.itt.seip.time.FormattedDate;
import com.sirma.itt.seip.time.FormattedDateTime;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for {@link DateToFormattedDateConverter}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 07/11/2017
 */
public class DateToFormattedDateConverterTest {

	@InjectMocks
	private DateToFormattedDateConverter converterProvider;
	@Mock
	private DateConverter dateConverter;

	@Before
	public void setup() {
		converterProvider = new DateToFormattedDateConverter();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void convert_toFormattedDate_nullValue() throws Exception {
		TypeConverter converter = new TypeConverterImpl();
		converterProvider.register(converter);
		FormattedDate convert = converter.convert(FormattedDate.class, (Object) null);
		Assert.assertNull(convert);
	}

	@Test
	public void convert_toFormattedDate() throws Exception {
		Date date = new Date();
		when(dateConverter.formatDate(any())).thenReturn(date.toString());
		TypeConverter converter = new TypeConverterImpl();
		converterProvider.register(converter);
		FormattedDate convert = converter.convert(FormattedDate.class, date);
		Assert.assertEquals(convert.getFormatted(), date.toString());
	}

	@Test
	public void convert_toFormattedDateTime() throws Exception {
		Date date = new Date();
		when(dateConverter.formatDateTime(any())).thenReturn(date.toString());
		TypeConverter converter = new TypeConverterImpl();
		converterProvider.register(converter);
		FormattedDateTime convert = converter.convert(FormattedDateTime.class, date);
		Assert.assertEquals(convert.getFormatted(), date.toString());
	}

	@Test
	public void convert_toFormattedDateTime_nullValue() throws Exception {
		TypeConverter converter = new TypeConverterImpl();
		converterProvider.register(converter);
		FormattedDateTime convert = converter.convert(FormattedDateTime.class, (Object) null);
		Assert.assertNull(convert);
	}
}