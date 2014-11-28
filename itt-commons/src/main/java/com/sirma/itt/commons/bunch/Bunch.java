/**
 * Copyright (c) 2008 15.08.2008 , Sirma ITT.
/*

/**
 * 
 */
package com.sirma.itt.commons.bunch;

import java.util.HashMap;

/**
 * Abstract bunch representation. The purpose of bunch is to store a set of
 * object from type T with associated name for the object. To retrieve such
 * object from the bunch {@link #getTwig(String)} method should be used. If the
 * object is not in the bunch it is then tried to be loaded with
 * {@link #loadTwig(String)} method.
 * 
 * @author Hristo Iliev
 * @param <K>
 *            Type of the keys by which are searched the keys
 * @param <T>
 *            Type of the stored twigs.
 * @param <E>
 *            Exception thrown if the twig is not in the bunch and it cannot be
 *            loaded.
 */
public abstract class Bunch<K, T, E extends TwigLoadingException> extends
	HashMap<K, T> {

    /**
     * Comment for serialVersionUID.
     */
    private static final long serialVersionUID = 5592214561089086397L;

    /**
     * Retrieve an object from the set according the specified name. If the
     * object is not in the repository then the it is tried to be loaded. If the
     * loading fails too, then the E exception is thrown.
     * 
     * @param twigName
     *            {@link String}, name of the object in the repository
     * @return retrieved object
     * @throws E
     *             thrown if the object cannot be loaded
     */
    public final synchronized T getTwig(String twigName) throws E {
	T result = get(getTwigKey(twigName));
	if (result == null) {
	    result = loadTwig(twigName);
	    put(createTwigKey(twigName, result), result);
	}
	return result;
    }

    /**
     * Load the object according the twigName. If the loading fails E exception
     * should be thrown.
     * 
     * @param twigName
     *            String, name of the object which should be loaded
     * @return loaded object
     * @throws E
     *             thrown if the object cannot be loaded
     */
    @SuppressWarnings("unchecked")
    private T loadTwig(String twigName) throws E {
	Exception cause = null;
	Class<T> twigClass = null;
	try {
	    twigClass = (Class<T>) Class.forName(getTwigClass(twigName));
	    if (isValidTwigClass(twigClass)) {
		return twigClass.getConstructor(String.class).newInstance(
			twigName);
	    }
	} catch (Exception e) {
	    cause = e;
	}
	return manageException(twigName, cause);
    }

    /**
     * Create new key for the specified object name. The returned value will be
     * used as further access key for the twig.
     * 
     * @param twigName
     *            String, name of the object
     * @param twig
     *            T, created object
     * @return K, key for the object
     */
    protected abstract K createTwigKey(String twigName, T twig);

    /**
     * Retrieve key in the bunch for the specified object name. This method
     * should try to get the key according the twigName. To retrieve the current
     * keys use {@link #keySet()} method. If the key is not in the set then null
     * should be returned. If null is returned then the bunch will try to load
     * the twig.
     * 
     * @param twigName
     *            String, name of the object
     * @return K, retrieved key
     */
    protected abstract K getTwigKey(String twigName);

    /**
     * Manage exception if loading fails. This method should basically throw
     * exception of type E.
     * 
     * @param twigName
     *            String, name of the twig which is tried to be loaded
     * 
     * @param cause
     *            Exception, the exception to be managed
     * @return loaded object if the exception can be managed
     * @throws E
     *             thrown if the exception cannot be managed.
     */
    protected abstract T manageException(String twigName, Exception cause)
	    throws E;

    /**
     * Check if the class is correct class to be used for instantiation of
     * object. This method is should check if the class can be add to the bunch.
     * To be valid twig class then the class should be either subclass or the
     * <code>T</code> class itself.
     * 
     * @param twigClass
     *            Class<?>, class to be checked
     * @return true if the class is valid and can be added to the bunch
     */
    protected abstract boolean isValidTwigClass(Class<?> twigClass);

    /**
     * Get the name of the class to be loaded. This method should retrieve the
     * name of the class according the twig name.
     * 
     * @param twigName
     *            String, name of the object for which will be retrieved the
     *            class
     * @return String, name of the class to be loaded
     */
    protected abstract String getTwigClass(String twigName);
}
