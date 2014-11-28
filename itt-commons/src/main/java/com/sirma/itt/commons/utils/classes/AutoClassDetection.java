/**
 * Copyright (c) 2008 15.12.2008 , Sirma ITT.
/*

/**
 * 
 */
package com.sirma.itt.commons.utils.classes;

import java.util.ArrayList;
import java.util.List;

import com.sirma.itt.commons.utils.classes.detectors.AbstractClassDetector;
import com.sirma.itt.commons.utils.classes.detectors.ClassDetectorBunch;
import com.sirma.itt.commons.utils.classes.detectors.ClassDetectorLoadingException;

/**
 * 
 * 
 * @author Hristo Iliev
 */
public final class AutoClassDetection {

    /**
     * Hide utility constructor.
     */
    private AutoClassDetection() {
	// Hide utility constructor
    }

    /**
     * Check if the value is from specified type and it contains enough
     * information to create an instance from this type.
     * 
     * @param value
     *            String, value which type will be detected
     * @param clazz
     *            {@link Class}, type which will be checked if the value fulfill
     *            it
     * @return true if the value fulfills the class type
     * @throws ClassDetectorLoadingException
     *             if cannot be loaded an detector for specified type
     */
    public static boolean detectClass(String value, Class<?> clazz)
	    throws ClassDetectorLoadingException {
	return ClassDetectorBunch.getInstance().getTwig(clazz.getName())
		.detect(value);
    }

    /**
     * Check if the value is from specified type and it contains enough
     * information to create an instance from the specified type. This method
     * will return the first such type.
     * 
     * @param value
     *            String, value which type will be detected
     * @param classes
     *            {@link Class}, types which will be checked if the value
     *            fulfill it
     * @return the first type which identify the value
     * @throws ClassDetectorLoadingException
     *             if cannot be loaded an detector for specified type
     */
    public static Class<?> getAllowedClass(String value, Class<?>... classes)
	    throws ClassDetectorLoadingException {
	for (Class<?> clazz : classes) {
	    if (detectClass(value, clazz)) {
		return clazz;
	    }
	}
	return null;
    }

    /**
     * Check if the value is from specified type and it contains enough
     * information to create an instance from the specified type. This method
     * will return the first such type.
     * 
     * @param value
     *            String, value which type will be detected
     * @param classes
     *            {@link Class}, types which will be checked if the value
     *            fulfill it
     * @return the first type which identify the value
     * @throws ClassDetectorLoadingException
     *             if cannot be loaded an detector for specified type
     */
    public static Class<?> getAllowedClass(String value, List<Class<?>> classes)
	    throws ClassDetectorLoadingException {
	for (Class<?> clazz : classes) {
	    if (detectClass(value, clazz)) {
		return clazz;
	    }
	}
	return null;
    }

    /**
     * Check if the value is from specified types and it contains enough
     * information to create an instance from the specified type. This method
     * will return all such types.
     * 
     * @param value
     *            String, value which type will be detected
     * @param classes
     *            {@link Class}, types which will be checked if the value
     *            fulfill it
     * @return the first type which identify the value
     * @throws ClassDetectorLoadingException
     *             if cannot be loaded an detector for specified type
     */
    public static List<Class<?>> getAllowedClasses(String value,
	    Class<?>... classes) throws ClassDetectorLoadingException {
	List<Class<?>> result = new ArrayList<Class<?>>();
	for (Class<?> clazz : classes) {
	    if (detectClass(value, clazz)) {
		result.add(clazz);
	    }
	}
	return result;
    }

    /**
     * Check if the value is from specified types and it contains enough
     * information to create an instance from the specified type. This method
     * will return all such types.
     * 
     * @param value
     *            String, value which type will be detected
     * @param classes
     *            {@link Class}, types which will be checked if the value
     *            fulfill it
     * @return the first type which identify the value
     * @throws ClassDetectorLoadingException
     *             if cannot be loaded an detector for specified type
     */
    public static List<Class<?>> getAllowedClasses(String value,
	    List<Class<?>> classes) throws ClassDetectorLoadingException {
	List<Class<?>> result = new ArrayList<Class<?>>();
	for (Class<?> clazz : classes) {
	    if (detectClass(value, clazz)) {
		result.add(clazz);
	    }
	}
	return result;
    }

    /**
     * Create an instance of the specified class. The initialization of the
     * class is made through the content of the <code>value</code>. To create an
     * instance of the specified class an derived of
     * {@link AbstractClassDetector} class should be written for the specified
     * type. See {@link AbstractClassDetector} for more information.
     * 
     * @param <E>
     *            type of created instance
     * @param value
     *            {@link String}, the value which contains the information which
     *            should be used to create the instance.
     * @param instanceClass
     *            {@link Class}, class of the created instance
     * @return E, the created instance initialized according the content of
     *         <code>value</code>
     * @throws ClassDetectorLoadingException
     *             thrown if the detector of the specified type cannot be found
     * @throws InstantiationException
     *             thrown if the detector cannot create an instance either
     *             because the detector does not provide the ability to create
     *             an instance of this type, or the information in
     *             <code>value</code> is not enough.
     * @see AbstractClassDetector
     */
    @SuppressWarnings("unchecked")
    public static <E> E createInstance(String value, Class<E> instanceClass)
	    throws ClassDetectorLoadingException, InstantiationException {
	AbstractClassDetector<E> classDetector = (AbstractClassDetector<E>) ClassDetectorBunch
		.getInstance().getTwig(instanceClass.getName());
	return classDetector.createNewInstance(value);
    }
}
