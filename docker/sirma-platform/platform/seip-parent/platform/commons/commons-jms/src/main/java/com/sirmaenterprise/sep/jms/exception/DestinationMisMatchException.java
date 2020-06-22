package com.sirmaenterprise.sep.jms.exception;

/**
 * Exception throw when the resolved destination is of different than the expected type. This is the case when a
 * listener is configured to listen on a Queue but the destination JNDI points to a Topic.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 16/05/2017
 */
public class DestinationMisMatchException extends JmsRuntimeException {
	private final String destinationJndi;
	private final Class<?> expectedType;
	private final Class<?> actualType;

	/**
	 * Instantiate new instance using the provided data.
	 *
	 * @param destinationJndi the destination JNDI name that was looked up
	 * @param expectedType the expected destination type
	 * @param actualType the actual destination type
	 */
	public DestinationMisMatchException(String destinationJndi, Class<?> expectedType, Class<?> actualType) {
		super("Expected " + expectedType + " but found " + actualType + " at " + destinationJndi);
		this.destinationJndi = destinationJndi;
		this.expectedType = expectedType;
		this.actualType = actualType;
	}

	public String getDestinationJndi() {
		return destinationJndi;
	}

	public Class<?> getExpectedType() {
		return expectedType;
	}

	public Class<?> getActualType() {
		return actualType;
	}
}
