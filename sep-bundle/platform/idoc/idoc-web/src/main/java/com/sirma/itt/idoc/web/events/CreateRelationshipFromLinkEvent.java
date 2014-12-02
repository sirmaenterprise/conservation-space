package com.sirma.itt.idoc.web.events;


import java.util.Set;

import com.sirma.itt.emf.instance.model.InstanceReference;

/**
 * Event payload object.
 */
public class CreateRelationshipFromLinkEvent {

	/** The instance id. */
	private final String instanceId;
	/** The instance type. */
	private final String instanceType;
	/** The from. */
	private final InstanceReference from;
	/** The new instance ids. */
	private final Set<String> newLinkedInstances;

	/**
	 * Constructor.
	 *
	 * @param instanceId
	 *            Instance id.
	 * @param instanceType
	 *            Instance type. .
	 * @param from
	 *            Instance from which to create the relationship.
	 * @param newInstanceIds
	 *            the new instance ids
	 */
	public CreateRelationshipFromLinkEvent(String instanceId, String instanceType,
			InstanceReference from, Set<String> newInstanceIds) {
		this.instanceId = instanceId;
		this.instanceType = instanceType;
		this.from = from;
		this.newLinkedInstances = newInstanceIds;
	}

	/**
	 * Gets the instance id.
	 *
	 * @return the instance id
	 */
	public String getInstanceId() {
		return instanceId;
	}

	/**
	 * Gets the instance type.
	 *
	 * @return the instance type
	 */
	public String getInstanceType() {
		return instanceType;
	}

	/**
	 * Gets the from.
	 *
	 * @return the from
	 */
	public InstanceReference getFrom() {
		return from;
	}

	/**
	 * Getter method for newInstanceIds.
	 *
	 * @return the newInstanceIds
	 */
	public Set<String> getNewLinkedInstances() {
		return newLinkedInstances;
	}

}
