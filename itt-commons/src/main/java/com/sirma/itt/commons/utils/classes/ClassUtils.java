/**
 * Copyright (c) 2008 11.08.2008 , Sirma ITT.
/*

/**
 * 
 */
package com.sirma.itt.commons.utils.classes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Utilities for classes.
 * 
 * @author Hristo Iliev
 */
public final class ClassUtils {
	/**
	 * Hide the utility constructor.
	 */
	private ClassUtils() {
		// Hide the utility constructor
	}

	/**
	 * Retrieve all the subclasses for specified class. The classes are searched
	 * in specified package.
	 * 
	 * @param <T>
	 *            type of searched class
	 * @param class1
	 *            Class<T>, class object with searched file
	 * @param classList
	 *            list with classes to check if they are subclasses of the
	 *            specified type
	 * @return list with found subclasses
	 */
	public static <T> List<Class<?>> getAllKnownSubclasses(Class<T> class1,
			List<Class<?>> classList) {
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>(classList);
		for (Iterator<Class<?>> it = classes.iterator(); it.hasNext();) {
			Class<?> class2 = it.next();
			if (!class1.isAssignableFrom(class2)) {
				it.remove();
			} else if (class2.equals(class1)) {
				it.remove();
			}
		}
		return classes;
	}

	/**
	 * Instantiates a class by its name. The class should have a public
	 * non-argument constructor (e.g. the default constructor).
	 * 
	 * @param className
	 *            name of the class to instantiate.
	 * @return the constructed instance.
	 */
	public static <T> T instantiate(String className) {
		Class<T> clazz;
		try {
			clazz = (Class<T>) Class.forName(className);
			return clazz.newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

}
