package com.sirma.sep.instance.properties.expression.evaluation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.util.DateConverter;
import com.sirma.itt.seip.security.UserPreferences;
import com.sirma.sep.instance.properties.expression.evaluation.UserDateConverter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for {@link UserDateConverter}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 03/10/2017
 */
public class UserDateConverterTest {

	@InjectMocks
	private UserDateConverter cut;
	@Mock
	private DateConverter dateConverter;
	@Mock
	private UserPreferences userPreferences;
	@Mock
	private TypeConverter typeConverter;

	@Before
	public void init() {
		cut = new UserDateConverter();
		MockitoAnnotations.initMocks(this);

		// Work with fixed date to avoid concurrency issues: The 1st of January 2017.
		Date fixedDate = new Date(1483228800000L);

		// mock for the system's short datetime format
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		when(dateConverter.getSystemShortFormat()).thenReturn(dateFormat);
		when(userPreferences.getUserTimezone()).thenReturn(mock(TimeZone.class));
		when(typeConverter.convert(Date.class, "date")).thenReturn(fixedDate);
	}

	@Test
	public void testEvaluateDateWithZoneOffset_success() {
		String date = cut.evaluateDateWithZoneOffset("date");
		assertEquals("01/01/2017", date);
		verify(dateConverter).getSystemShortFormat();
		verify(userPreferences).getUserTimezone();
		verify(typeConverter).convert(eq(Date.class), any(String.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEvaluateDateWithZoneOffset_nullValue() {
		cut.evaluateDateWithZoneOffset(null);
	}
}