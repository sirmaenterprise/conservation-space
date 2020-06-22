package com.sirma.itt.seip.runtime;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

import com.sirma.itt.seip.runtime.boot.FailedExecutionException;

/**
 * Component instance definition. This is common interface to defining component instances. Each
 * instance should point to a single unit that can be executed. These components are loaded during
 * deployment and can be executed when needed.
 * <p>
 * An example for this is the {@link com.sirma.itt.seip.security.annotation.OnTenantRemove}
 * annotation.
 *
 * @author BBonev
 * @see {@link com.sirma.itt.seip.security.annotation.OnTenantRemove}
 * @see {@link com.sirma.itt.seip.security.annotation.OnTenantAdd}}
 */
public interface Component {

	/**
	 * This component name. This name will be used to determine the other components dependencies
	 * and requirements.
	 *
	 * @return this component name.
	 */
	String getName();

	/**
	 * Gets the actual class.
	 *
	 * @return the actual class
	 */
	Class<?> getActualClass();

	/**
	 * Execute the component.
	 * 
	 * @throws FailedExecutionException
	 *             the exception
	 */
	void execute();

	/**
	 * Gets the annotated object that represents the current component.
	 *
	 * @return the annotated
	 */
	AnnotatedElement getAnnotated();

	/**
	 * Checks if is annotation present.
	 *
	 * @param annotationType
	 *            the annotation type
	 * @return true, if is annotation present
	 */
	default boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
		return getAnnotated().isAnnotationPresent(annotationType);
	}

	/**
	 * Gets the annotation from the annotated element
	 *
	 * @param <A>
	 *            the generic type
	 * @param annotationType
	 *            the annotation type
	 * @return the annotation
	 */
	default <A extends Annotation> A getAnnotation(Class<A> annotationType) {
		return getAnnotated().getAnnotation(annotationType);
	}
}
