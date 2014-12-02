package com.sirma.itt.emf.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sirma.itt.emf.hash.HashCalculator;
import com.sirma.itt.emf.hash.HashCalculatorExtension;
import com.sirma.itt.emf.hash.HashHelper;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.resources.model.EmfResource;
import com.sirma.itt.emf.security.model.EmfGroup;
import com.sirma.itt.emf.security.model.EmfUser;

/**
 * Resources hash calculator.
 * 
 * @author BBonev
 */
@Extension(target = HashCalculatorExtension.TARGET_NAME, order = 11)
public class ResourceHashCalculatorExtension implements HashCalculatorExtension {

	/** The Constant SUPPORTED_OBJECTS. */
	private static final List<Class<?>> SUPPORTED_OBJECTS = new ArrayList<Class<?>>(Arrays.asList(
			EmfResource.class, EmfUser.class, EmfGroup.class));

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
	private Integer computeHashCode(EmfResource object) {
		int result = 1;

		result = HashHelper.computeHash(result, object.getIdentifier(), "Identifier");
		result = HashHelper.computeHash(result, object.getDisplayName(), "DisplayName");
		result = HashHelper.computeHash(result, object.getType(), "Type");
		result = HashHelper.computeHash(result, object.getProperties(), "Properties");
		return result;
	}

	/**
	 * Compute EmfGroup hash code.
	 * 
	 * @param object
	 *            the object
	 * @return the integer
	 */
	private Integer computeHashCode(EmfGroup object) {
		return computeHashCode((EmfResource) object);
	}

	/**
	 * Compute EmfUser hash code.
	 * 
	 * @param object
	 *            the object
	 * @return the integer
	 */
	private Integer computeHashCode(EmfUser object) {
		return computeHashCode((EmfResource) object);
	}

}
