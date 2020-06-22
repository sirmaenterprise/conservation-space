package com.sirma.itt.seip.search.converters;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.joda.time.DateTime;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.time.DateRange;

/**
 * Converter from {@link JsonObject} to {@link DateRange} for operators "before". The json have to look like:
 *
 * <pre>
 * {
 * 	"field": "emf:createdOn",
 * 	"operator": "before",
 * 	"type": "dateTime",
 * 	"value": ["today",1,"days"]
 * 	}
 * </pre>
 *
 * @author Boyan Tonchev
 */
@ApplicationScoped
@Extension(target = AbstractDateRangeConverter.PLUGIN_NAME, order = 3)
public class WithinDateConverter extends AbstractDateRangeConverter {

	private static final int COUNT_STEP = 1;
	private static final int KIND_STEP = 2;

	private static final List<String> SUPPORTED_OPERATIONS = Arrays.asList("within");

	private static final String DAYS = "days";
	private static final String HOURS = "hours";
	private static final String WEEKS = "weeks";
	private static final String MONTHS = "months";
	private static final String YEARS = "years";

	private static final String TODAY = "today";
	private static final String NEXT = "next";
	private static final String LAST = "last";
	private static final String AFTER = "after";
	private static final String BEFORE = "before";

	@Override
	protected List<String> getSupportedOperators() {
		return SUPPORTED_OPERATIONS;
	}

	@Override
	public DateRange convert(DateTime dateTime, JsonObject filterCriteria) {
		JsonArray dateRangeJson = filterCriteria.getJsonArray(JsonKeys.VALUE);
		if (!filterCriteria.containsKey(JsonKeys.VALUE) || dateRangeJson.size() != 3) {
			return new DateRange(null, null);
		}

		return convertInternal(dateTime, dateRangeJson);
	}

	/**
	 * Convert internal.
	 *
	 * @param dateTime
	 *            - base date for calculation.
	 * @param dateRangeJson
	 *            - can be:
	 *
	 *            <pre>
	 * ["today",1,"days"]
	 * ["next",1,"days"]
	 * ["next",1,"hours"]
	 * ["next",1,"weeks"]
	 * ["next",1,"months"]
	 * ["next",1,"years"]
	 * ["last",1,"years"]
	 * ["last",1,"months"]
	 * ["last",1,"weeks"]
	 * ["last",1,"days"]
	 * ["last",1,"hours"]
	 * ["after",1,"hours"]
	 * ["after",1,"days"]
	 * ["after",1,"weeks"]
	 * ["after",1,"months"]
	 * ["after",1,"years"]
	 * ["before",1,"years"]
	 * ["before",1,"months"]
	 * ["before",1,"weeks"]
	 * ["before",1,"days"]
	 * ["before",1,"hours"]
	 *            </pre>
	 *
	 * @return the date range object.
	 */
	public static DateRange convertInternal(DateTime dateTime, JsonArray dateRangeJson) {
		DateRange dateRange = new DateRange(null, null);
		switch (dateRangeJson.getString(0)) {
			case TODAY:
				dateRange = new DateRange(dateTime.minusDays(1).toDate(), dateTime.toDate());
				break;
			case NEXT:
				dateRange = createNextRange(dateTime, dateRangeJson.getInt(COUNT_STEP),
						dateRangeJson.getString(KIND_STEP));
				break;
			case LAST:
				dateRange = createLastRange(dateTime, dateRangeJson.getInt(COUNT_STEP),
						dateRangeJson.getString(KIND_STEP));
				break;
			case AFTER:
				dateRange = createAfterRange(dateTime, dateRangeJson.getInt(COUNT_STEP),
						dateRangeJson.getString(KIND_STEP));
				break;
			case BEFORE:
				dateRange = createBeforeRange(dateTime, dateRangeJson.getInt(COUNT_STEP),
						dateRangeJson.getString(KIND_STEP));
				break;
			default:
				break;
		}

		return dateRange;
	}

	/**
	 * Create next range object. Start date will be <code>dateTime</code> and end date will be <code>countStep</code> of
	 * <code>kindStep</code> bigger than <code>dateTime</code>. For example if <code>countStep</code> is 2 and
	 * <code>dateTime</code> weeks resulted range will represent all two week after <code>dateTime</code>.
	 *
	 * @param dateTime
	 *            - base date for calculation.
	 * @param countStep
	 *            - how bigger than <code>dateTime</code>
	 * @param kindStep
	 *            - kind of step (days, hours ....)
	 * @return created {@link DateRange} object.
	 */
	public static DateRange createNextRange(DateTime dateTime, int countStep, String kindStep) {
		Date endDate = null;
		switch (kindStep) {
			case DAYS:
				endDate = dateTime.plusDays(countStep).toDate();
				break;
			case HOURS:
				endDate = dateTime.plusHours(countStep).toDate();
				break;
			case WEEKS:
				endDate = dateTime.plusWeeks(countStep).toDate();
				break;
			case MONTHS:
				endDate = dateTime.plusMonths(countStep).toDate();
				break;
			case YEARS:
				endDate = dateTime.plusYears(countStep).toDate();
				break;
			default:
				break;
		}

		return new DateRange(dateTime.toDate(), endDate);
	}

	/**
	 * Create last range object. Start date will be with <code>countStep</code> of <code>kindStep</core>
	 * smaller than <code>dateTime</code> and end date will be <code>dateTime</code>. For example: if
	 * <code>countStep</code> is 3 and <code>kindStep</core> days resulted object.
	 * will represents all 3 days before <code>dateTime</code>.
	 *
	 * @param dateTime
	 *            - base date for calculation.
	 * @param countStep-
	 *            - how smaller than <code>dateTime</code>
	 * @param kindStep
	 *            - kind of step (days, hours ....)
	 * @return created {@link DateRange} object.
	 */
	public static DateRange createLastRange(DateTime dateTime, int countStep, String kindStep) {
		Date startDate = null;
		switch (kindStep) {
			case DAYS:
				startDate = dateTime.minusDays(countStep).toDate();
				break;
			case HOURS:
				startDate = dateTime.minusHours(countStep).toDate();
				break;
			case WEEKS:
				startDate = dateTime.minusWeeks(countStep).toDate();
				break;
			case MONTHS:
				startDate = dateTime.minusMonths(countStep).toDate();
				break;
			case YEARS:
				startDate = dateTime.minusYears(countStep).toDate();
				break;
			default:
				break;
		}

		return new DateRange(startDate, dateTime.toDate());
	}

	/**
	 * Create after range object. Start date will be <code>countStep</code> of <code>kindStep</code> bigger than
	 * <code>dateTime</code> second will be null. For example if <code>countStep</code> is 3 and <code>kindStep</code>
	 * years the resulted object will represent all years bigger more than 3 form <code>dateTime</code>.
	 *
	 * @param dateTime
	 *            - base date for calculation.
	 * @param countStep
	 *            - how bigger than <code>dateTime</code>
	 * @param kindStep
	 *            - kind of step (days, hours ....)
	 * @return created {@link DateRange} object.
	 */
	public static DateRange createAfterRange(DateTime dateTime, int countStep, String kindStep) {
		Date startDate = null;
		switch (kindStep) {
			case DAYS:
				startDate = dateTime.plusDays(countStep).toDate();
				break;
			case HOURS:
				startDate = dateTime.plusHours(countStep).toDate();
				break;
			case WEEKS:
				startDate = dateTime.plusWeeks(countStep).toDate();
				break;
			case MONTHS:
				startDate = dateTime.plusMonths(countStep).toDate();
				break;
			case YEARS:
				startDate = dateTime.plusYears(countStep).toDate();
				break;
			default:
				break;
		}

		return new DateRange(startDate, null);
	}

	/**
	 * Create before range object. Start date will be null second date will be <code>countStep</code> of
	 * <code>kindStep</code> smaller than <code>dateTime</code>. For example if <code>countStep</code> 3 and
	 * <code>kindStep</code> years the resulted object will represent all years before 3 years ago.
	 *
	 * @param dateTime
	 *            - base date for calculation.
	 * @param countStep
	 *            - how smaller than <code>dateTime</code>
	 * @param kindStep
	 *            - kind of step (days, hours ....)
	 * @return created {@link DateRange} object.
	 */
	public static DateRange createBeforeRange(DateTime dateTime, int countStep, String kindStep) {
		Date endDate = null;
		switch (kindStep) {
			case DAYS:
				endDate = dateTime.minusDays(countStep).toDate();
				break;
			case HOURS:
				endDate = dateTime.minusHours(countStep).toDate();
				break;
			case WEEKS:
				endDate = dateTime.minusWeeks(countStep).toDate();
				break;
			case MONTHS:
				endDate = dateTime.minusMonths(countStep).toDate();
				break;
			case YEARS:
				endDate = dateTime.minusYears(countStep).toDate();
				break;
			default:
				break;
		}

		return new DateRange(null, endDate);
	}

}
