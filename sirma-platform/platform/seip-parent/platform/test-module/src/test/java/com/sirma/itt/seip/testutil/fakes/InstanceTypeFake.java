package com.sirma.itt.seip.testutil.fakes;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;

import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceType;

/**
 * Builder for {@link InstanceType} fake objects for test purposes
 *
 * @author BBonev
 */
public class InstanceTypeFake {

	/**
	 * Builds a instance type that has the given id and category
	 *
	 * @param classId
	 *            the class/instance type id
	 * @param category
	 *            the instance type category
	 * @return the instance type
	 */
	public static InstanceType build(String classId, String category) {
		ClassInstance classInstance = new ClassInstance();
		classInstance.setId(classId);
		classInstance.setCategory(category);
		return classInstance.type();
	}

	/**
	 * Builds an instance type for the given category without id
	 *
	 * @param category
	 *            the category
	 * @return the instance type
	 */
	public static InstanceType buildForCategory(String category) {
		return build(null, category);
	}

	/**
	 * Builds an instance type for the given class id without category
	 *
	 * @param classId
	 *            the class id
	 * @return the instance type
	 */
	public static InstanceType buildForClass(String classId) {
		return build(classId, null);
	}

	/**
	 * Sets an instance type to the given instance using the data for the instance type.
	 *
	 * @param instance
	 *            the instance
	 * @param classId
	 *            the class id
	 * @param category
	 *            the category
	 * @return the instance
	 */
	public static Instance setType(Instance instance, String classId, String category) {
		instance.addIfNotNull(SEMANTIC_TYPE, classId);
		instance.setType(build(classId, category));
		return instance;
	}

	/**
	 * Sets an instance type to the given instance using the class short name for the category. The class id will be
	 * null
	 *
	 * @param instance
	 *            the instance
	 * @return the instance
	 */
	public static Instance setType(Instance instance) {
		String type = instance.getClass().getSimpleName().toLowerCase();
		instance.addIfNotNull(SEMANTIC_TYPE, type);
		instance.setType(build(type, type));
		return instance;
	}
}
