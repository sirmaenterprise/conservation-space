package com.sirma.itt.seip.runtime;

import java.util.Arrays;
import java.util.Collection;

import javax.enterprise.inject.spi.BeanManager;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;

/**
 * Base {@link Component} implementation.
 *
 * @author BBonev
 */
public abstract class AbstractComponentInstance implements Component {

	/** The actual class. */
	private Class<?> actualClass;
	/** The annotation. */
	private Startup annotation;
	/** The bean manager. */
	private BeanManager beanManager;

	/**
	 * Instantiates a new abstract component instance.
	 *
	 * @param annotation
	 *            the annotation
	 * @param actualClass
	 *            the actual class
	 * @param beanManager
	 *            the bean manager
	 */
	public AbstractComponentInstance(Startup annotation, Class<?> actualClass, BeanManager beanManager) {
		this.annotation = annotation;
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
	 * Gets the phase.
	 *
	 * @return the phase
	 */
	@Override
	public StartupPhase getPhase() {
		return annotation.phase();
	}

	/**
	 * Checks if is async.
	 *
	 * @return true, if is async
	 */
	@Override
	public boolean isAsync() {
		return annotation.async();
	}

	/**
	 * Gets the order.
	 *
	 * @return the order
	 */
	@Override
	public double getOrder() {
		return annotation.order();
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	@Override
	public String getName() {
		String name = annotation.name();
		if (StringUtils.isNullOrEmpty(name)) {
			name = actualClass.getSimpleName();
		}
		return name;
	}

	/**
	 * Gets the depends on.
	 *
	 * @return the depends on
	 */
	@Override
	public Collection<String> getDependsOn() {
		return Arrays.asList(annotation.dependsOn());
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
	 * Gets the annotation.
	 *
	 * @return the annotation
	 */
	protected Startup getAnnotation() {
		return annotation;
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
		return new StringBuilder(128)
				.append("Component [name=")
					.append(getName())
					.append(", phase=")
					.append(getPhase())
					.append(", order=")
					.append(getOrder())
					.append(", async=")
					.append(isAsync())
					.append("]")
					.toString();
	}


}