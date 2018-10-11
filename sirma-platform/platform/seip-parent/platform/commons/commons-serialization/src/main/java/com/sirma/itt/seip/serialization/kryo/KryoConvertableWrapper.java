package com.sirma.itt.seip.serialization.kryo;

import com.sirma.itt.seip.Copyable;

/**
 * Wrapper class used for some conversions in {@link com.sirma.itt.seip.convert.TypeConverter}.
 *
 * @author BBonev
 */
public class KryoConvertableWrapper implements Copyable<KryoConvertableWrapper> {

	private Object target;

	/**
	 * Instantiates a new convertable wrapper.
	 */
	public KryoConvertableWrapper() {
		// default constructor
	}

	/**
	 * Instantiates a new convertable wrapper.
	 *
	 * @param target
	 *            the target
	 */
	public KryoConvertableWrapper(Object target) {
		this.target = target;
	}

	/**
	 * Gets the target.
	 *
	 * @return the target
	 */
	public Object getTarget() {
		return target;
	}

	/**
	 * Sets the target.
	 *
	 * @param target
	 *            the new target
	 */
	public void setTarget(Object target) {
		this.target = target;
	}

	@Override
	public KryoConvertableWrapper createCopy() {
		return new KryoConvertableWrapper(target);
	}

}
