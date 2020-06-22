package com.sirma.itt.seip.time.schedule.script;

import static org.testng.Assert.assertEquals;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.seip.script.GlobalBindingsExtension;
import com.sirma.itt.seip.script.ScriptTest;
import com.sirma.itt.seip.time.DateRange;
import com.sirma.itt.seip.time.schedule.DeadlineCalculator;

/**
 * Tests for {@link TimeSchedulerScriptProvider}
 *
 * @author Valeri Tishev
 *
 */
public class TimeSchedulerScriptTest extends ScriptTest {

	@Override
	protected void provideBindings(List<GlobalBindingsExtension> bindingsExtensions) {
		super.provideBindings(bindingsExtensions);

		GlobalBindingsExtension timeSchedulerScriptProvider = new TimeSchedulerScriptProvider();
		ReflectionUtils.setFieldValue(timeSchedulerScriptProvider, "deadlineCalculator", new DeadlineCalculator());
		bindingsExtensions.add(timeSchedulerScriptProvider);
	}

	/**
	 * Test building of a {@link DateRange}
	 *
	 * @param startDateAsString the start date in ISO format
	 * @param endDateAsString the end date in ISO format
	 * @param expectedStartDateAsString the expected start date in ISO format
	 * @param expectedEndDateAsString  the expected end date in ISO format
	 */
	@Test(dataProvider = "dateRangeDataProvider")
	public void buildDateRange(
			String startDateAsString,
			String endDateAsString,
			String expectedStartDateAsString,
			String expectedEndDateAsString) {

		Date startDate = getDateFromString(startDateAsString);
		Date endDate = getDateFromString(endDateAsString);

		Date expectedStartDate = getDateFromString(expectedStartDateAsString);
		Date expectedEndDate = getDateFromString(expectedEndDateAsString);

		final String javaScript = "buildDateRange(startDate, endDate);";

		Map<String, Object> bindings = new LinkedHashMap<>();
		bindings.put("startDate", startDate);
		bindings.put("endDate", endDate);

		DateRange actualDeadRange = (DateRange) eval(javaScript, bindings);

		assertEquals(actualDeadRange.getFirst(), expectedStartDate);
		assertEquals(actualDeadRange.getSecond(), expectedEndDate);
	}

	/**
	 * Test {@link TimeSchedulerScriptProvider#calculateDeadLine(Date, int, boolean, boolean, boolean)}
	 *
	 * @param startDateAsString start date in ISO format
	 * @param duration the duration [days]
	 * @param mindWorkdayExclusions mind or not workday exclusions
	 * @param startOnWorkdayExclusion start or not on a workday exlusion
	 * @param endOnWorkdayExclusion end or not on a workday exclusion
	 * @param expectedDeadlineAsString expected calculated deadline date in ISO format
	 */
	@Test(dataProvider = "deadLineDataProvider")
	public void timeSchedulerSetDeadline(
			String startDateAsString,
			int duration,
			boolean mindWorkdayExclusions,
			boolean startOnWorkdayExclusion,
			boolean endOnWorkdayExclusion,
			String expectedDeadlineAsString) {

		final Date expectedDeadline = getDateFromString(expectedDeadlineAsString);
		final Date startDate = getDateFromString(startDateAsString);

		Map<String, Object> bindings = new LinkedHashMap<>();
		bindings.put("startDate", startDate);
		bindings.put("duration", duration);
		bindings.put("mindWorkdayExclusions", mindWorkdayExclusions);
		bindings.put("startOnWorkdayExclusion", startOnWorkdayExclusion);
		bindings.put("endOnWorkdayExclusion", endOnWorkdayExclusion);


		final String javaScript =
				"timeScheduler.calculateDeadLine(startDate, duration, mindWorkdayExclusions, startOnWorkdayExclusion, endOnWorkdayExclusion);" ;
		Date actualDeadline = (Date) eval(javaScript, bindings);

		Assert.assertEquals(actualDeadline, expectedDeadline);
	}

	@DataProvider(name = "dateRangeDataProvider")
	private Object[][] dateRangeDataProvider() {
		return new Object[][] {
			{"2016-02-08T14:21:23.456", "2016-02-09T16:17:18.976", "2016-02-08T00:00:00.000", "2016-02-09T23:59:59.999"},
			{null, "2016-02-09T16:17:18.976", null, "2016-02-09T23:59:59.999"},
			{"2016-02-08T14:21:23.456", null, "2016-02-08T00:00:00.000", null}
		};
	}

	@DataProvider(name = "deadLineDataProvider")
	private Object[][] deadLineDataProvider() {
		return new Object[][] {
			{"2016-06-15T14:21:23.456", 2, false, false, false, "2016-06-17T14:21:23.456"}
		};
	}

	/**
	 * Gets a date from its ISO string representation
	 *
	 * @param dateAsString the date in ISO format
	 * @return the date object
	 */
	private Date getDateFromString(String dateAsString) {
		if (dateAsString != null) {
			return DatatypeConverter.parseDateTime(dateAsString).getTime();
		}

		return null;
	}

}
