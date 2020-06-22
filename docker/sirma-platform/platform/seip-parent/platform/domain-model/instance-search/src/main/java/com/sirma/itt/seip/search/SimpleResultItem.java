package com.sirma.itt.seip.search;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Simple implementation of {@link com.sirma.itt.seip.search.ResultItem}.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 21/07/2017
 */
// the class does not need to override the equals method as the super class equals tests through the interface and
// this class is covered
public class SimpleResultItem extends AbstractResultItem { // NOSONAR

	private final Map<String, Serializable> values;

	/**
	 * Instantiate new instance with the given values mapping
	 *
	 * @param values the values to add
	 */
	public SimpleResultItem(Map<String, Serializable> values) {
		this.values = values;
	}

	/**
	 * Create new empty instance
	 *
	 * @return the created instance
	 */
	public static SimpleResultItem create() {
		return new SimpleResultItem(new HashMap<>());
	}

	/**
	 * Add value to the result item that will be returned by the get methods
	 *
	 * @param key the key to use for value identification
	 * @param value the value to add
	 * @return the same instance to allow method chaining
	 */
	public SimpleResultItem add(String key, Serializable value) {
		values.put(key, value);
		return this;
	}

	@Override
	public Iterator<ResultValue> iterator() {
		return values.entrySet()
				.stream()
				.filter(e -> e.getValue() != null)
				.map(e -> ResultValue.create(e.getKey(), e.getValue()))
				.iterator();
	}

	@Override
	public Set<String> getValueNames() {
		return values.keySet();
	}

	@Override
	public boolean hasValue(String name) {
		return values.containsKey(name);
	}

	@Override
	public ResultValue getValue(String name) {
		Serializable value = values.get(name);
		if (value == null) {
			return null;
		}
		return ResultValue.create(name, value);
	}

	@Override
	public Serializable getResultValue(String name) {
		return values.get(name);
	}

	@Override
	public int size() {
		return (int) values.values()
				.stream()
				.filter(Objects::nonNull)
				.count();
	}
}
