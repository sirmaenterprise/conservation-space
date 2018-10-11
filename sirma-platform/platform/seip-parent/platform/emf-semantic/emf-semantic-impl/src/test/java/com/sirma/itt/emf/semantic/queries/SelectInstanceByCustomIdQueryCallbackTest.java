package com.sirma.itt.emf.semantic.queries;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.emf.semantic.persistence.ValueConverter;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterImpl;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.search.NamedQueries;
import com.sirma.itt.seip.search.NamedQueries.Params;

/**
 * Test for {@link SelectInstanceByCustomIdQueryCallback}
 *
 * @author BBonev
 */
public class SelectInstanceByCustomIdQueryCallbackTest {

	SparqlQueryFilterProvider filterProvider = new SparqlQueryFilterProvider();

	@Before
	public void beforeMethod() {
		TypeConverter converter = new TypeConverterImpl();
		new ValueConverter().register(converter);
		TypeConverterUtil.setTypeConverter(converter);
	}

	@Test
	public void buildSingle() throws Exception {
		SelectInstanceByCustomIdQueryCallback callback = new SelectInstanceByCustomIdQueryCallback();
		Map<String, Object> properties = new HashMap<>();
		assertNull(callback.singleValue(null, properties, Collections.emptyList()));
		assertNull(callback.singleValue("", properties, Collections.emptyList()));
	}

	@Test
	public void queryBuilding() throws Exception {
		SelectInstanceByCustomIdQueryCallback callback = new SelectInstanceByCustomIdQueryCallback();
		Map<String, Object> properties = new HashMap<>();
		properties.put(Params.PROPERTY_ID, "emf:customId");
		properties.put(Params.IS_URI, Boolean.TRUE);
		List<Serializable> values = Arrays.asList("emf:value1", "http://address#value2");

		StringBuilder builder = buildQuery(callback, properties, values, filterProvider
				.getFilterBuilders(NamedQueries.Filters.IS_NOT_DELETED, NamedQueries.Filters.IS_NOT_REVISION));
		String query = builder.toString();
		assertTrue(query.contains("<http://address#value2>"));
		assertTrue(query.contains("emf:revisionType emf:revision"));
		assertTrue(query.contains("emf:isDeleted"));
		assertTrue(query.contains("emf:value1"));
	}

	private static StringBuilder buildQuery(SelectInstanceByCustomIdQueryCallback callback,
			Map<String, Object> properties, List<Serializable> values, List<Function<String, String>> filters) {
		StringBuilder builder = new StringBuilder();
		builder.append(callback.getStart(filters, Collections.emptyList()));

		boolean isFirst = true;
		Iterator<Serializable> iterator = values.iterator();
		while (iterator.hasNext()) {
			Serializable uri = iterator.next();
			if (isFirst) {
				isFirst = false;
			} else {
				builder.append(" UNION ");
			}
			builder.append(callback.singleValue(uri, properties, filters));
		}

		builder.append(callback.getEnd());
		return builder;
	}

	@Test
	public void queryBuilding_withLiterals() throws Exception {
		SelectInstanceByCustomIdQueryCallback callback = new SelectInstanceByCustomIdQueryCallback();
		Map<String, Object> properties = new HashMap<>();
		properties.put(Params.PROPERTY_ID, "emf:customId");
		List<Serializable> values = Arrays.asList("value1", 2232);

		StringBuilder builder = buildQuery(callback, properties, values, Collections.emptyList());
		String query = builder.toString();
		assertTrue(query.contains("\"value1\""));
		assertTrue(query.contains("\"2232\""));
	}
}
