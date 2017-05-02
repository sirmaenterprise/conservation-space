package com.sirma.itt.seip.search.converters;

import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.joda.time.DateTime;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.time.DateRange;

/**
 * Converter from {@link JsonObject} to {@link DateRange} for operators "between" and "is". The json have to look like:
 * *
 *
 * <pre>
 * {
 * 	"field": "emf:createdOn",
 * 	"operator": "is",
 * 	"type": "dateTime",
 * 	"value": ["2016-12-05T22:00:00.000Z", "2016-12-06T21:59:59.999Z"]
 * 	}
 * </pre>
 *
 * OR
 *
 * <pre>
 * {
 * 	"field": "emf:createdOn",
 * 	"operator": "between",
 * 	"type": "dateTime",
 * 	"value": ["2016-12-05T22:00:00.000Z","2016-12-13T22:01:00.000Z"]
 * 	}
 * </pre>
 *
 * @author Boyan Tonchev
 */
@ApplicationScoped
@Extension(target = AbstractDateRangeConverter.PLUGIN_NAME, order = 2)
public class BetweenDateRangeConverter extends AbstractDateRangeConverter {

	private static final List<String> SUPPORTED_OPERATIONS = Arrays.asList("between", "is");

	/**
	 * {@inheritDoc}
	 *
	 * @param filterCriteria
	 *            - the {@link JsonObject} which have to be converted. It look like:
	 *
	 *            <pre>
	 * {
	 * 	"field": "emf:createdOn",
	 * 	"operator": "is",
	 * 	"type": "dateTime",
	 * 	"value": ["2016-12-05T22:00:00.000Z", "2016-12-06T21:59:59.999Z"]
	 * 	}
	 *            </pre>
	 *
	 *            OR
	 *
	 *            <pre>
	 * {
	 * 	"field": "emf:createdOn",
	 * 	"operator": "between",
	 * 	"type": "dateTime",
	 * 	"value": ["2016-12-05T22:00:00.000Z","2016-12-13T22:01:00.000Z"]
	 * 	}
	 *            </pre>
	 *
	 * @param dateTime
	 *            is not used in current implementation.
	 */
	@Override
	public DateRange convert(DateTime current, JsonObject filterCriteria) {
		DateRange result = new DateRange(null, null);
		if (filterCriteria.containsKey(JsonKeys.VALUE)) {
			JsonArray dateRangeJson = filterCriteria.getJsonArray(JsonKeys.VALUE);
			result.setFirst(convertToDate(dateRangeJson.getString(0)));
			result.setSecond(convertToDate(dateRangeJson.getString(1)));
		}

		return result;
	}

	@Override
	protected List<String> getSupportedOperators() {
		return SUPPORTED_OPERATIONS;
	}
}
