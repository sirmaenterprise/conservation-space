package com.sirma.itt.seip.time.schedule;

import static org.testng.Assert.assertEquals;
import static com.sirma.itt.seip.time.schedule.OperationType.ADD;
import static com.sirma.itt.seip.time.schedule.OperationType.SUB;

import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link DeadlineCalculator}
 *
 * @author Valeri Tishev
 *
 */
public class DeadlineCalculatorTest {

	private static final DeadlineCalculator CUT = new DeadlineCalculator();

	/**
	 * Test date calculation.
	 *
	 * @param start
	 *            the start date
	 * @param duration
	 *            the period duration [in days]
	 * @param mindWorkdayExclusions
	 *            mind or not workday exclusions
	 * @param operation
	 *            addition or subtraction
	 * @param end
	 *            the expected end date
	 */
	@Test(dataProvider = "testDataProvider")
	public void testDateCalculation(String start, int duration, boolean mindWorkdayExclusions, OperationType operation,
			String end) {
		Date startDate = getDateFromString(start);
		Date expectedDeadline = getDateFromString(end);
		Date actualDeadline = CUT.calculateDate(startDate, duration, mindWorkdayExclusions, operation);
		assertEquals(actualDeadline, expectedDeadline);
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

	/**
	 * Test data provider.
	 *
	 * @return the test data
	 */
	@DataProvider
	private Object[][] testDataProvider() {
		return new Object[][] {
			{ "2015-06-26T00:00:00.000", 7, false, ADD, "2015-07-06T00:00:00.000" },
			{ "2015-06-26T00:00:00.000", 7, true, ADD, "2015-07-07T00:00:00.000" },
			{ "2015-09-03T00:00:00.000", 7, false, ADD, "2015-09-10T00:00:00.000" },
			{ "2015-09-04T00:00:00.000", 7, false, ADD, "2015-09-14T00:00:00.000" },
			{ "2015-09-04T00:00:00.000", 7, true, ADD, "2015-09-15T00:00:00.000" },
			{ "2015-09-07T00:00:00.000", 3, false, ADD, "2015-09-10T00:00:00.000" },
			{ "2016-02-17T00:00:00.000", 4, false, ADD, "2016-02-22T00:00:00.000" },
			{ "2016-03-08T00:00:00.000", 7, false, SUB, "2016-03-01T00:00:00.000" },
			{ "2016-03-10T00:00:00.000", 7, true, SUB, "2016-03-01T00:00:00.000" }
		};
	}

}
