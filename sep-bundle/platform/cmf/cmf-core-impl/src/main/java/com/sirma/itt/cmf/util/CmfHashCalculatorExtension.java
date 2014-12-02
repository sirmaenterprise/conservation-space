package com.sirma.itt.cmf.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.DocumentDefinitionTemplate;
import com.sirma.itt.cmf.beans.definitions.SectionDefinition;
import com.sirma.itt.cmf.beans.definitions.TaskDefinition;
import com.sirma.itt.cmf.beans.definitions.TaskDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.TaskDefinitionTemplate;
import com.sirma.itt.cmf.beans.definitions.WorkflowDefinition;
import com.sirma.itt.cmf.beans.definitions.impl.CaseDefinitionImpl;
import com.sirma.itt.cmf.beans.definitions.impl.DocumentDefinitionImpl;
import com.sirma.itt.cmf.beans.definitions.impl.DocumentDefinitionRefImpl;
import com.sirma.itt.cmf.beans.definitions.impl.GenericDefinitionImpl;
import com.sirma.itt.cmf.beans.definitions.impl.SectionDefinitionImpl;
import com.sirma.itt.cmf.beans.definitions.impl.TaskDefinitionImpl;
import com.sirma.itt.cmf.beans.definitions.impl.TaskDefinitionRefImpl;
import com.sirma.itt.cmf.beans.definitions.impl.TaskDefinitionTemplateImpl;
import com.sirma.itt.cmf.beans.definitions.impl.WorkflowDefinitionImpl;
import com.sirma.itt.emf.definition.model.GenericDefinition;
import com.sirma.itt.emf.hash.HashCalculator;
import com.sirma.itt.emf.hash.HashCalculatorExtension;
import com.sirma.itt.emf.hash.HashHelper;
import com.sirma.itt.emf.plugin.Extension;

/**
 * Extension point that adds CMF classes that need specific hash computation to the main calculator
 * implementation.
 *
 * @author BBonev
 */
@Extension(target = HashCalculatorExtension.TARGET_NAME, priority = 20)
public class CmfHashCalculatorExtension implements HashCalculatorExtension {

	private static final String PURPOSE = "Purpose";
	private static final String IS_ABSTRACT = "isAbstract";
	private static final String PARENT_DEFINITION_ID = "ParentDefinitionId";
	private static final String PARENT_TASK_ID = "ParentTaskId";
	private static final String TASK_DEFINITION_ID = "TaskDefinitionId";
	private static final String ALLOWED_CHILDREN = "AllowedChildren";
	private static final String STATE_TRANSITIONS = "StateTransitions";
	private static final String TRANSITIONS = "Transitions";
	private static final String REGIONS = "Regions";
	private static final String FIELDS = "Fields";
	private static final String REFERENCE_ID = "ReferenceId";
	private static final String DMS_ID = "DmsId";
	private static final String DMS_TYPE = "DmsType";
	private static final String EXPRESSION = "Expression";

	/** The Constant SUPPORTED_OBJECTS. */
	private static final List<Class<?>> SUPPORTED_OBJECTS = new ArrayList<Class<?>>(
			Arrays.asList(CaseDefinitionImpl.class, WorkflowDefinitionImpl.class,
					DocumentDefinitionImpl.class, DocumentDefinitionRefImpl.class,
					SectionDefinitionImpl.class, TaskDefinitionTemplateImpl.class,
					TaskDefinitionRefImpl.class, TaskDefinitionImpl.class, GenericDefinitionImpl.class));

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Class<?>> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer computeHash(HashCalculator calculator, Object e) {
		if (e instanceof CaseDefinition) {
			return computeHashCode((CaseDefinition) e, calculator);
		} else if (e instanceof WorkflowDefinition) {
			return computeHashCode((WorkflowDefinition) e, calculator);
		} else if (e instanceof DocumentDefinitionTemplate) {
			return computeHashCode((DocumentDefinitionTemplate) e, calculator);
		} else if (e instanceof TaskDefinitionTemplate) {
			return computeHashCode((TaskDefinitionTemplate) e, calculator);
		} else if (e instanceof SectionDefinition) {
			return computeHashCode((SectionDefinition) e, calculator);
		} else if (e instanceof TaskDefinition) {
			return computeHashCode((TaskDefinition) e, calculator);
		} else if (e instanceof TaskDefinitionRef) {
			return computeHashCode((TaskDefinitionRef) e, calculator);
		} else if (e instanceof DocumentDefinitionRef) {
			return computeHashCode((DocumentDefinitionRef) e, calculator);
		} else  if (e instanceof GenericDefinition) {
			return computeHashCode((GenericDefinition) e, calculator);
		}
		return null;
	}

	/**
	 * Compute hash code.
	 *
	 * @param definition
	 *            the definition
	 * @param calculator
	 *            the calculator
	 * @return the integer
	 */
	protected Integer computeHashCode(GenericDefinition definition, HashCalculator calculator) {
		int result = 1;

		result = HashHelper.computeHash(result, definition.getIdentifier(), "GenericId");
		result = HashHelper.computeHash(result, definition.getType(), "Type");
		result = HashHelper.computeHash(result, definition.getReferenceId(), REFERENCE_ID);
		result = HashHelper.computeHash(result, definition.isAbstract(), IS_ABSTRACT);
		result = HashHelper.computeHash(result, definition.getDmsId(), DMS_ID);
		result = HashHelper.computeHash(result, definition.getParentDefinitionId(),
				PARENT_DEFINITION_ID);
		result = HashHelper.computeHash(result, definition.getExpression(), EXPRESSION);

		result = HashHelper.computeHash(result, definition.getFields(), calculator, FIELDS);
		result = HashHelper.computeHash(result, definition.getRegions(), calculator, REGIONS);
		result = HashHelper.computeHash(result, definition.getTransitions(), calculator, TRANSITIONS);
		result = HashHelper.computeHash(result, definition.getStateTransitions(), calculator, STATE_TRANSITIONS);
		result = HashHelper.computeHash(result, definition.getAllowedChildren(), calculator, ALLOWED_CHILDREN);
		result = HashHelper.computeHash(result, definition.getSubDefinitions(), calculator, "SubDefinitions");

		return result;
	}

	/**
	 * Compute hash code.
	 *
	 * @param definition
	 *            the definition
	 * @param calculator
	 *            the calculator
	 * @return the integer
	 */
	protected Integer computeHashCode(TaskDefinition definition, HashCalculator calculator) {
		int result = 1;

		result = HashHelper.computeHash(result, definition.getIdentifier(), TASK_DEFINITION_ID);
		result = HashHelper.computeHash(result, definition.getParentTaskId(), PARENT_TASK_ID);
		result = HashHelper.computeHash(result, definition.getExpression(), EXPRESSION);
		result = HashHelper.computeHash(result, definition.getDmsType(), DMS_TYPE);
		result = HashHelper.computeHash(result, definition.getDmsId(), DMS_ID);
		result = HashHelper.computeHash(result, definition.getReferenceId(), REFERENCE_ID);
		result = HashHelper.computeHash(result, definition.isAbstract(), IS_ABSTRACT);

		result = HashHelper.computeHash(result, definition.getFields(), calculator, FIELDS);
		result = HashHelper.computeHash(result, definition.getRegions(), calculator, REGIONS);
		result = HashHelper.computeHash(result, definition.getTransitions(), calculator, TRANSITIONS);
		result = HashHelper.computeHash(result, definition.getStateTransitions(), calculator, STATE_TRANSITIONS);
		result = HashHelper.computeHash(result, definition.getAllowedChildren(), calculator, ALLOWED_CHILDREN);

		return result;
	}

	/**
	 * Compute hash code.
	 *
	 * @param definition
	 *            the definition
	 * @param calculator
	 *            the calculator
	 * @return the int
	 */
	protected int computeHashCode(TaskDefinitionTemplate definition,
			HashCalculator calculator) {
		int result = 1;

		result = HashHelper.computeHash(result, definition.getIdentifier(), TASK_DEFINITION_ID);
		result = HashHelper.computeHash(result, definition.getParentTaskId(), PARENT_TASK_ID);
		result = HashHelper.computeHash(result, definition.getExpression(), EXPRESSION);

		result = HashHelper.computeHash(result, definition.getFields(), calculator, FIELDS);
		result = HashHelper.computeHash(result, definition.getRegions(), calculator, REGIONS);
		result = HashHelper.computeHash(result, definition.getTransitions(), calculator, TRANSITIONS);
		result = HashHelper.computeHash(result, definition.getStateTransitions(), calculator, STATE_TRANSITIONS);
		result = HashHelper.computeHash(result, definition.getAllowedChildren(), calculator, ALLOWED_CHILDREN);

		return result;
	}

	/**
	 * Compute hash code.
	 *
	 * @param definition
	 *            the definition
	 * @param calculator
	 *            the calculator
	 * @return the int
	 */
	protected int computeHashCode(DocumentDefinitionTemplate definition,
			HashCalculator calculator) {
		int result = 1;

		result = HashHelper.computeHash(result, definition.getIdentifier(), "DocumentId");
		result = HashHelper.computeHash(result, definition.getParent(), "Parent");
		result = HashHelper.computeHash(result, definition.getExpression(), EXPRESSION);

		result = HashHelper.computeHash(result, definition.getFields(), calculator, FIELDS);
		result = HashHelper.computeHash(result, definition.getRegions(), calculator, REGIONS);
		result = HashHelper.computeHash(result, definition.getTransitions(), calculator, TRANSITIONS);
		result = HashHelper.computeHash(result, definition.getStateTransitions(), calculator, STATE_TRANSITIONS);
		result = HashHelper.computeHash(result, definition.getAllowedChildren(), calculator, ALLOWED_CHILDREN);

		return result;
	}

	/**
	 * Compute hash code.
	 *
	 * @param definition
	 *            the definition
	 * @param calculator
	 *            the calculator
	 * @return the int
	 */
	protected int computeHashCode(WorkflowDefinition definition, HashCalculator calculator) {
		int result = 1;

		result = HashHelper.computeHash(result, definition.getIdentifier(), "WorkflowDefinitionId");
		result = HashHelper.computeHash(result, definition.isAbstract(), IS_ABSTRACT);
		result = HashHelper.computeHash(result, definition.getDmsId(), DMS_ID);
		result = HashHelper.computeHash(result, definition.getParentDefinitionId(),
				PARENT_DEFINITION_ID);

		result = HashHelper.computeHash(result, definition.getTasks(), calculator, "Tasks");
		result = HashHelper.computeHash(result, definition.getFields(), calculator, FIELDS);
		result = HashHelper.computeHash(result, definition.getRegions(), calculator, REGIONS);
		result = HashHelper.computeHash(result, definition.getTransitions(), calculator, TRANSITIONS);
		result = HashHelper.computeHash(result, definition.getStateTransitions(), calculator, STATE_TRANSITIONS);
		result = HashHelper.computeHash(result, definition.getAllowedChildren(), calculator, ALLOWED_CHILDREN);

		return result;
	}

	/**
	 * Compute hash code.
	 *
	 * @param definition
	 *            the definition
	 * @param calculator
	 *            the calculator
	 * @return the int
	 */
	protected int computeHashCode(TaskDefinitionRef definition, HashCalculator calculator) {
		int result = 1;
		result = HashHelper.computeHash(result, definition.getIdentifier(), TASK_DEFINITION_ID);
		result = HashHelper.computeHash(result, definition.getPurpose(), PURPOSE);
		result = HashHelper.computeHash(result, definition.getReferenceTaskId(), "ReferenceTaskId");
		result = HashHelper.computeHash(result, definition.getExpression(), EXPRESSION);

		result = HashHelper.computeHash(result, definition.getFields(), calculator, FIELDS);
		result = HashHelper.computeHash(result, definition.getRegions(), calculator, REGIONS);
		result = HashHelper.computeHash(result, definition.getTransitions(), calculator, TRANSITIONS);
		result = HashHelper.computeHash(result, definition.getStateTransitions(), calculator, STATE_TRANSITIONS);
		result = HashHelper.computeHash(result, definition.getAllowedChildren(), calculator, ALLOWED_CHILDREN);

		return result;
	}

	/**
	 * Compute hash code for the given case definition. Method does not take into account the
	 * properties that are time or DB dependent but only once that come from the XML definition
	 *
	 * @param definition
	 *            the definition
	 * @param calculator
	 *            the calculator
	 * @return the int
	 */
	protected int computeHashCode(CaseDefinition definition, HashCalculator calculator) {
		int result = 1;

		result = HashHelper.computeHash(result, definition.getIdentifier(), "CaseId");
		result = HashHelper.computeHash(result, definition.isAbstract(), IS_ABSTRACT);
		result = HashHelper.computeHash(result, definition.getDmsId(), DMS_ID);
		result = HashHelper.computeHash(result, definition.getParentDefinitionId(),
				PARENT_DEFINITION_ID);
		result = HashHelper.computeHash(result, definition.getExpression(), EXPRESSION);

		result = HashHelper.computeHash(result, definition.getSectionDefinitions(), calculator, "SectionDefinitions");
		result = HashHelper.computeHash(result, definition.getFields(), calculator, FIELDS);
		result = HashHelper.computeHash(result, definition.getRegions(), calculator, REGIONS);
		result = HashHelper.computeHash(result, definition.getTransitions(), calculator, TRANSITIONS);
		result = HashHelper.computeHash(result, definition.getStateTransitions(), calculator, STATE_TRANSITIONS);
		result = HashHelper.computeHash(result, definition.getAllowedChildren(), calculator, ALLOWED_CHILDREN);

		return result;
	}

	/**
	 * Compute hash code for {@link SectionDefinition}.
	 *
	 * @param definition
	 *            the definition
	 * @param calculator
	 *            the calculator
	 * @return the int
	 */
	protected int computeHashCode(SectionDefinition definition, HashCalculator calculator) {
		int result = 1;
		result = HashHelper.computeHash(result, definition.getIdentifier(), "SectionId");
		result = HashHelper.computeHash(result, definition.getPurpose(), PURPOSE);
		result = HashHelper.computeHash(result, definition.getReferenceId(), REFERENCE_ID);

		result = HashHelper.computeHash(result, definition.getDocumentDefinitions(), calculator, "DocumentDefinitions");
		result = HashHelper.computeHash(result, definition.getFields(), calculator, FIELDS);
		result = HashHelper.computeHash(result, definition.getRegions(), calculator, REGIONS);
		result = HashHelper.computeHash(result, definition.getTransitions(), calculator, TRANSITIONS);
		result = HashHelper.computeHash(result, definition.getStateTransitions(), calculator, STATE_TRANSITIONS);
		result = HashHelper.computeHash(result, definition.getAllowedChildren(), calculator, ALLOWED_CHILDREN);

		return result;
	}

	/**
	 * Compute hash code for {@link DocumentDefinitionRef}.
	 *
	 * @param definition
	 *            the definition
	 * @param calculator
	 *            the calculator
	 * @return the int+
	 */
	protected int computeHashCode(DocumentDefinitionRef definition, HashCalculator calculator) {
		int result = 1;
		result = HashHelper.computeHash(result, definition.getReferenceId(), REFERENCE_ID);
		result = HashHelper.computeHash(result, definition.getIdentifier(), "DocumentDefinitionId");
		result = HashHelper.computeHash(result, definition.getMaxInstances(), "MaxInstances");
		result = HashHelper.computeHash(result, definition.getMandatory(), "Mandatory");
		result = HashHelper.computeHash(result, definition.getStructured(), "Structured");
		result = HashHelper.computeHash(result, definition.getPurpose(), PURPOSE);
		result = HashHelper.computeHash(result, definition.getExpression(), EXPRESSION);

		result = HashHelper.computeHash(result, definition.getFields(), calculator, FIELDS);
		result = HashHelper.computeHash(result, definition.getRegions(), calculator, REGIONS);
		result = HashHelper.computeHash(result, definition.getTransitions(), calculator, TRANSITIONS);
		result = HashHelper.computeHash(result, definition.getStateTransitions(), calculator, STATE_TRANSITIONS);
		result = HashHelper.computeHash(result, definition.getAllowedChildren(), calculator, ALLOWED_CHILDREN);

		return result;
	}

}
