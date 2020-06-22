package com.sirma.itt.seip.instance.save.expression.evaluation;

/**
 * Data object that holds an information for a expression function. This object is used to store the relevant data for
 * an emf expression. The relevant data we need to store is the name/label of the expression, specified in the name
 * attribute and the very expression given as value.
 * <pre>
 *     {@code
 *     <control-param id="function" name="labelId">${seq({+eaiSequence})}</control-param>
 *     }
 * </pre>
 * <br />
 * See {@link FieldsExpressionsEvaluationStep} for more information.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 10/07/2017
 */
public class ExpressionFunction {

	private String name;
	private String expression;

	/**
	 * Constructs a new expression function object.
	 *
	 * @param name
	 * 		here is stored a label for the function. Can be id of a label which is resolved in the UI or can be just
	 * 		some text.
	 * @param expression
	 * 		this should be emf expression that should be evaluated by the class
	 * 		{@link com.sirma.itt.seip.expressions.ExpressionsManager}
	 */
	ExpressionFunction(String name, String expression) {
		this.name = name;
		this.expression = expression;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}
}
