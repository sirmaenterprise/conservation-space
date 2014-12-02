package com.sirma.itt.objects.web.caseinstance.dashboard;

import java.util.Comparator;
import java.util.Date;

import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.objects.domain.model.ObjectInstance;

/**
 * This class represent custom object comparator. Will compare instances of type
 * {@link ObjectInstance} based on last modification.
 * 
 * @author cdimitrov
 */
public class ObjectComparator implements Comparator<ObjectInstance> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compare(ObjectInstance firstObject, ObjectInstance secondObject) {
		Date firstObjectModifyOn = (Date) firstObject.getProperties().get(
				DefaultProperties.MODIFIED_ON);

		Date secondObjectModifyOn = (Date) secondObject.getProperties().get(
				DefaultProperties.MODIFIED_ON);

		return firstObjectModifyOn.compareTo(secondObjectModifyOn);
	}
}
