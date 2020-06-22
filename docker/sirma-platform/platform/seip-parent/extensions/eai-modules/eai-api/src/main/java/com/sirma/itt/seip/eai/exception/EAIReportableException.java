package com.sirma.itt.seip.eai.exception;

/**
 * An exception that indicates an error during communication with external system - protocol, format, etc. Wraps the
 * original cause.<br>
 * {@link EAIReportableException} should be reported to the external system
 * 
 * @author bbanchev
 */
public class EAIReportableException extends EAIException {

	/** serialVersionUID. */
	private static final long serialVersionUID = -8215254379633498706L;

	private final String origin;

	/**
	 * Instantiates a new detailed {@link EAIReportableException}.
	 *
	 * @param message
	 *            the details of exception
	 * @param cause
	 *            the cause of exception
	 */
	public EAIReportableException(String message, Throwable cause) {
		this(message, cause, null);
	}

	/**
	 * Instantiates a new {@link EAIReportableException}.
	 *
	 * @param message
	 *            the details of exception
	 */
	public EAIReportableException(String message) {
		this(message, null, null);
	}

	/**
	 * Instantiates a new detailed {@link EAIReportableException}.
	 *
	 * @param message
	 *            the details of exception
	 * @param cause
	 *            the cause of exception
	 * @param origin
	 *            the origin of reportable exception
	 */
	public EAIReportableException(String message, Throwable cause, String origin) {
		super(message, cause);
		this.origin = origin;
	}

	/**
	 * Instantiates a new {@link EAIReportableException}.
	 *
	 * @param message
	 *            the details of exception
	 * @param origin
	 *            the origin of reportable exception
	 */
	public EAIReportableException(String message, String origin) {
		this(message, null, origin);
	}

	/**
	 * Gets the origing of reportable exception - might be null
	 * 
	 * @return the origin
	 */
	public String getOrigin() {
		return origin;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getName());
		if (getMessage() != null) {
			builder.append(getMessage());
		}
		builder.append("[");
		builder.append(origin);
		builder.append("]");
		return builder.toString();
	}

}