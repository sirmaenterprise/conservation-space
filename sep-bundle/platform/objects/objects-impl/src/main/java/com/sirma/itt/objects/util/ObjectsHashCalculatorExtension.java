package com.sirma.itt.objects.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sirma.itt.emf.hash.HashCalculator;
import com.sirma.itt.emf.hash.HashCalculatorExtension;
import com.sirma.itt.emf.hash.HashHelper;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.objects.domain.definitions.ObjectDefinition;
import com.sirma.itt.objects.domain.definitions.impl.ObjectDefinitionImpl;

/**
 * Extension for Objects module definitions cash calculation
 *
 * @author BBonev
 */
@Extension(target = HashCalculatorExtension.TARGET_NAME, order = 40)
public class ObjectsHashCalculatorExtension implements HashCalculatorExtension {

	/** The Constant SUPPORTED_OBJECTS. */
	private static final List<Class<?>> SUPPORTED_OBJECTS = new ArrayList<Class<?>>(Arrays.asList(ObjectDefinitionImpl.class));

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
		if (o instanceof ObjectDefinition) {
			return computeHash((ObjectDefinition) o, calculator);
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
	public static int computeHash(ObjectDefinition definition, HashCalculator calculator) {
		int result = 1;

		result = HashHelper.computeHash(result, definition.getIdentifier(), "Identifier");
		result = HashHelper.computeHash(result, definition.isAbstract(), "isAbstract");
		result = HashHelper.computeHash(result, definition.getDmsId(), "DmsId");
		result = HashHelper.computeHash(result, definition.getParentDefinitionId(), "ParentDefinitionId");
		result = HashHelper.computeHash(result, definition.getExpression(), "Expression");

		result = HashHelper.computeHash(result, definition.getFields(), calculator, "Fields");
		result = HashHelper.computeHash(result, definition.getRegions(), calculator, "Regions");
		result = HashHelper.computeHash(result, definition.getTransitions(), calculator, "Transitions");
		result = HashHelper.computeHash(result, definition.getStateTransitions(), calculator, "StateTransitions");
		result = HashHelper.computeHash(result, definition.getAllowedChildren(), calculator, "AllowedChildren");

		return result;
	}

}
