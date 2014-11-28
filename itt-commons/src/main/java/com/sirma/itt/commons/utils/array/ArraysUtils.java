/**
 * Copyright (c) 2008 09.09.2008 , Sirma ITT.
/*

/**
 * 
 */
package com.sirma.itt.commons.utils.array;

import java.lang.reflect.Array;
import java.util.List;

/**
 * 
 * 
 * @author Hristo Iliev
 */
public final class ArraysUtils {

    /**
     * Hide default constructor.
     */
    private ArraysUtils() {
	// Hide default constructor
    }

    /**
     * Convert list of arrays of objects to array of arrays of objects.
     * 
     * @param <T>
     *                type of the array
     * 
     * @param list
     *                {@link List}, list of arrays
     * @param arrayElements
     *                Class, class of the array type
     * @return Object
     */
    @SuppressWarnings("unchecked")
    public static <T> T[][] listOfArraysToDoubleArray(List<T[]> list, Class<T> arrayElements) {
	T[][] result = (T[][]) Array.newInstance(arrayElements, new int[] {
		list.size(), 0 });
	int i = 0;
	for (T[] array : list) {
	    result[i++] = array;
	}
	return result;
    }

}
