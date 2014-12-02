package com.sirma.cmf.web.form.builder;

import java.text.MessageFormat;

import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputLabel;
import javax.faces.context.FacesContext;

import com.sirma.itt.emf.domain.Pair;

/**
 * AbstractFormBuilder provides base functionality and template methods for the form builder.
 * 
 * @author svelikov
 */
public abstract class AbstractFormBuilder {

	/**
	 * Updates the created label component if necessary.
	 * 
	 * @param label
	 *            The label component.
	 */
	public abstract void updateLabel(HtmlOutputLabel label);

	/**
	 * Updates the created component.
	 * 
	 * @param uiComponent
	 *            the ui component {@link UIComponent}.
	 */
	public abstract void updateField(UIComponent uiComponent);

	/**
	 * Build and add content after the field.
	 * 
	 * @param wrapper
	 *            The field wrapper.
	 */
	public abstract void addAfterFieldContent(UIComponent wrapper);

	/**
	 * Updates the created wrapper.
	 * 
	 * @param wrapper
	 *            the wrapper {@link UIComponent}.
	 */
	public abstract void updateWrapper(UIComponent wrapper);

	/**
	 * Instantiates an instance of required type.
	 * 
	 * @return {@link UIComponent}.
	 */
	public abstract UIComponent getComponentInstance();

	/**
	 * Set a field validator.
	 * 
	 * @param component
	 *            The component.
	 * @param validatorData
	 *            the validator data contains the type to be used as regex pattern and message if
	 *            validation fails.
	 */
	public abstract void setFieldValidator(UIComponent component, Pair<String, String> validatorData);

	/**
	 * Getter for the return type of value expression for value attribute.
	 * 
	 * @return The type
	 */
	public abstract Class<?> getValueResultType();

	/**
	 * Assembles an EL expression as string.
	 * 
	 * @param instanceName
	 *            The object instance name.
	 * @param propertyName
	 *            The instance's property name.
	 * @return EL expression as string.
	 */
	protected String getValueExpressionString(String instanceName, String propertyName) {
		return "#{" + instanceName + ".properties['" + propertyName + "']}";
	}

	/**
	 * Assembles an EL expression as string.
	 * 
	 * @param instanceName
	 *            The object instance name.
	 * @param propertyName
	 *            The instance's property name.
	 * @param clNumber
	 *            Codelist number.
	 * @return EL expression as string.
	 */
	protected String getValueExpressionStringForCodelist(String instanceName, String propertyName,
			int clNumber) {
		// TODO: try to use LabelBuilder.getCodelistDisplayValue in order to have consistent value
		String expression = MessageFormat.format(
				"#'{'cls.getDescription({0},{1}.properties[''{2}''])'}'", clNumber, instanceName,
				propertyName);
		return expression;
	}

	/**
	 * Gets the value expression for user.
	 * 
	 * @param fieldname
	 *            the fieldname
	 * @return the value expression for user
	 */
	protected String getValueExpressionForUser(String fieldname) {
		return MessageFormat.format(
				"#'{'labelBuilder.getUserDisplayName(documentContext,''{0}'')'}'", fieldname);
	}

	/**
	 * Creates a value expression.
	 * 
	 * @param stringValueExpression
	 *            The value expression string.
	 * @param valueType
	 *            The expected return type from evaluation of the value expression.
	 * @return created ValueExpression.
	 */
	protected ValueExpression createValueExpression(String stringValueExpression, Class<?> valueType) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		return facesContext
				.getApplication()
				.getExpressionFactory()
				.createValueExpression(facesContext.getELContext(), stringValueExpression,
						valueType);
	}

	/**
	 * Creates the method expression.
	 * 
	 * @param stringExpression
	 *            the string expression
	 * @return the method expression
	 */
	protected MethodExpression createMethodExpression(String stringExpression) {
		return _createMethodExpression(stringExpression, new Class<?>[] {});
	}

	/**
	 * Creates the method expression.
	 * 
	 * @param stringExpression
	 *            the string expression
	 * @param argumentsType
	 *            the arguments type
	 * @return the method expression
	 */
	protected MethodExpression createMethodExpression(String stringExpression,
			Class<?>[] argumentsType) {
		return _createMethodExpression(stringExpression, argumentsType);
	}

	/**
	 * _create method expression.
	 * 
	 * @param stringExpression
	 *            the string expression
	 * @param argumentsType
	 *            the arguments type
	 * @return the method expression
	 */
	private MethodExpression _createMethodExpression(String stringExpression,
			Class<?>[] argumentsType) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		return facesContext
				.getApplication()
				.getExpressionFactory()
				.createMethodExpression(facesContext.getELContext(), stringExpression,
						String.class, argumentsType);
	}

}
