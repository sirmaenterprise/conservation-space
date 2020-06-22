package com.sirma.itt.seip.db.customtype;

import java.io.Serializable;

import org.hibernate.usertype.UserType;

/**
 * Adapter class for hibernate {@link UserType}
 *
 * @author BBonev
 */
public abstract class UserTypeAdapter implements UserType {

	/**
	 * Copy the current object.
	 *
	 * @param value
	 *            The value that needs to be copy
	 * @return A copy
	 */
	@Override
	public Object deepCopy(Object value) {
		return value;
	}

	/**
	 * Defines mutable. The current type is mutable.
	 *
	 * @return always <code>true</code>.
	 */
	@Override
	public boolean isMutable() {
		return true;
	}

	/**
	 * Assembles the object.
	 *
	 * @param cached
	 *            Cached instance
	 * @param owner
	 *            Owner
	 * @return The object
	 */
	@Override
	public Object assemble(Serializable cached, Object owner) {
		return cached;
	}

	/**
	 * Disassembles the object.
	 *
	 * @param value
	 *            The object
	 * @return Serializable @ the hibernate exception
	 */
	@Override
	public Serializable disassemble(Object value) {
		return (Serializable) value;
	}

	/**
	 * Replace the current object.
	 *
	 * @param original
	 *            The original object
	 * @param target
	 *            The target
	 * @param owner
	 *            The owner
	 * @return The object
	 */
	@Override
	public Object replace(Object original, Object target, Object owner) {
		return original;
	}

	/**
	 * Equals two objects.
	 *
	 * @param x
	 *            First Object
	 * @param y
	 *            Second object
	 * @return True if the objects are equals or false if they are not
	 */
	@Override
	public boolean equals(Object x, Object y) { // NOSONAR
		if (x == y) { // NOSONAR
			return true;
		}
		if (null == x || null == y) {
			return false;
		}
		return x.equals(y);
	}

	/**
	 * Returns hashcode of the current objects.
	 *
	 * @param arg0
	 *            Object to hash
	 * @return The hashcode
	 */
	@Override
	public int hashCode(Object arg0) {
		return arg0.hashCode();
	}

}