package com.sirma.itt.seip.expressions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.util.DateConverter;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.security.UserPreferences;

/**
 * Tests for {@link DateEvaluator}.
 *
 * @author smustafov
 */
public class DateEvaluatorTest {

	private static final String FORMAT_SHORT = "yy-dd-MM";
	private static final String FORMAT_FULL = "yyyy-dd-MM HH:mm";
	private static final String FORMAT_DEFAULT = "yyyy-dd-MM";

	@InjectMocks
	private DateEvaluator evaluator = new DateEvaluator();

	@Mock
	private DateConverter dateConverter;

	@Mock
	private javax.enterprise.inject.Instance<ExpressionsManager> expressionManagerInjector;

	@Mock
	private TypeConverter converter;

	@Mock
	private UserPreferences userPreferences;

	private ExpressionsManager expressionManager;
	private EmfInstance instance;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);

		expressionManager = mock(ExpressionsManager.class);
		when(expressionManagerInjector.get()).thenReturn(expressionManager);
		when(userPreferences.getLanguage()).thenReturn("en");

		instance = new EmfInstance();
		instance.add(DefaultProperties.PLANNED_END_DATE, new Date());
		when(expressionManager.evaluateRule(anyString(), eq(Serializable.class), any(ExpressionContext.class),
				anyVararg())).thenReturn(instance);

		when(userPreferences.getUserTimezone()).thenReturn(TimeZone.getTimeZone("UTC"));
		when(dateConverter.getSystemDefaultFormat()).thenReturn(new SimpleDateFormat(FORMAT_DEFAULT));
		when(dateConverter.getSystemFullFormat()).thenReturn(new SimpleDateFormat(FORMAT_FULL));
		when(dateConverter.getSystemShortFormat()).thenReturn(new SimpleDateFormat(FORMAT_SHORT));
	}

	@Test
	public void testExpressionId() {
		assertEquals("date", evaluator.getExpressionId());
	}

	@Test
	public void testGetPattern() {
		assertNotNull(evaluator.getPattern());
	}

	@Test
	public void testEvaluate_withSeriesOfFormating() {
		Date currentDate = (Date) instance.get(DefaultProperties.PLANNED_END_DATE);

		String format = "EEE, dd.MM.yyyy, HH:mm aaa";
		Serializable result = evaluator.evaluate("${date([plannedEndDate]).format(" + format + ")}");
		assertEquals("<span data-property=\"plannedEndDate\" data-format=\"EEE, dd.MM.yyyy, HH:mm aaa\">"
				+ buildExpectedFormat(currentDate, format) + "</span>", result);

		format = "dd. MM  .yyyy     HH:  mm";
		result = evaluator.evaluate("${date([plannedEndDate]).format(" + format + ")}");
		assertEquals("<span data-property=\"plannedEndDate\" data-format=\"dd. MM  .yyyy     HH:  mm\">"
				+ buildExpectedFormat(currentDate, format) + "</span>", result);

		format = "dd\\MM\\yyyy HH:mm K-Z X s_z";
		result = evaluator.evaluate("${date([plannedEndDate]).format(" + format + ")}");
		assertEquals("<span data-property=\"plannedEndDate\" data-format=\"dd\\MM\\yyyy HH:mm K-Z X s_z\">"
				+ buildExpectedFormat(currentDate, format) + "</span>", result);

		format = "dd/MM/yyyy hh:mm";
		result = evaluator.evaluate("${date([plannedEndDate]).format(" + format + ")}");
		assertEquals("<span data-property=\"plannedEndDate\" data-format=\"dd/MM/yyyy hh:mm\">"
				+ buildExpectedFormat(currentDate, format) + "</span>", result);

		format = " dd-MM-yyyy kk:mm ";
		result = evaluator.evaluate("${date([plannedEndDate]).format(" + format + ")}");
		assertEquals("<span data-property=\"plannedEndDate\" data-format=\" dd-MM-yyyy kk:mm \">"
				+ buildExpectedFormat(currentDate, format) + "</span>", result);

		format = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
		result = evaluator.evaluate("${date([plannedEndDate]).format(" + format + ")}");
		assertEquals("<span data-property=\"plannedEndDate\" data-format=\"yyyy-MM-dd'T'HH:mm:ss.SSSZ\">"
				+ buildExpectedFormat(currentDate, format) + "</span>", result);

		format = "sYYYY-MM-DD'T'hh:mm:ss.sss'T'ZD";
		result = evaluator.evaluate("${date([plannedEndDate]).format(" + format + ")}");
		assertEquals("<span data-property=\"plannedEndDate\" data-format=\"sYYYY-MM-DD'T'hh:mm:ss.sss'T'ZD\">"
				+ buildExpectedFormat(currentDate, format) + "</span>", result);

		format = "GG yyyy YY MMM LL ww W D dd F EE u aa HH kk KK hh mm ss SS z Z XX";
		result = evaluator.evaluate("${date([plannedEndDate]).format(" + format + ")}");
		assertEquals(
				"<span data-property=\"plannedEndDate\" data-format=\"GG yyyy YY MMM LL ww W D dd F EE u aa HH kk KK hh mm ss SS z Z XX\">"
						+ buildExpectedFormat(currentDate, format) + "</span>",
				result);

		format = "default";
		result = evaluator.evaluate("${date([plannedEndDate]).format(" + format + ")}");
		assertEquals(
				"<span data-property=\"plannedEndDate\" data-format=\"" + FORMAT_DEFAULT + "\">"
						+ buildExpectedFormat(currentDate, FORMAT_DEFAULT) + "</span>",
						result);

		format = "full";
		result = evaluator.evaluate("${date([plannedEndDate]).format(" + format + ")}");
		assertEquals(
				"<span data-property=\"plannedEndDate\" data-format=\"" + FORMAT_FULL + "\">"
						+ buildExpectedFormat(currentDate, FORMAT_FULL) + "</span>",
						result);

		format = "short";
		result = evaluator.evaluate("${date([plannedEndDate]).format(" + format + ")}");
		assertEquals(
				"<span data-property=\"plannedEndDate\" data-format=\"" + FORMAT_SHORT + "\">"
						+ buildExpectedFormat(currentDate, FORMAT_SHORT) + "</span>",
						result);
	}

	@Test
	public void testEvaluate_withPredefinedFormat() {
		Date currentDate = (Date) instance.get(DefaultProperties.PLANNED_END_DATE);
		String defaultFormat = "dd MM yyyy HH:mm";
		String shortFormat = "dd MM yyyy";
		String fullFormat = "dd MM yyyy HH:mm:ss";

		when(dateConverter.getSystemDefaultFormat()).thenReturn(createDateFormat(defaultFormat));
		when(dateConverter.getSystemShortFormat()).thenReturn(createDateFormat(shortFormat));
		when(dateConverter.getSystemFullFormat()).thenReturn(createDateFormat(fullFormat));

		Serializable result = evaluator.evaluate("${date([plannedEndDate]).format(default)}");
		assertEquals("<span data-property=\"plannedEndDate\" data-format=\"dd MM yyyy HH:mm\">"
				+ buildExpectedFormat(currentDate, defaultFormat) + "</span>", result);

		result = evaluator.evaluate("${date([plannedEndDate]).format(short)}");
		assertEquals("<span data-property=\"plannedEndDate\" data-format=\"dd MM yyyy\">"
				+ buildExpectedFormat(currentDate, shortFormat) + "</span>", result);

		result = evaluator.evaluate("${date([plannedEndDate]).format(full)}");
		assertEquals("<span data-property=\"plannedEndDate\" data-format=\"dd MM yyyy HH:mm:ss\">"
				+ buildExpectedFormat(currentDate, fullFormat) + "</span>", result);
	}

	@Test
	public void testEvaluate_withNoMatches() {
		String format = "0 49 8372";
		Serializable result = evaluator.evaluate("${date([plannedEndDate]).format(" + format + ")}");
		assertNull(result);

		format = "! ^& ()*  %!@ #+~`";
		result = evaluator.evaluate("${date([plannedEndDate]).format(" + format + ")}");
		assertNull(result);

		format = "!^&132()*%0980!@#+~`2355";
		result = evaluator.evaluate("${date([plannedEndDate]).format(" + format + ")}");
		assertNull(result);

		format = "A B C I J N O P Q R T U V b c e f g i j l n o p q r t v x";
		result = evaluator.evaluate("${date([plannedEndDate]).format(" + format + ")}");
		assertNull(result);

		format = "ABCIJNOPQRTUVbcefgijlnopqrtvx";
		result = evaluator.evaluate("${date([plannedEndDate]).format(" + format + ")}");
		assertNull(result);
	}

	@Test
	public void testEvaluate_withEmptyExpression() {
		assertNull(evaluator.evaluate(""));
	}

	@Test
	public void testEvaluate_withEmptyFormat() {
		String defaultFormat = "dd MM yyyy HH:mm";
		Date currentDate = (Date) instance.get(DefaultProperties.PLANNED_END_DATE);
		when(dateConverter.getSystemDefaultFormat()).thenReturn(createDateFormat(defaultFormat));

		Serializable result = evaluator.evaluate("${date([plannedEndDate]).format()}");
		assertNull(result);

		result = evaluator.evaluate("${date([plannedEndDate]).format( )}");
		assertEquals("<span data-property=\"plannedEndDate\" data-format=\"" + defaultFormat + "\">"
				+ buildExpectedFormat(currentDate, defaultFormat) + "</span>", result);
	}

	@Test
	public void testEvaluate_withNoFormat() {
		Date currentDate = (Date) instance.get(DefaultProperties.PLANNED_END_DATE);
		String defaultFormat = "dd MM yyyy HH:mm";
		when(dateConverter.getSystemDefaultFormat()).thenReturn(createDateFormat(defaultFormat));

		Serializable result = evaluator.evaluate("${date([startDate])}");
		assertEquals("<span data-property=\"startDate\" data-format=\"" + defaultFormat + "\"></span>", result);
	}

	@Test
	public void testEvaluate_withNotExistingProperty() {
		String format = "dd MM yyyy HH:mm";
		Serializable result = evaluator.evaluate("${date([startDate]).format(" + format + ")}");
		assertEquals("<span data-property=\"startDate\" data-format=\"" + format + "\"></span>", result);
	}

	@Test
	public void testEvaluate_withDateAsString() {
		String format = "dd MM yyyy HH:mm";
		Date newDate = new Date();
		instance.add(DefaultProperties.PLANNED_END_DATE, newDate.toString());

		when(converter.convert(Date.class, newDate.toString())).thenReturn(newDate);

		Serializable result = evaluator.evaluate("${date([plannedEndDate]).format(" + format + ")}");
		assertEquals("<span data-property=\"plannedEndDate\" data-format=\"" + format + "\">"
				+ buildExpectedFormat(newDate, format) + "</span>", result);
	}

	private static String buildExpectedFormat(Date date, String pattern) {
		return createDateFormat(pattern).format(date);
	}

	private static DateFormat createDateFormat(String pattern) {
		SimpleDateFormat format = new SimpleDateFormat(pattern, new Locale("en"));
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		return format;
	}

}
