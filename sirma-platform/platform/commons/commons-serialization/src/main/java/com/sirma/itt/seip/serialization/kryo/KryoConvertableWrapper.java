package com.sirma.itt.seip.serialization.kryo;

/**
 * Wrapper class used for some conversions in {@link com.sirma.itt.seip.convert.TypeConverter}.
 *
 * @author BBonev
 */
public class KryoConvertableWrapper implements Cloneable {

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
	public Object clone() { // NOSONAR
		return new KryoConvertableWrapper(target);
	}

}
