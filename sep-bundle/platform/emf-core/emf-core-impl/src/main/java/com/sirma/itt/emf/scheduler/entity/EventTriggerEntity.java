package com.sirma.itt.emf.scheduler.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Wrapper object for event trigger properties
 * 
 * @author BBonev
 */
@Embeddable
public class EventTriggerEntity implements Serializable, Cloneable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -6408359131313038209L;

	/** The event class id for event triggers. */
	@Column(name = "eventClassId")
	private Integer eventClassId;

	/** The target class id for event triggers. */
	@Column(name = "targetClassId")
	private Integer targetClassId;

	/** The target id for event triggers. */
	@Column(name = "targetId", length = 100)
	private String targetId;

	/** The operation. */
	@Column(name = "operation", length = 150)
	private String operation;

	/**
	 * Getter method for eventClass.
	 * 
	 * @return the eventClass
	 */
	public Integer getEventClassId() {
		return eventClassId;
	}

	/**
	 * Setter method for eventClass.
	 * 
	 * @param eventClass
	 *            the eventClass to set
	 */
	public void setEventClassId(Integer eventClass) {
		this.eventClassId = eventClass;
	}

	/**
	 * Getter method for targetClass.
	 * 
	 * @return the targetClass
	 */
	public Integer getTargetClassId() {
		return targetClassId;
	}

	/**
	 * Setter method for targetClass.
	 * 
	 * @param targetClass
	 *            the targetClass to set
	 */
	public void setTargetClassId(Integer targetClass) {
		this.targetClassId = targetClass;
	}

	/**
	 * Getter method for targetId.
	 * 
	 * @return the targetId
	 */
	public String getTargetId() {
		return targetId;
	}

	/**
	 * Setter method for targetId.
	 * 
	 * @param targetId
	 *            the targetId to set
	 */
	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((eventClassId == null) ? 0 : eventClassId.hashCode());
		result = (prime * result) + ((operation == null) ? 0 : operation.hashCode());
		result = (prime * result) + ((targetClassId == null) ? 0 : targetClassId.hashCode());
		result = (prime * result) + ((targetId == null) ? 0 : targetId.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		EventTriggerEntity other = (EventTriggerEntity) obj;
		if (eventClassId == null) {
			if (other.eventClassId != null) {
				return false;
			}
		} else if (!eventClassId.equals(other.eventClassId)) {
			return false;
		}
		if (operation == null) {
			if (other.operation != null) {
				return false;
			}
		} else if (!operation.equals(other.operation)) {
			return false;
		}
		if (targetClassId == null) {
			if (other.targetClassId != null) {
				return false;
			}
		} else if (!targetClassId.equals(other.targetClassId)) {
			return false;
		}
		if (targetId == null) {
			if (other.targetId != null) {
				return false;
			}
		} else if (!targetId.equals(other.targetId)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EventTriggerEntity [eventClassId=");
		builder.append(eventClassId);
		builder.append(", targetClassId=");
		builder.append(targetClassId);
		builder.append(", targetId=");
		builder.append(targetId);
		builder.append(", operation=");
		builder.append(operation);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Getter method for operation.
	 *
	 * @return the operation
	 */
	public String getOperation() {
		return operation;
	}

	/**
	 * Setter method for operation.
	 *
	 * @param operation the operation to set
	 */
	public void setOperation(String operation) {
		this.operation = operation;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EventTriggerEntity clone() {
		EventTriggerEntity copy = new EventTriggerEntity();
		copy.eventClassId = eventClassId;
		copy.operation = operation;
		copy.targetClassId = targetClassId;
		copy.targetId = targetId;
		return copy;
	}

}
