package com.sirma.itt.seip.runtime;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.inject.Singleton;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.util.CDI;

/**
 * Collects startup components on startup and provides means of accessing them.
 *
 * @author BBonev
 */
@Singleton
public class StartupComponentProvider implements Extension {
	/** The component mapping. */
	private Map<StartupPhase, List<StartupComponent>> componentMapping = CollectionUtils.createLinkedHashMap(4);

	/**
	 * Gets the components for phase.
	 *
	 * @param phase
	 *            the phase
	 * @return the components for phase
	 */
	public List<StartupComponent> getComponentsForPhase(StartupPhase phase) {
		return new ArrayList<>(componentMapping.get(phase));
	}

	/**
	 * Gets the deployment components.
	 *
	 * @return the deployment components
	 */
	public List<StartupComponent> getDeploymentComponents() {
		return getComponentsForPhase(StartupPhase.DEPLOYMENT);
	}

	/**
	 * Gets the before application started components.
	 *
	 * @return the before application started components
	 */
	public List<StartupComponent> getBeforeApplicationStartedComponents() {
		return getComponentsForPhase(StartupPhase.BEFORE_APP_START);
	}

	/**
	 * Gets the after application started components.
	 *
	 * @return the after application started components
	 */
	public List<StartupComponent> getAfterApplicationStartedComponents() {
		return getComponentsForPhase(StartupPhase.AFTER_APP_START);
	}

	/**
	 * Before deployment.
	 *
	 * @param <X>
	 *            the generic type
	 * @param beanDiscovery
	 *            the bean discovery
	 */
	protected <X> void beforeDeployment(@Observes BeforeBeanDiscovery beanDiscovery) {
		for (StartupPhase startupPhase : StartupPhase.values()) {
			componentMapping.put(startupPhase, new LinkedList<StartupComponent>());
		}
	}

	/**
	 * After deployment.
	 *
	 * @param <X>
	 *            the generic type
	 * @param beanDiscovery
	 *            the bean discovery
	 */
	@SuppressWarnings("static-method")
	protected <X> void afterDeployment(@Observes AfterBeanDiscovery beanDiscovery, BeanManager manager) {
		CDI.setCachedBeanManager(manager);
		// do some validation here
	}

	/**
	 * On annotated type.
	 *
	 * @param <X>
	 *            the generic type
	 * @param pat
	 *            the annotated type
	 * @param beanManager
	 *            the bean manager
	 */
	<T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat, BeanManager beanManager) {
		AnnotatedType<T> annotatedType = pat.getAnnotatedType();
		if (annotatedType.isAnnotationPresent(Startup.class)) {
			registerClassAnnotation(annotatedType, beanManager);
		}
		Set<AnnotatedMethod<? super T>> methods = annotatedType.getMethods();
		for (AnnotatedMethod<? super T> annotatedMethod : methods) {
			if (annotatedMethod.isAnnotationPresent(Startup.class)) {
				registerMethodAnnotation(annotatedMethod, beanManager);
			}
		}

	}

	/**
	 * Register class annotation.
	 *
	 * @param <X>
	 *            the generic type
	 * @param annotatedType
	 *            the annotated type
	 * @param beanManager
	 *            the bean manager
	 */
	private <X> void registerClassAnnotation(AnnotatedType<X> annotatedType, BeanManager beanManager) {
		Startup annotation = annotatedType.getAnnotation(Startup.class);
		CollectionUtils.addValueToMap(componentMapping, annotation.phase(),
				new StartupClassComponentInstance(annotation, annotatedType.getJavaClass(), beanManager));
	}

	/**
	 * Register method annotation.
	 *
	 * @param <X>
	 *            the generic type
	 * @param annotatedType
	 *            the annotated type
	 * @param beanManager
	 *            the bean manager
	 */
	private <X> void registerMethodAnnotation(AnnotatedMethod<X> annotatedType, BeanManager beanManager) {
		Startup annotation = annotatedType.getAnnotation(Startup.class);

		CollectionUtils.addValueToMap(componentMapping, annotation.phase(), new StartupMethodComponentInstance(annotation,
				annotatedType.getDeclaringType().getJavaClass(), annotatedType.getJavaMember(), beanManager));
	}

}