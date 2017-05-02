package com.sirma.itt.cmf.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sirma.itt.cmf.beans.definitions.impl.GenericDefinitionImpl;
import com.sirma.itt.seip.definition.util.PathHelper;
import com.sirma.itt.seip.definition.util.hash.HashCalculator;
import com.sirma.itt.seip.definition.util.hash.HashCalculatorExtension;
import com.sirma.itt.seip.definition.util.hash.HashHelper;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Extension point that adds CMF classes that need specific hash computation to the main calculator implementation.
 *
 * @author BBonev
 */
@Extension(target = HashCalculatorExtension.TARGET_NAME, priority = 20)
public class CmfHashCalculatorExtension implements HashCalculatorExtension {

	private static final String PURPOSE = "/Purpose";
	private static final String IS_ABSTRACT = "/isAbstract";
	private static final String PARENT_DEFINITION_ID = "/ParentDefinitionId";
	private static final String PARENT_TASK_ID = "/ParentTaskId";
	private static final String TASK_DEFINITION_ID = "/TaskDefinitionId";
	private static final String REFERENCE_ID = "/ReferenceId";
	private static final String DMS_ID = "/DmsId";
	private static final String DMS_TYPE = "/DmsType";
	private static final String EXPRESSION = "/Expression";

	/** The Constant SUPPORTED_OBJECTS. */
	private static final List<Class> SUPPORTED_OBJECTS = new ArrayList<>(Arrays.asList(GenericDefinitionImpl.class));

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Class> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer computeHash(HashCalculator calculator, Object e) {
		if (e instanceof GenericDefinition) {
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

		String path = PathHelper.getPath(definition);
		result = HashHelper.computeHash(result, definition.getIdentifier(), path + "/GenericId");
		result = HashHelper.computeHash(result, definition.getType(), path + "/Type");
		result = HashHelper.computeHash(result, definition.getReferenceId(), path + REFERENCE_ID);
		result = HashHelper.computeHash(result, definition.isAbstract(), path + IS_ABSTRACT);
		result = HashHelper.computeHash(result, definition.getDmsId(), path + DMS_ID);
		result = HashHelper.computeHash(result, definition.getParentDefinitionId(), path + PARENT_DEFINITION_ID);
		result = HashHelper.computeHash(result, definition.getExpression(), path + EXPRESSION);

		result = HashHelper.computeGenericDefinitionInterfaces(result, definition, calculator);
		result = HashHelper.computeHash(result, definition.getSubDefinitions(), calculator, path + "/SubDefinitions");

		return result;
	}
}
