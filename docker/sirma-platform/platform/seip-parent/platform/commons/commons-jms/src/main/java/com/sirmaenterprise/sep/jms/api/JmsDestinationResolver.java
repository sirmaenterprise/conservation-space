package com.sirmaenterprise.sep.jms.api;

import java.io.Serializable;

import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.Topic;

import com.sirmaenterprise.sep.jms.exception.DestinationMisMatchException;

/**
 * Resolver that can be used to lookup JMS destinations.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 16/05/2017
 */
public interface JmsDestinationResolver extends Serializable {
	/**
	 * Resolve a JMS {@link Destination} instance by it's JNDI name. <br>If the destination could not be resolved or
	 * provisioned a {@link com.sirmaenterprise.sep.jms.exception.DestinationNotFoundException} should be thrown.
	 *
	 * @param jndi the JNDI to resolve
	 * @param <D> the expected destination type
	 * @return the resolved destination.
	 */
	<D extends Destination> D resolve(String jndi);

	/**
	 * Resolve a JMS {@link Queue} by it's JNDI name. <br>If the queue cannot be found then
	 * {@link com.sirmaenterprise.sep.jms.exception.DestinationNotFoundException} should be thrown. <br>If the resolved
	 * destination points to a {@link Destination} then {@link DestinationMisMatchException} should be thrown.
	 *
	 * @param jndi the JNDI to resolve
	 * @return the resolved Queue
	 */
	default Queue resolveQueue(String jndi) {
		Destination destination = resolve(jndi);
		if (!(destination instanceof Queue)) {
			throw new DestinationMisMatchException(jndi, Queue.class, destination.getClass());
		}
		return (Queue) destination;
	}

	/**
	 * Resolve a JMS {@link Topic} by it's JNDI name. <br>If the topic cannot be found then
	 * {@link com.sirmaenterprise.sep.jms.exception.DestinationNotFoundException} should be thrown. <br>If the resolved
	 * destination points to a {@link Queue} then {@link DestinationMisMatchException} should be thrown.
	 *
	 * @param jndi the JNDI to resolve
	 * @return the resolved Queue
	 */
	default Topic resolveTopic(String jndi) {
		Destination destination = resolve(jndi);
		if (!(destination instanceof Topic)) {
			throw new DestinationMisMatchException(jndi, Topic.class, destination.getClass());
		}
		return (Topic) destination;
	}
}
