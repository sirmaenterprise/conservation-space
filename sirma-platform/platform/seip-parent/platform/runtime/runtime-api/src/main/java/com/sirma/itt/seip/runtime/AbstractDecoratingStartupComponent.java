/**
 *
 */
package com.sirma.itt.seip.runtime;

import java.lang.reflect.AnnotatedElement;
import java.util.Collection;

import com.sirma.itt.seip.runtime.boot.StartupPhase;

/**
 * Abstract component that can be used to decorate other components behavior. The implementer should
 * provide implementation for at least {@link #execute()} method.
 *
 * @author BBonev
 */
public abstract class AbstractDecoratingStartupComponent implements StartupComponent {

	private final StartupComponent decorated;

	/**
	 * Instantiates a new abstract decorating component.
	 *
	 * @param decorated
	 *            the decorated
	 */
	public AbstractDecoratingStartupComponent(StartupComponent decorated) {
		this.decorated = decorated;
	}

	/**
	 * Gets the phase.
	 *
	 * @return the phase
	 */
	@Override
	public StartupPhase getPhase() {
		return decorated.getPhase();
	}

	/**
	 * Checks if is async.
	 *
	 * @return true, if is async
	 */
	@Override
	public boolean isAsync() {
		return decorated.isAsync();
	}

	/**
	 * Gets the order.
	 *
	 * @return the order
	 */
	@Override
	public double getOrder() {
		return decorated.getOrder();
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	@Override
	public String getName() {
		return decorated.getName();
	}

	/**
	 * Gets the depends on.
	 *
	 * @return the depends on
	 */
	@Override
	public Collection<String> getDependsOn() {
		return decorated.getDependsOn();
	}

	/**
	 * Gets the actual class.
	 *
	 * @return the actual class
	 */
	@Override
	public Class<?> getActualClass() {
		return decorated.getActualClass();
	}

	/**
	 * Gets the annotated.
	 *
	 * @return the annotated
	 */
	@Override
	public AnnotatedElement getAnnotated() {
		return decorated.getAnnotated();
	}

	/**
	 * Gets the decorated.
	 *
	 * @return the decorated
	 */
	protected StartupComponent getDecorated() {
		return decorated;
	}

}