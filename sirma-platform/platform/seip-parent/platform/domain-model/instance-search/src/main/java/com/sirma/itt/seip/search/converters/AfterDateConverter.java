package com.sirma.itt.seip.search.converters;

import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.json.JsonObject;

import org.joda.time.DateTime;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.time.DateRange;

/**
 * Converter from {@link JsonObject} to {@link DateRange} for operators "after". The json have to look like:
 *
 * <pre>
 * {
 * 	"field": "emf:createdOn",
 * 	"operator": "after",
 * 	"type": "dateTime",
 * 	"value": "2016-12-06T22:00:00.000Z"
 * 	}
 * </pre>
 *
 * @author Boyan Tonchev
 */
@ApplicationScoped
@Extension(target = AbstractDateRangeConverter.PLUGIN_NAME, order = 1)
public class AfterDateConverter extends AbstractDateRangeConverter {

	private static final List<String> SUPPORTED_OPERATIONS = Arrays.asList("after");

	/**
	 * {@inheritDoc}
	 *
	 * @param filterCriteria
	 *            - the {@link JsonObject} which have to be converted. It look like:
	 *
	 *            <pre>
	 * {
	 * 	"field": "emf:createdOn",
	 * 	"operator": "after",
	 * 	"type": "dateTime",
	 * 	"value": "2016-12-06T22:00:00.000Z"
	 * 	}
	 *            </pre>
	 *
	 * @param dateTime
	 *            is not used in current implementation.
	 */
	@Override
	public DateRange convert(DateTime dateTime, JsonObject filterCriteria) {
		return filterCriteria.containsKey(JsonKeys.VALUE)
				? new DateRange(convertToDate(filterCriteria.getString(JsonKeys.VALUE)), null)
				: new DateRange(null, null);
	}

	@Override
	protected List<String> getSupportedOperators() {
		return SUPPORTED_OPERATIONS;
	}
}
