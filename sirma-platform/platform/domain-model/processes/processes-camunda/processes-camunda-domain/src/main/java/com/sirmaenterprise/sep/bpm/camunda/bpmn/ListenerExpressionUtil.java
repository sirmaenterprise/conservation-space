package com.sirmaenterprise.sep.bpm.camunda.bpmn;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.BaseDelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.impl.el.Expression;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;

import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationRuntimeException;
import com.sirmaenterprise.sep.bpm.camunda.properties.BPMTaskProperties;

/**
 * Helper utility class holding common evaluators for injected expression in {@link JavaDelegate} or
 * {@link TaskListener}
 */
public class ListenerExpressionUtil {

	private ListenerExpressionUtil() {
		// utility class
	}

	/**
	 * Extract business id for 'source' expression. The expression may return the workflow instance id in most
	 * cases but in can return also the task instance id.
	 *
	 * @param context
	 *            the current data context
	 * @param source
	 *            the source expression
	 * @return the business id in SEIP or throws exception on inability to resolve it
	 */
	public static String extractBusinessIdBySourceExpression(VariableScope context, Expression source) {
		Object sourceValue = source.getValue(context);
		Objects.requireNonNull(sourceValue, "Source value cound not be resolved in context! Original value: " + source);

		String businessId;
		if (sourceValue instanceof BaseDelegateExecution) {
			businessId = ((BaseDelegateExecution) sourceValue).getBusinessKey();
		} else if (sourceValue instanceof ExecutionEntity) {
			businessId = ((ExecutionEntity) sourceValue).getBusinessKey();
		} else if (sourceValue instanceof TaskEntity) {
			businessId = ((TaskEntity) sourceValue).getVariables().get(BPMTaskProperties.TASK_ID).toString();
		} else {
			throw new CamundaIntegrationRuntimeException(
					"Could not detect the required value 'business key' for scope: " + context);
		}
		return businessId;
	}
}
