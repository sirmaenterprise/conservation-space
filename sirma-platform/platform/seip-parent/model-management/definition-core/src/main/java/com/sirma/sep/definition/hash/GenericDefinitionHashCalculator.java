package com.sirma.sep.definition.hash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sirma.itt.seip.definition.model.GenericDefinitionImpl;
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
@SuppressWarnings("rawtypes")
@Extension(target = HashCalculatorExtension.TARGET_NAME, priority = 20)
public class GenericDefinitionHashCalculator implements HashCalculatorExtension {

	private static final String IS_ABSTRACT = "/isAbstract";
	private static final String PARENT_DEFINITION_ID = "/ParentDefinitionId";
	private static final String REFERENCE_ID = "/ReferenceId";
	private static final String DMS_ID = "/DmsId";
	private static final String EXPRESSION = "/Expression";

	private static final List<Class> SUPPORTED_OBJECTS = new ArrayList<>(Arrays.asList(GenericDefinitionImpl.class));

	@Override
	public List<Class> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
	}

	@Override
	public Integer computeHash(HashCalculator calculator, Object e) {
		if (e instanceof GenericDefinition) {
			return computeHashCode((GenericDefinition) e, calculator);
		}

		return null;
	}

	private static Integer computeHashCode(GenericDefinition definition, HashCalculator calculator) {
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

		return result;
	}
}
