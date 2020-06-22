package com.sirma.itt.seip.instance.save.expression.evaluation;

/**
 * Event fired when emf expression failed to get evaluated. This happens when an instance field has set expression
 * template in the definition. During the save process those expression are evaluated and if for some reason (definition
 * mistake) the expression evaluation fails then we throw an exception to roll back the save process, as we don't want
 * to store invalid data.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 12/07/2017
 */
public class FieldExpressionEvaluationException extends RuntimeException {

	/**
	 * The label Id that will be sent to UI where to be displayed to the end-user.
	 */
	private final String labelId;

	/**
	 * Instantiates a new Business Process Action runtime exception.
	 *
	 * @param message
	 * 		the exception message
	 * @param labelId
	 * 		the label id which will be send to UI
	 */
	public FieldExpressionEvaluationException(String message, String labelId) {
		super(message);
		this.labelId = labelId;
	}
	
	/**
	 * Instantiates a new Business Process Action runtime exception.
	 *
	 * @param message
	 * 		the exception message
	 * @param labelId
	 * 		the label id which will be send to UI
	 * @param cause
	 * 		the cause
	 */
	public FieldExpressionEvaluationException(String message, String labelId, Throwable cause) {
		super(message, cause);
		this.labelId = labelId;
	}

	/**
	 * Gets the action exception label id.
	 *
	 * @return the pre-defined label id for this exception.
	 */
	public String getLabelId() {
		return labelId;
	}
}
