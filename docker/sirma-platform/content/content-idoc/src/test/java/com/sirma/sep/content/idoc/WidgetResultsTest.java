package com.sirma.sep.content.idoc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * Test for {@link WidgetResults}.
 *
 * @author A. Kunchev
 */
@SuppressWarnings("static-method")
public class WidgetResultsTest {

	@Test
	public void areAny_withResults() {
		WidgetResults results = WidgetResults.fromConfiguration(new Object());
		assertTrue(results.areAny());
	}

	@Test
	public void areAny_withoutResults() {
		WidgetResults results = WidgetResults.fromConfiguration(null);
		assertFalse(results.areAny());
	}

	@Test
	public void areAny_widgetResultsEmptyObject() {
		assertFalse(WidgetResults.EMPTY.areAny());
	}

	@Test
	public void getResultsAsMap_withResults() {
		Map<String, Object> results = new HashMap<>(1);
		results.put("key", new Object());
		assertFalse(WidgetResults.fromConfiguration(results).getResultsAsMap().isEmpty());
	}

	@Test
	public void getResultsAsMap_withoutResults() {
		assertTrue(WidgetResults.fromConfiguration(null).getResultsAsMap().isEmpty());
	}

	@Test
	public void getResultsAsCollection_withResults() {
		assertFalse(WidgetResults
				.fromConfiguration(Collections.singleton(new Object()))
					.getResultsAsCollection()
					.isEmpty());
	}

	@Test
	public void getResultsAsCollection_withoutResults() {
		assertTrue(WidgetResults.fromConfiguration(null).getResultsAsCollection().isEmpty());
	}

	@Test
	public void isFoundBySearch_true() {
		WidgetResults results = WidgetResults.fromSearch(null);
		assertTrue(results.isFoundBySearch());
	}

	@Test
	public void isFoundBySearch_false() {
		WidgetResults results = WidgetResults.fromConfiguration(null);
		assertFalse(results.isFoundBySearch());
	}
}
