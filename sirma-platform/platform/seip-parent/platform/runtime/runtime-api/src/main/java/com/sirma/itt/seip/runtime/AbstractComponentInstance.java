package com.sirma.itt.seip.runtime;

import javax.enterprise.inject.spi.BeanManager;

/**
 * Base {@link Component} implementation.
 *
 * @author BBonev
 */
public abstract class AbstractComponentInstance implements Component {

	/** The actual class. */
	protected Class<?> actualClass;

	/** The bean manager. */
	private BeanManager beanManager;

	/**
	 * Instantiates a new abstract component instance.
	 *
	 * @param actualClass
	 *            the actual class
	 * @param beanManager
	 *            the bean manager
	 */
	public AbstractComponentInstance(Class<?> actualClass, BeanManager beanManager) {
		this.actualClass = actualClass;
		this.beanManager = beanManager;
	}

	/**
	 * Instantiates a new abstract component instance.
	 */
	public AbstractComponentInstance() {
		super();
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	@Override
	public String getName() {
		return actualClass.getSimpleName();
	}

	/**
	 * Gets the actual class.
	 *
	 * @return the actual class
	 */
	@Override
	public Class<?> getActualClass() {
		return actualClass;
	}

	/**
	 * Gets the bean manager.
	 *
	 * @return the bean manager
	 */
	protected BeanManager getBeanManager() {
		return beanManager;
	}

	@Override
	public String toString() {
		return new StringBuilder(128).append("Component [name=").append(getName()).append("]").toString();
	}

}