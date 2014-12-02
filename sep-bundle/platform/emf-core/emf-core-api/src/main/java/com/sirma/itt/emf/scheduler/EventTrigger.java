package com.sirma.itt.emf.scheduler;

import java.io.Serializable;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.event.OperationEvent;
import com.sirma.itt.emf.exceptions.EmfConfigurationException;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.serialization.kryo.KryoSerializationEngine;

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

	/** The event class. */
	private Class<? extends EmfEvent> eventClass;

	/** The target instance class. */
	private Class<?> targetInstanceClass;

	/** The event class register id. */
	@Tag(1)
	protected Integer eventClassRegisterId;
	/** The target instance class register id. */
	@Tag(2)
	protected Integer targetInstanceClassRegisterId;
	/** The target instance identifier. */
	@Tag(3)
	protected Serializable targetInstanceIdentifier;
	/**
	 * The custom user/system operation. identified by
	 * {@link com.sirma.itt.emf.event.OperationEvent}
	 */
	@Tag(4)
	protected String operation;

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
	 *            the event class
	 * @param targetInstanceClass
	 *            the target instance class
	 * @param targetInstanceIdentifier
	 *            the target instance identifier
	 * @param operation
	 *            the operation
	 */
	public EventTrigger(Class<? extends EmfEvent> eventClass,
			Class<? extends Instance> targetInstanceClass, Serializable targetInstanceIdentifier,
			String operation) {
		setEventClass(eventClass);
		setTargetInstanceClass(targetInstanceClass);
		this.targetInstanceIdentifier = targetInstanceIdentifier;
		this.operation = operation;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public <E extends EmfEvent> Class<E> getEventClass() {
		if ((eventClass == null) && (eventClassRegisterId != null)) {
			eventClass = (Class<? extends EmfEvent>) KryoSerializationEngine
					.getRegisteredClass(eventClassRegisterId);
		}
		return (Class<E>) eventClass;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public <I extends Instance> Class<I> getTargetInstanceClass() {
		if ((targetInstanceClass == null) && (targetInstanceClassRegisterId != null)) {
			targetInstanceClass = KryoSerializationEngine
					.getRegisteredClass(targetInstanceClassRegisterId);
		}
		return (Class<I>) targetInstanceClass;
	}

	/**
	 * {@inheritDoc}
	 */
	public Serializable getTargetInstanceIdentifier() {
		return targetInstanceIdentifier;
	}

	/**
	 * Setter method for eventClass.
	 *
	 * @param eventClass
	 *            the eventClass to set
	 */
	public void setEventClass(Class<? extends EmfEvent> eventClass) {
		if (eventClass != null) {
			eventClassRegisterId = KryoSerializationEngine.getClassRegistration(eventClass);
			if ((eventClassRegisterId == null)
					|| !OperationEvent.class.isAssignableFrom(eventClass)) {
				throw new EmfConfigurationException("The event class " + eventClass
						+ " is not registred for Kryo serialization!");
			}
		}
		this.eventClass = eventClass;
	}

	/**
	 * Setter method for targetInstanceClass.
	 *
	 * @param targetInstanceClass
	 *            the targetInstanceClass to set
	 */
	public void setTargetInstanceClass(Class<?> targetInstanceClass) {
		if (targetInstanceClass != null) {
			targetInstanceClassRegisterId = KryoSerializationEngine
					.getClassRegistration(targetInstanceClass);
			if (targetInstanceClassRegisterId == null) {
				throw new EmfConfigurationException("The target instance class "
						+ targetInstanceClass + " is not registred for Kryo serialization!");
			}
		}
		this.targetInstanceClass = targetInstanceClass;
	}

	/**
	 * Setter method for targetInstanceIdentifier.
	 *
	 * @param targetInstanceIdentifier
	 *            the targetInstanceIdentifier to set
	 */
	public void setTargetInstanceIdentifier(Serializable targetInstanceIdentifier) {
		this.targetInstanceIdentifier = targetInstanceIdentifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result)
				+ ((eventClassRegisterId == null) ? 0 : eventClassRegisterId.hashCode());
		result = (prime * result)
				+ ((targetInstanceClassRegisterId == null) ? 0 : targetInstanceClassRegisterId
						.hashCode());
		result = (prime * result)
				+ ((targetInstanceIdentifier == null) ? 0 : targetInstanceIdentifier.hashCode());
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
		EventTrigger other = (EventTrigger) obj;
		if (eventClassRegisterId == null) {
			if (other.eventClassRegisterId != null) {
				return false;
			}
		} else if (!eventClassRegisterId.equals(other.eventClassRegisterId)) {
			return false;
		}
		if (targetInstanceClassRegisterId == null) {
			if (other.targetInstanceClassRegisterId != null) {
				return false;
			}
		} else if (!targetInstanceClassRegisterId.equals(other.targetInstanceClassRegisterId)) {
			return false;
		}
		if (targetInstanceIdentifier == null) {
			if (other.targetInstanceIdentifier != null) {
				return false;
			}
		} else if (!targetInstanceIdentifier.equals(other.targetInstanceIdentifier)) {
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
		builder.append("EventTrigger [eventClass=");
		builder.append(eventClass);
		builder.append(", targetInstanceClass=");
		builder.append(targetInstanceClass);
		builder.append(", targetInstanceIdentifier=");
		builder.append(targetInstanceIdentifier);
		builder.append(", operation=");
		builder.append(operation);
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
	 * Getter method for targetInstanceClassRegisterId.
	 * 
	 * @return the targetInstanceClassRegisterId
	 */
	protected Integer getTargetInstanceClassRegisterId() {
		return targetInstanceClassRegisterId;
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

}
