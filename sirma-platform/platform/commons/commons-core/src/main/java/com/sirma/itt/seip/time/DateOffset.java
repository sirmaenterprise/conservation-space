package com.sirma.itt.seip.time;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

import com.sirma.itt.seip.collections.CollectionUtils;

/**
 * Holds offsets for unclear point in time &amp; space. Should use {@link DateOffsetKeys} for putting &amp; getting
 * values from this Map.
 *
 * @author Mihail Radkov
 */
public class DateOffset extends AbstractMap<String, Integer> {

	// Year + day + hour + minute + ms
	private static final Integer INITIAL_SIZE = 5;

	private final Map<String, Integer> offsets;

	/**
	 * Instantiates a new date offset.
	 */
	public DateOffset() {
		offsets = CollectionUtils.createHashMap(INITIAL_SIZE);
	}

	@Override
	public Integer put(String key, Integer value) {
		return offsets.put(key, value);
	}

	@Override
	public Set<java.util.Map.Entry<String, Integer>> entrySet() {
		return offsets.entrySet();
	}

}
