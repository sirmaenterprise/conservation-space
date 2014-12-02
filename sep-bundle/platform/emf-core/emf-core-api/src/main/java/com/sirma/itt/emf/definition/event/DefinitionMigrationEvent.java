package com.sirma.itt.emf.definition.event;

import java.util.Collections;
import java.util.List;

import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.event.AbstractContextEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired to notify about definition migration of instances. If known the event will provide
 * information about which definitions for a particular definition type will be updated.
 * 
 * @author BBonev
 */
@Documentation("Event fired to notify about definition migration of instances. If known the event will provide information about which definitions for a particular definition type will be updated.")
public class DefinitionMigrationEvent extends AbstractContextEvent {

	/** The target. */
	private final Class<?> target;

	/** The definition ids. */
	private final List<Pair<String, Long>> definitionIds;

	/**
	 * Instantiates a new definition migration event.
	 * 
	 * @param target
	 *            the target
	 */
	public DefinitionMigrationEvent(Class<?> target) {
		this(target, Collections.<Pair<String, Long>> emptyList());
	}

	/**
	 * Instantiates a new definition migration event.
	 * 
	 * @param target
	 *            the target
	 * @param definitionIds
	 *            the definition ids
	 */
	public DefinitionMigrationEvent(Class<?> target, List<Pair<String, Long>> definitionIds) {
		this.target = target;
		this.definitionIds = definitionIds;
	}

	/**
	 * Gets the target.
	 * 
	 * @return the target
	 */
	public Class<?> getTarget() {
		return target;
	}

	/**
	 * Gets the definition ids.
	 * 
	 * @return the definition ids
	 */
	public List<Pair<String, Long>> getDefinitionIds() {
		return definitionIds;
	}

}
