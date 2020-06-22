package com.sirma.itt.seip.domain.instance;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Comparator for property model. The sorting is done by comparing one specific field.
 * <p>
 * TODO: implement it to allow different sorting for different properties.
 *
 * @author BBonev
 */
public class PropertyModelComparator implements Comparator<PropertyModel> {

	/** The sort property. */
	private final String sortProperty;
	private String[] otherProps;
	private boolean ascending;

	/**
	 * Instantiates a new property model comparator.
	 *
	 * @param ascending
	 *            the ascending order
	 * @param sortProperty
	 *            the sort property
	 * @param otherProps
	 *            the other sort properties
	 */
	public PropertyModelComparator(boolean ascending, String sortProperty, String... otherProps) {
		this.ascending = ascending;
		this.sortProperty = sortProperty;
		this.otherProps = otherProps;
		if (this.sortProperty == null) {
			throw new IllegalArgumentException("Cannot compare null keys");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compare(PropertyModel model1, PropertyModel model2) {
		int compare = EqualsHelper.nullCompare(model1, model2);
		if (compare != 2) {
			return ascending ? compare : -compare;
		}
		Map<String, Serializable> props1 = model1.getProperties();
		Map<String, Serializable> props2 = model2.getProperties();
		compare = EqualsHelper.nullCompare(props1, props2);
		if (compare != 2) {
			return ascending ? compare : -compare;
		}
		compare = compareProps(props1, props2, sortProperty);
		if (compare == 0) {
			if (otherProps == null || otherProps.length == 0) {
				return ascending ? compare : -compare;
			}
			for (int i = 0; i < otherProps.length; i++) {
				String key = otherProps[i];
				compare = compareProps(props1, props2, key);
				if (compare != 0) {
					break;
				}
			}
		}
		return ascending ? compare : -compare;
	}

	/**
	 * Compare single prop from the given maps.
	 *
	 * @param props1
	 *            the props1
	 * @param props2
	 *            the props2
	 * @param key
	 *            the key to check
	 * @return the int
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static int compareProps(Map<String, Serializable> props1, Map<String, Serializable> props2, String key) {
		Serializable value1 = props1.get(key);
		Serializable value2 = props2.get(key);
		int compare = EqualsHelper.nullCompare(value1, value2);
		if (compare != 2) {
			return compare;
		}
		if (value1 instanceof Comparable && value2 instanceof Comparable) {
			return ((Comparable) value1).compareTo(value2);
		}
		throw new EmfRuntimeException(
				"Cannot compare values of different types " + value1.getClass() + " and " + value2.getClass());
	}

}
