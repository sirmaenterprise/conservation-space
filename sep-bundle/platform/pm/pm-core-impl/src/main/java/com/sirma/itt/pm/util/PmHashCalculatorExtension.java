package com.sirma.itt.pm.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sirma.itt.emf.hash.HashCalculator;
import com.sirma.itt.emf.hash.HashCalculatorExtension;
import com.sirma.itt.emf.hash.HashHelper;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.pm.domain.definitions.ProjectDefinition;
import com.sirma.itt.pm.domain.definitions.impl.ProjectDefinitionImpl;

/**
 * The Class PmHashCalculatorExtension.
 *
 * @author BBonev
 */
@Extension(target = HashCalculatorExtension.TARGET_NAME, order = 30)
public class PmHashCalculatorExtension implements HashCalculatorExtension {

	/** The Constant prime. */
	private static final int PRIME = HashHelper.PRIME;

	/** The Constant SUPPORTED_OBJECTS. */
	private static final List<Class<?>> SUPPORTED_OBJECTS = new ArrayList<Class<?>>(
			Arrays.asList(ProjectDefinitionImpl.class));

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
	public Integer computeHash(HashCalculator calculator, Object o) {
		if (o instanceof ProjectDefinition) {
			return computeHash((ProjectDefinition) o, calculator);
		}
		return null;
	}

	/**
	 * Compute hash.
	 *
	 * @param definition
	 *            the definition
	 * @param calculator
	 *            the calculator
	 * @return the int
	 */
	public static int computeHash(ProjectDefinition definition, HashCalculator calculator) {
		int result = 1;

		result = HashHelper.computeHash(result, definition.getIdentifier(), "Identifier");
		result = (PRIME * result) + (definition.isAbstract() ? 1231 : 1237);
		result = HashHelper.computeHash(result, definition.getDmsId(), "DmsId");
		result = HashHelper.computeHash(result, definition.getParentDefinitionId(),
				"ParentDefinitionId");
		result = HashHelper.computeHash(result, definition.getExpression(), "Expression");

		result = HashHelper.computeHash(result, definition.getFields(), calculator, "Fields");
		result = HashHelper.computeHash(result, definition.getRegions(), calculator, "Regions");
		result = HashHelper.computeHash(result, definition.getTransitions(), calculator, "Transitions");
		result = HashHelper.computeHash(result, definition.getStateTransitions(), calculator, "StateTransitions");
		result = HashHelper.computeHash(result, definition.getAllowedChildren(), calculator, "getAllowedChildren");

		return result;
	}

}
