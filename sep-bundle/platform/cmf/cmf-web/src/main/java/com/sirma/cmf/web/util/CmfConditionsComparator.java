package com.sirma.cmf.web.util;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sirma.itt.emf.definition.model.Condition;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Comparator that can sort {@link Condition} objects by priority.
 * 
 * @author Svilen Velikov
 */
public class CmfConditionsComparator implements
		Comparator<Map.Entry<Pair<String, Priority>, List<Condition>>> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compare(Entry<Pair<String, Priority>, List<Condition>> o1,
			Entry<Pair<String, Priority>, List<Condition>> o2) {
		Priority sortable1 = o1.getKey().getSecond();
		Priority sortable2 = o2.getKey().getSecond();

		if ((sortable1 == null) || (sortable2 == null)) {
			return EqualsHelper.nullCompare(sortable1, sortable2);
		}
		return sortable1.compareTo(sortable2);
	}
}
