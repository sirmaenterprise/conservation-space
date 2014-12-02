package com.sirma.cmf.web.form.control;

import java.util.Comparator;

import com.sirma.itt.emf.definition.model.ControlParam;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Comparator for control parameters used in form builder. Used while sorting controls.
 * 
 * @author svelikov
 */
public class CMFControlParameterComparator implements Comparator<ControlParam> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compare(ControlParam o1, ControlParam o2) {
		if ((o1 == null) || (o2 == null)) {
			return EqualsHelper.nullCompare(o1, o2);
		}
		String param1 = o1.getName();
		String param2 = o2.getName();
		return param1.compareTo(param2);
	}

}
