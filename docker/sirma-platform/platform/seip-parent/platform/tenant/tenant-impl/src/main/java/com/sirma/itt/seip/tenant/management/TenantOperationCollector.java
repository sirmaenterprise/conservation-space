package com.sirma.itt.seip.tenant.management;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import com.sirma.itt.seip.runtime.Component;
import com.sirma.itt.seip.runtime.MethodComponentInstance;
import com.sirma.itt.seip.security.annotation.OnTenantAdd;
import com.sirma.itt.seip.security.annotation.OnTenantRemove;

/**
 * Collector, responsible for collecting all Components, annotated with {@link OnTenantRemove} or
 * {@link OnTenantAdd}.
 *
 * @author nvelkov
 */
public class TenantOperationCollector implements Extension {

	private List<Component> tenantAddedComponents = new ArrayList<>();

	private List<Component> tenantRemovedComponets = new ArrayList<>();

	/**
	 * Observe all annotated types. Add all {@link OnTenantAdd} components to the tenantAddedComponents list and all
	 * {@link OnTenantRemove} components to the tenantRemovedComponents list. {@link OnTenantAdd} components are sorted
	 * by their order.
	 *
	 * @param processAnnotatedType
	 *            the annotated type
	 * @param beanManager
	 *            the bean manager
	 */
	<T> void registerDestination(@Observes ProcessAnnotatedType<T> processAnnotatedType, BeanManager beanManager) {
		AnnotatedType<T> type = processAnnotatedType.getAnnotatedType();
		Method[] methods = type.getJavaClass().getDeclaredMethods();
		for (Method method : methods) {
			if (method.isAnnotationPresent(OnTenantRemove.class)) {
				tenantRemovedComponets.add(new MethodComponentInstance(
						processAnnotatedType.getAnnotatedType().getJavaClass(), method, beanManager));
			}
			if (method.isAnnotationPresent(OnTenantAdd.class)) {
				tenantAddedComponents.add(new MethodComponentInstance(
						processAnnotatedType.getAnnotatedType().getJavaClass(), method, beanManager));
			}
		}

		tenantAddedComponents.sort(sortComponentsByOrder(OnTenantAdd.class, OnTenantAdd::order));
		tenantRemovedComponets.sort(sortComponentsByOrder(OnTenantRemove.class, OnTenantRemove::order));
	}

	private static <T extends Annotation> Comparator<Component> sortComponentsByOrder(Class<T> annotationType, Function<T, Double> orderProvider) {
		return (componentA, componentB) -> {
			T annotationA = componentA.getAnnotated().getAnnotation(annotationType);
			T annotationB = componentB.getAnnotated().getAnnotation(annotationType);

			Double orderA = orderProvider.apply(annotationA);
			Double orderB = orderProvider.apply(annotationB);
			return Double.compare(orderA, orderB);
		};
	}

	/**
	 * Get all components annotated with {@link OnTenantAdd}.
	 *
	 * @return all tenant add components
	 */
	public List<Component> getTenantAddedComponents() {
		return tenantAddedComponents;
	}

	/**
	 * Get all components annotated with {@link OnTenantRemove}
	 *
	 * @return all tenant remove components
	 */
	public List<Component> getTenantRemovedComponets() {
		return tenantRemovedComponets;
	}

}
