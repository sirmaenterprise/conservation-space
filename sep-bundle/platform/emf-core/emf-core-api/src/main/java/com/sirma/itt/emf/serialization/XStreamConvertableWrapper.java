package com.sirma.itt.emf.serialization;

import com.sirma.itt.emf.domain.model.GenericProxy;

/**
 * Wrapper class used for some conversions in {@link com.sirma.itt.emf.converter.TypeConverter}.
 * 
 * @author BBonev
 */
public class XStreamConvertableWrapper implements GenericProxy<Object> {

	/** The target. */
	private Object target;

	/**
	 * Instantiates a new convertable wrapper.
	 */
	public XStreamConvertableWrapper() {
	}

	/**
	 * Instantiates a new convertable wrapper.
	 *
	 * @param target
	 *            the target
	 */
	public XStreamConvertableWrapper(Object target) {
		this.target = target;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getTarget() {
		return target;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setTarget(Object target) {
		this.target = target;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object cloneProxy() {
		return clone();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object clone() {
		return new XStreamConvertableWrapper(target);
	}

}
