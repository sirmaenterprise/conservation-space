package com.sirma.itt.seip.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sirma.itt.seip.definition.util.hash.HashCalculator;
import com.sirma.itt.seip.definition.util.hash.HashCalculatorExtension;
import com.sirma.itt.seip.definition.util.hash.HashHelper;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Resources hash calculator.
 *
 * @author BBonev
 */
@Extension(target = HashCalculatorExtension.TARGET_NAME, order = 11)
public class ResourceHashCalculatorExtension implements HashCalculatorExtension {

	/** The Constant SUPPORTED_OBJECTS. */
	private static final List<Class> SUPPORTED_OBJECTS = new ArrayList<>(
			Arrays.asList(EmfResource.class, EmfUser.class, EmfGroup.class));

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
	public Integer computeHash(HashCalculator calculator, Object object) {
		if (object instanceof EmfUser) {
			return computeHashCode((EmfUser) object);
		} else if (object instanceof EmfGroup) {
			return computeHashCode((EmfGroup) object);
		} else if (object instanceof EmfResource) {
			return computeHashCode((EmfResource) object);
		}
		return null;
	}

	/**
	 * Compute EmfResource hash code.
	 *
	 * @param object
	 *            the object
	 * @return the integer
	 */
	private static Integer computeHashCode(EmfResource object) {
		int result = 1;

		result = HashHelper.computeHash(result, object.getName(), "Name");
		result = HashHelper.computeHash(result, object.getDisplayName(), "DisplayName");
		result = HashHelper.computeHash(result, object.getType(), "Type");
		result = HashHelper.computeHash(result, object.getProperties(), "Properties");
		result = HashHelper.computeHash(result, object.isActive(), "isActive");
		return result;
	}

	/**
	 * Compute EmfGroup hash code.
	 *
	 * @param object
	 *            the object
	 * @return the integer
	 */
	private static Integer computeHashCode(EmfGroup object) {
		return computeHashCode((EmfResource) object);
	}

	/**
	 * Compute EmfUser hash code.
	 *
	 * @param object
	 *            the object
	 * @return the integer
	 */
	private static Integer computeHashCode(EmfUser object) {
		return computeHashCode((EmfResource) object);
	}

}
