package com.sirma.itt.seip.instance.dao;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link InstanceExistResult}.
 *
 * @author A. Kunchev
 */
public class InstanceExistResultTest {

	private InstanceExistResult<String> result;

	@Before
	public void setup() {
		Map<String, Boolean> map = new HashMap<>(2);
		map.put("existing-instance-id", Boolean.TRUE);
		map.put("not-existing-instance-id", Boolean.FALSE);
		result = new InstanceExistResult<>(Collections.unmodifiableMap(map));
	}

	@SuppressWarnings({ "static-method", "unused" })
	@Test(expected = NullPointerException.class)
	public void instanceExistResult_nullInputArgument() {
		new InstanceExistResult<String>(null);
	}

	@Test
	public void retrievingResults() {
		Map<String, Boolean> resultMap = result.getAll();
		assertEquals("There should be only 2 results in the map.", 2, resultMap.size());

		Collection<String> existing = result.get();
		assertEquals("The id of the existing instance should be only 1.", 1, existing.size());
		assertEquals("Should be id of existing.", "existing-instance-id", existing.iterator().next());

		Collection<String> notExisting = result.getNotExisting();
		assertEquals("The id of the not existing instance should be only 1.", 1, notExisting.size());
		assertEquals("Should be id of not existing.", "not-existing-instance-id", notExisting.iterator().next());
	}
}
