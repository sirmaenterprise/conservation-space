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
 * Converter from {@link JsonObject} to {@link DateRange} for operators "before". The json have to look like:
 *
 * <pre>
 * {
 * 	"field": "emf:createdOn",
 * 	"operator": "before",
 * 	"type": "dateTime",
 * 	"value": "2016-12-06T22:00:00.000Z"
 * 	}
 * </pre>
 *
 * @author Boyan Tonchev
 */
@ApplicationScoped
@Extension(target = AbstractDateRangeConverter.PLUGIN_NAME, order = 0)
public class BeforeDateConverter extends AbstractDateRangeConverter {

	private static final List<String> SUPPORTED_OPERATIONS = Arrays.asList("before");

	/**
	 * {@inheritDoc}
	 *
	 * @param filterCriteria
	 *            - the {@link JsonObject} which have to be converted.
	 *
	 *            <pre>
	 * {
	 * 	"field": "emf:createdOn",
	 * 	"operator": "before",
	 * 	"type": "dateTime",
	 * 	"value": "2016-12-06T22:00:00.000Z"
	 * 	}
	 *            </pre>
	 *
	 * @param dateTime
	 *            is not used in current implementation. It look like:
	 */
	@Override
	public DateRange convert(DateTime current, JsonObject filterCriteria) {
		return filterCriteria.containsKey(JsonKeys.VALUE)
				? new DateRange(null, convertToDate(filterCriteria.getString(JsonKeys.VALUE)))
				: new DateRange(null, null);
	}

	@Override
	protected List<String> getSupportedOperators() {
		return SUPPORTED_OPERATIONS;
	}
}
