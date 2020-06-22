package com.sirma.itt.seip.tasks.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.sirma.itt.seip.Copyable;
import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Wrapper object for event trigger properties
 *
 * @author BBonev
 */
@Embeddable
@PersistenceUnitBinding({PersistenceUnits.CORE, PersistenceUnits.PRIMARY})
public class EventTriggerEntity implements Serializable, Copyable<EventTriggerEntity> {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -6408359131313038209L;

	/**
	 * The event class id for event triggers.
	 */
	@Column(name = "eventClassId")
	private Integer eventClassId;

	/**
	 * The target semantic class id for event triggers.
	 */
	@Column(name = "targetClass", length = 150)
	private String targetSemanticClass;

	/**
	 * The user event user operation.
	 */
	@Column(name = "user_operation", length = 150)
	private String userOperation;

	/**
	 * The target id for event triggers.
	 */
	@Column(name = "targetId", length = 100)
	private String targetId;

	/**
	 * The event serverOperation.
	 */
	@Column(name = "operation", length = 150)
	private String serverOperation;

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
	 * 		the eventClass to set
	 */
	public void setEventClassId(Integer eventClass) {
		eventClassId = eventClass;
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
	 * 		the targetId to set
	 */
	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	/**
	 * Getter method for serverOperation.
	 *
	 * @return the serverOperation
	 */
	public String getServerOperation() {
		return serverOperation;
	}

	/**
	 * Setter method for server serverOperation.
	 *
	 * @param serverOperation
	 * 		the server serverOperation to set
	 */
	public void setServerOperation(String serverOperation) {
		this.serverOperation = serverOperation;
	}

	/**
	 * Get method for target class name.
	 *
	 * @return target class name
	 */
	public String getTargetSemanticClass() {
		return targetSemanticClass;
	}

	/**
	 * Set method for target class name.
	 *
	 * @param targetSemanticClass
	 * 		the target class name to set
	 */
	public void setSemanticTargetClass(String targetSemanticClass) {
		this.targetSemanticClass = targetSemanticClass;
	}

	/**
	 * Get method for user serverOperation.
	 *
	 * @return user serverOperation
	 */
	public String getUserOperation() {
		return userOperation;
	}

	/**
	 * Set method for user serverOperation.
	 *
	 * @param userOperation
	 * 		the user serverOperation to set
	 */
	public void setUserOperation(String userOperation) {
		this.userOperation = userOperation;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + (eventClassId == null ? 0 : eventClassId.hashCode());
		result = PRIME * result + (serverOperation == null ? 0 : serverOperation.hashCode());
		result = PRIME * result + (userOperation == null ? 0 : userOperation.hashCode());
		result = PRIME * result + (targetSemanticClass == null ? 0 : targetSemanticClass.hashCode());
		result = PRIME * result + (targetId == null ? 0 : targetId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof EventTriggerEntity)) {
			return false;
		}
		EventTriggerEntity other = (EventTriggerEntity) obj;
		if (!(EqualsHelper.nullSafeEquals(eventClassId, other.eventClassId)
				&& EqualsHelper.nullSafeEquals(serverOperation, other.serverOperation)
				&& EqualsHelper.nullSafeEquals(targetSemanticClass, other.targetSemanticClass)
				&& EqualsHelper.nullSafeEquals(userOperation, other.userOperation))) {
			return false;
		}
		return EqualsHelper.nullSafeEquals(targetId, other.targetId);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EventTriggerEntity [eventClassId=");
		builder.append(eventClassId);
		builder.append(", targetSemanticClass=");
		builder.append(targetSemanticClass);
		builder.append(", targetId=");
		builder.append(targetId);
		builder.append(", serverOperation=");
		builder.append(serverOperation);
		builder.append(", userOperation=");
		builder.append(userOperation);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public EventTriggerEntity createCopy() {
		EventTriggerEntity copy = new EventTriggerEntity();
		copy.eventClassId = eventClassId;
		copy.serverOperation = serverOperation;
		copy.targetSemanticClass = targetSemanticClass;
		copy.userOperation = userOperation;
		copy.targetId = targetId;
		return copy;
	}

}
