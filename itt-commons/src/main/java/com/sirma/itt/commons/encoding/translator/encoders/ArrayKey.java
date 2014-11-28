/*
 * Created on 15.02.2008 @ 16:11:41
 *
 * Author: Hristo Iliev
 * Company: ITT
 * Email: hristo.iliev@ittbg.com
 */
package com.sirma.itt.commons.encoding.translator.encoders;

/**
 * This class is used as wrapper of byte array. This class implement basic
 * functionality upon the array so this class may be used as key for
 * {@link java.util.HashMap}.
 * 
 * @author Hristo Iliev
 * 
 */
public class ArrayKey {

    /** array of bytes. */
    private final byte[] array;

    /** hash code of the key. */
    private int hash;

    /**
     * @param array
     *                byte[], the array of bytes
     */
    public ArrayKey(byte[] array) {
	this.array = array;
	generateHashcode();
    }

    /**
     * Generate an hash code from the values of the array.
     */
    private void generateHashcode() {
	if ((array == null) || (array.length == 0)) {
	    hash = super.hashCode();
	    return;
	}
	hash = array[0];
	if (array.length > 1) {
	    if (array.length < 5) {
		for (int index = 1; index < array.length; index++) {
		    hash = (hash << 8) & array[index];
		}
	    } else {
		for (int index = array.length - 4; index < array.length; index++) {
		    hash = (hash << 8) & array[index];
		}
	    }
	}
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
	return hash;
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj) {
	if (obj instanceof ArrayKey) {
	    ArrayKey keyObj = (ArrayKey) obj;
	    if (array.length != keyObj.array.length) {
		return false;
	    }
	    for (int index = 0; index < array.length; index++) {
		if (array[index] != keyObj.array[index]) {
		    return false;
		}
	    }
	    return true;
	}
	return false;
    }

    /**
     * @return the array
     */
    public final byte[] getArray() {
	return array;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
	if (array == null) {
	    return super.toString();
	}
	StringBuffer result = new StringBuffer().append('[');
	for (int index = 0; index < array.length; index++) {
	    result.append(Integer.toString(array[index], 16));
	    if (index != array.length - 1) {
		result.append(' ');
	    }
	}
	result.append(']');
	return result.toString();
    }
}
