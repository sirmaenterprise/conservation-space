package com.sirma.itt.seip.instance.save.expression.evaluation;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceSaveStep;
import com.sirma.itt.seip.plugin.Extension;

/**
 * This save step handles instance fields which have expressions in them. Some such expressions can be evaluated only on
 * instance save. Here is an example for such definition
 *
 * <pre>
 * {@code
 * 	<field name="generatedField" type="an..1024" label="generatedField" previewEmpty="true" mandatory="true" displayType
="editable" uri="emf:generatedField">
 * 		<control id="default_value_pattern">
 * 			<control-param id="template" name=
"template">Unique number: ${seq({+eaiSequence})} with title: ${get([title])}</control-param>
 * 			<control-param id="function" name="{get([title])}">${get([title])}</control-param>
 * 			<control-param id="function" name="${seq({+eaiSequence})}">${seq({+eaiSequence})}</control-param>
 * 		</control>
 * 	</field>
 * }
 * </pre>
 *
 * <br />
 * In this example when saved the expression template has to get the next value from the described sequence and set it
 * as part of the exception. The class handles all such expressions.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 10/07/2017
 */
@Extension(target = InstanceSaveStep.NAME, enabled = true, order = 40)
public class FieldsExpressionsEvaluationStep implements InstanceSaveStep {
	/**
	 * This is the type of the <control-param /> definition tag used to set expressions. See example in the class
	 * javadoc.
	 */
	private static final String DEFAULT_VALUE_PATTERN = "default_value_pattern";
	/**
	 * This is the id of the <control-param /> definition tag used to store data about the functional expressions. See
	 * the example above.
	 */
	private static final String FUNCTION_ID = "function";
	private static final String FAILED_EXPRESSION_EVALUATION_ERROR_LABEL = "expression.evaluation.failure";

	@Inject
	private DefinitionService definitionService;
	@Inject
	private ExpressionsManager expressionManager;

	@Override
	public void beforeSave(InstanceSaveContext saveContext) {
		Instance instance = saveContext.getInstance();

		DefinitionModel instanceDefinition = definitionService.getInstanceDefinition(instance);
		if (instanceDefinition == null) {
			throw new FieldExpressionEvaluationException(
					"Could not extract the definition in order to evaluate the expression template",
					FAILED_EXPRESSION_EVALUATION_ERROR_LABEL);
		}

		instanceDefinition.fieldsStream().filter(field -> field.getControlDefinition() != null)
				.forEach(propertyDefinition -> process(instance, propertyDefinition));
	}

	private void process(Instance instance, PropertyDefinition propertyDefinition) {
		ControlDefinition controlDefinition = propertyDefinition.getControlDefinition();

		String fieldValue = instance.getString(propertyDefinition.getName());
		List<ExpressionFunction> expressionFunctions = collectFunctionBindings(controlDefinition);
		if (expressionFunctions.isEmpty()) {
			return;
		}
		for (ExpressionFunction function : expressionFunctions) {
			if (fieldValue == null || !fieldValue.contains(function.getExpression())) {
				continue;
			}
			// Evaluate the expression.
			String evaluationResult = evaluateExpression(instance, propertyDefinition, function);
			// The second part of the check seems a bit odd, perhaps we should return null the
			// ExpressionManager in every case it can't get executed.
			if (evaluationResult == null || evaluationResult.equals(function.getExpression())) {
				throw new FieldExpressionEvaluationException(
						"Could not evaluate the expression " + function.getExpression(),
						FAILED_EXPRESSION_EVALUATION_ERROR_LABEL);
			}
			// Escape the special symbols in the name, we need to replace exact strings.
			fieldValue = fieldValue.replace(function.getExpression(), evaluationResult);
		}

		instance.add(propertyDefinition.getName(), fieldValue);
	}

	private static List<ExpressionFunction> collectFunctionBindings(ControlDefinition controlDefinition) {
		return controlDefinition.getControlParams().stream()
				.filter(parameter -> FUNCTION_ID.equals(parameter.getIdentifier())
						&& DEFAULT_VALUE_PATTERN.equals(parameter.getType()))
				.map(parameter -> new ExpressionFunction(parameter.getName(), parameter.getValue()))
				.collect(Collectors.toList());
	}

	private String evaluateExpression(Instance instance, PropertyDefinition propertyDefinition,
			ExpressionFunction expression) {
		ExpressionContext context = expressionManager.createDefaultContext(instance, propertyDefinition,
				Collections.emptyMap());
		return expressionManager.evaluateRule(expression.getExpression(), String.class, context);
	}

	@Override
	public String getName() {
		return "evaluateExpressionStep";
	}
}
