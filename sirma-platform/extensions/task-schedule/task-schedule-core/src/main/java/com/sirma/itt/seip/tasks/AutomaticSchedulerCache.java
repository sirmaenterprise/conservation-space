/*
 *
 */
package com.sirma.itt.seip.tasks;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessInjectionTarget;

import com.sirma.itt.seip.tasks.Schedule;

/**
 * Extension to collect and store information about automatic scheduler executions.
 *
 * @author BBonev
 */
public class AutomaticSchedulerCache implements Extension {

	private Map<String, SchedulerMethodCaller> cache = new HashMap<>(64);

	/**
	 * Gets the executable.
	 *
	 * @param name
	 *            the name
	 * @return the executable
	 */
	public SchedulerMethodCaller getExecutable(String name) {
		return cache.get(name);
	}

	/**
	 * Gets the found invocations
	 *
	 * @return the all method callers
	 */
	public Collection<SchedulerMethodCaller> getAll() {
		return Collections.unmodifiableCollection(cache.values());
	}

	/**
	 * Collect annotations.
	 *
	 * @param <X>
	 *            the generic type
	 * @param injectionTarget
	 *            the injection target
	 * @param beanManager
	 *            the bean manager
	 */
	<X> void collectAnnotations(@Observes ProcessInjectionTarget<X> injectionTarget, BeanManager beanManager) {
		AnnotatedType<X> annotatedType = injectionTarget.getAnnotatedType();
		for (AnnotatedMethod<? super X> annotatedMethod : annotatedType.getMethods()) {
			if (annotatedMethod.isAnnotationPresent(Schedule.class)) {
				SchedulerMethodCaller methodCaller = new SchedulerMethodCaller(annotatedType.getJavaClass(),
						annotatedMethod.getJavaMember(), beanManager);

				if (cache.containsKey(methodCaller.getIdentifier())) {
					injectionTarget.addDefinitionError(new IllegalStateException(
							"Duplicate identifier " + methodCaller.getIdentifier() + " found!"));
				} else {
					cache.put(methodCaller.getIdentifier(), methodCaller);
				}
			}
		}
	}
}
