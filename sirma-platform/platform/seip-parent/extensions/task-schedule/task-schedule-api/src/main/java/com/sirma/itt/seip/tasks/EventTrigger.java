package com.sirma.itt.seip.tasks;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.seip.domain.event.OperationEvent;
import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.exception.EmfConfigurationException;
import com.sirma.itt.seip.serialization.kryo.KryoHelper;
import com.sirma.itt.seip.util.EqualsHelper;

import java.io.Serializable;

/**
 * Default implementation for {@link EventTrigger}.
 *
 * @author BBonev
 */
public class EventTrigger implements Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 8598529905848232201L;

	/**
	 * The event class.
	 */
	private Class<? extends EmfEvent> eventClass;

	/**
	 * The event class register id.
	 */
	@Tag(1)
	protected Integer eventClassRegisterId;
	/**
	 * The target instance class.
	 */
	@Tag(2)
	protected String targetSemanticInstanceClass;
	/**
	 * The target instance identifier.
	 */
	@Tag(3)
	protected Serializable targetInstanceIdentifier;
	/**
	 * The custom server operation. identified by {@link com.sirma.itt.seip.domain.event.OperationEvent}
	 */
	@Tag(4)
	protected String serverOperation;

	/**
	 * The custom user operation. identified by {@link com.sirma.itt.seip.domain.event.OperationEvent}
	 */
	@Tag(5)
	protected String userOperation;

	/**
	 * Instantiates a new default scheduler event trigger.
	 */
	public EventTrigger() {
		// nothing to do here
	}

	/**
	 * Instantiates a new default scheduler event trigger.
	 *
	 * @param eventClass
	 * 		the event class
	 * @param targetSemanticInstanceClass
	 * 		the target instance class
	 * @param targetInstanceIdentifier
	 * 		the target instance identifier
	 * @param serverOperation
	 * 		the server operation
	 * @param userOperation
	 * 		the user operation
	 */
	public EventTrigger(Class<? extends EmfEvent> eventClass, String targetSemanticInstanceClass,
			Serializable targetInstanceIdentifier, String serverOperation, String userOperation) {
		setEventClass(eventClass);
		setSemanticTargetInstanceClass(targetSemanticInstanceClass);
		this.targetInstanceIdentifier = targetInstanceIdentifier;
		this.serverOperation = serverOperation;
		this.userOperation = userOperation;
	}

	/**
	 * Get method for Event class.
	 *
	 * @param <E>
	 * 		type of event
	 * @return Event class
	 */
	@SuppressWarnings("unchecked")
	public <E extends EmfEvent> Class<E> getEventClass() {
		if (eventClass == null && eventClassRegisterId != null) {
			eventClass = (Class<? extends EmfEvent>) KryoHelper.getStaticInstance()
					.getRegisteredClass(eventClassRegisterId);
		}
		return (Class<E>) eventClass;
	}

	/**
	 * Get method for target instance semantic class.
	 *
	 * @return target instance class
	 */
	@SuppressWarnings("unchecked")
	public String getTargetSemanticInstanceClass() {
		return targetSemanticInstanceClass;
	}

	/**
	 * Get method for target instance identifier.
	 *
	 * @return target instance identifier
	 */
	public Serializable getTargetInstanceIdentifier() {
		return targetInstanceIdentifier;
	}

	/**
	 * Setter method for eventClass.
	 *
	 * @param eventClass
	 * 		the eventClass to set
	 */
	public void setEventClass(Class<? extends EmfEvent> eventClass) {
		if (eventClass != null) {
			eventClassRegisterId = KryoHelper.getStaticInstance().getClassRegistration(eventClass);
			if (eventClassRegisterId == null) {
				throw new EmfConfigurationException(
						"The event class " + eventClass + " is not registred for Kryo serialization");
			}
			if (!OperationEvent.class.isAssignableFrom(eventClass)) {
				throw new EmfConfigurationException(
						"The event class " + eventClass + " is not  " + OperationEvent.class.getSimpleName()
								+ " event!");
			}
		}
		this.eventClass = eventClass;
	}

	/**
	 * Setter method for target instance semantic class.
	 *
	 * @param targetInstanceClass
	 * 		the targetSemanticInstanceClass to set
	 */
	public void setSemanticTargetInstanceClass(String targetInstanceClass) {
		this.targetSemanticInstanceClass = targetInstanceClass;
	}

	/**
	 * Setter method for targetInstanceIdentifier.
	 *
	 * @param targetInstanceIdentifier
	 * 		the targetInstanceIdentifier to set
	 */
	public void setTargetInstanceIdentifier(Serializable targetInstanceIdentifier) {
		this.targetInstanceIdentifier = targetInstanceIdentifier;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (eventClassRegisterId == null ? 0 : eventClassRegisterId.hashCode());
		result = prime * result + (targetSemanticInstanceClass == null ? 0 : targetSemanticInstanceClass.hashCode());
		result = prime * result + (targetInstanceIdentifier == null ? 0 : targetInstanceIdentifier.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof EventTrigger)) {
			return false;
		}
		EventTrigger other = (EventTrigger) obj;
		return EqualsHelper.nullSafeEquals(eventClassRegisterId, other.getEventClassRegisterId())
				&& EqualsHelper.nullSafeEquals(targetSemanticInstanceClass, other.getTargetSemanticInstanceClass())
				&& EqualsHelper.nullSafeEquals(targetInstanceIdentifier, other.getTargetInstanceIdentifier());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EventTrigger [eventClass=");
		builder.append(eventClass);
		builder.append(", targetSemanticInstanceClass=");
		builder.append(targetSemanticInstanceClass);
		builder.append(", targetInstanceIdentifier=");
		builder.append(targetInstanceIdentifier);
		builder.append(", serverOperation=");
		builder.append(serverOperation);
		builder.append(", userOperation=");
		builder.append(userOperation);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Getter method for eventClassRegisterId.
	 *
	 * @return the eventClassRegisterId
	 */
	protected Integer getEventClassRegisterId() {
		return eventClassRegisterId;
	}

	/**
	 * Getter method for operation.
	 *
	 * @return the operation
	 */
	public String getServerOperation() {
		return serverOperation;
	}

	/**
	 * Setter method for operation.
	 *
	 * @param serverOperation
	 * 		the server operation to set
	 */
	public void setServerOperation(String serverOperation) {
		this.serverOperation = serverOperation;
	}

	/**
	 * Getter method for user operation.
	 *
	 * @return the user operation
	 */
	public String getUserOperation() {
		return userOperation;
	}

	/**
	 * Setter method for user operation.
	 *
	 * @param userOperation
	 * 		the user operation to set
	 */
	public void setUserOperation(String userOperation) {
		this.userOperation = userOperation;
	}

}
