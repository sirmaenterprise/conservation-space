package com.sirmaenterprise.sep.jms.exception;

/**
 * Exception thrown when a defined JMS destination is not found
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 16/05/2017
 */
public class DestinationNotFoundException extends JmsRuntimeException {
	private final String destinationJndi;

	/**
	 * Instantiate new instance using the given destination JNDI
	 *
	 * @param destinationJndi the required destination JNDI
	 */
	public DestinationNotFoundException(String destinationJndi) {
		super("Cannot find the JMS destination: " + destinationJndi);
		this.destinationJndi = destinationJndi;
	}

	public String getDestinationJndi() {
		return destinationJndi;
	}
}
