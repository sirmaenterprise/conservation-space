package org.camunda.bpm.engine.impl.persistence.entity;

import org.camunda.bpm.engine.impl.core.variable.scope.VariableCollectionProvider;

import javax.enterprise.inject.Vetoed;
import java.util.Collection;

/**
 * Proxy class of {@link ExecutionEntity} used in evaluation of bpm condition expressions on the fly,
 * without the need to update variables constantly.
 *
 * @author hlungov
 */
@Vetoed
public class ExecutionEntityProxy extends ExecutionEntity {

	/**
	 * Constructor.
	 */
	public ExecutionEntityProxy() {
		variableStore.setVariablesProvider(VariableCollectionProvider.<VariableInstanceEntity>emptyVariables());
	}

	/**
	 * Direct add variable to @{link VariableStore}, so this will not trigger camunda engine update.
	 * @param variables collection of {@link VariableInstanceEntity}
	 */
	public void addVariables(Collection<VariableInstanceEntity> variables) {
		variables.forEach(this::addVariable);
	}

	private void addVariable(VariableInstanceEntity variableInstanceEntity) {
		if (variableStore.containsKey(variableInstanceEntity.getName())) {
			variableStore.removeVariable(variableInstanceEntity.getName());
		}
		variableStore.addVariable(variableInstanceEntity);
	}
}
