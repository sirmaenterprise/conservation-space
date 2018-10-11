package com.sirma.itt.seip.runtime;

import java.util.Arrays;
import java.util.Collection;

import javax.enterprise.inject.spi.BeanManager;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;

/**
 * Base {@link StartupComponent} implementation.
 *
 * @author nvelkov
 * @see StartupComponent
 */
public abstract class AbstractStartupComponentInstance extends AbstractComponentInstance implements StartupComponent {

	private Startup annotation;

	/**
	 * Init the {@link AbstractStartupComponentInstance}.
	 *
	 * @param annotation
	 *            the startup annotation
	 * @param actualClass
	 *            the actual class
	 * @param beanManager
	 *            the bean manager
	 */
	public AbstractStartupComponentInstance(Startup annotation, Class<?> actualClass, BeanManager beanManager) {
		super(actualClass, beanManager);
		this.annotation = annotation;
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
		if (StringUtils.isBlank(name)) {
			name = super.getName();
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
	 * Gets the annotation.
	 *
	 * @return the annotation
	 */
	protected Startup getAnnotation() {
		return annotation;
	}

}