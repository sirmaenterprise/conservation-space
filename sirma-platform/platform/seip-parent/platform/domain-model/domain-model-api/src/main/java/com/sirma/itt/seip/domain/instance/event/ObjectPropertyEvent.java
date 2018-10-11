package com.sirma.itt.seip.domain.instance.event;

import java.io.Serializable;

import com.sirma.itt.seip.event.EmfEvent;

/**
 * Low level event that notifies for changes in instance object properties.
 *
 * @author BBonev
 */
public interface ObjectPropertyEvent extends EmfEvent {

	/**
	 * Gets the source instance id.
	 *
	 * @return the source id
	 */
	Serializable getSourceId();

	/**
	 * Gets the object property name
	 *
	 * @return the object property name
	 */
	String getObjectPropertyName();

	/**
	 * Gets the target instance id.
	 *
	 * @return the target id
	 */
	Serializable getTargetId();
}
