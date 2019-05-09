package com.sirma.sep.model.management;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.sirma.itt.seip.Copyable;
import com.sirma.sep.model.management.definition.DefinitionModelAttributes;
import com.sirma.sep.model.management.meta.ModelMetaInfo;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Holds definition information about a {@link ModelAction} behavior.
 * <pre>
 *  There are two kind of action executions:
 *  1.  Action executions which hold configuration about creation of relations.
 *       1.1 Definition representation:
 *          Such action execution is described by control tag with attribute id which value is "configuration";
 *       2.1 Modeling representation:
 *          Such action execution will be a {@link ModelActionExecution} object with type {@link ModelActionExecution#CREATE_RELATION_MODELING}.
 *  2.  Action executions which holds javascript which have to be executed during action processing.
 *      2.1 Definition representation:
 *          Such action execution is described by control tag with attribute id which value is "SCRIPT";
 *      2.1 Modeling representation:
 *           Such action execution will be {@link ModelActionExecution} object with type {@link ModelActionExecution#EXECUTE_SCRIPT_MODELING}.
 * </pre>
 *
 * @author Boyan Tonchev.
 */
public class ModelActionExecution extends AbstractModelNode<ModelActionExecution, ModelAction>
		implements Copyable<ModelActionExecution> {

	static final String MODEL_TYPE = "actionExecution";

	/**
	 * Definition term for action execution with javascript.
	 */
	public static final String EXECUTE_SCRIPT_DEFINITION = "SCRIPT";

	/**
	 * Modeling term for action execution with javascript.
	 */
	public static final String EXECUTE_SCRIPT_MODELING = "executeScript";

	/**
	 * Definition term for action execution with configuration for create relations.
	 */
	public static final String CREATE_RELATION_DEFINITION = "configuration";

	/**
	 * Modeling term for action execution with configuration for create relations.
	 */
	public static final String CREATE_RELATION_MODELING = "createRelation";

	@Override
	public ModelActionExecution createCopy() {
		return copyNodeTo(new ModelActionExecution());
	}

	@Override
	protected String getLabelAttributeKey() {
		return DefinitionModelAttributes.LABEL;
	}

	@Override
	public int hashCode() {
		return super.hashCode() * 29;
	}

	@Override
	protected String getTypeName() {
		return MODEL_TYPE;
	}

	@Override
	protected Function<String, Optional<ModelActionExecution>> getRemoveFunction() {
		return getContext()::removeActionExecution;
	}

	@Override
	public boolean equals(Object o) {
		return super.equals(o);
	}

	@Override
	@JsonIgnore
	public Map<String, ModelMetaInfo> getAttributesMetaInfo() {
		return getModelsMetaInfo().getActionExecutionsMapping();
	}
}
