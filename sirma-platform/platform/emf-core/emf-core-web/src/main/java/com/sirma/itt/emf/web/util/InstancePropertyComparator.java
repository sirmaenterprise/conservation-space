package com.sirma.itt.emf.web.util;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Comparator for {@link Instance}s by one of their properties.
 *
 * @author yasko
 */
public class InstancePropertyComparator implements Comparator<Instance> {

	/**
	 * Comparator by instance title
	 */
	public static final InstancePropertyComparator BY_TITLE_COMPARATOR = new InstancePropertyComparator(
			DefaultProperties.TITLE);

	private String property;

	/**
	 * Constructor.
	 *
	 * @param property
	 *            Property to campare by.
	 */
	public InstancePropertyComparator(String property) {
		this.property = property;
	}

	@Override
	@SuppressWarnings("unchecked")
	public int compare(Instance o1, Instance o2) {
		if (o1 == o2) {
			return 0;
		}

		Serializable v1 = null;
		Serializable v2 = null;

		Map<String, Serializable> p1 = o1.getProperties();
		Map<String, Serializable> p2 = o2.getProperties();

		if (p1 == null || (v1 = p1.get(property)) == null) {
			v1 = "";
		}

		if (p2 == null || (v2 = p2.get(property)) == null) {
			v2 = "";
		}

		if (v1 instanceof Comparable && v2 instanceof Comparable) {
			return ((Comparable<Serializable>) v1).compareTo(v2);
		}
		return 0;
	}

}
