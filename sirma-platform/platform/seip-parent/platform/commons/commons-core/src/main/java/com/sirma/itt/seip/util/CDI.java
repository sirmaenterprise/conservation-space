package com.sirma.itt.seip.util;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Named;
import javax.inject.Singleton;

import com.sirma.itt.seip.collections.CollectionUtils;

/**
 * Utilities and helpers for CDI
 *
 * @author Adrian Mitev
 */
public class CDI {

	private static BeanManager cachedBeanManager;

	/**
	 * Instantiates a new cdi.
	 */
	private CDI() {
		// utility class
	}

	/**
	 * Sets the given bean manager as a static instance in the class. This should be used in context where the lookup
	 * does not work properly (not initialized yet).
	 *
	 * @param beanManager
	 *            the new cached bean manager
	 */
	public static void setCachedBeanManager(BeanManager beanManager) {
		cachedBeanManager = beanManager;
	}

	/**
	 * Gets statically cached bean manager or <code>null</code> if not initialized, yet.
	 *
	 * @return the cached bean manager
	 */
	public static BeanManager getCachedBeanManager() {
		return cachedBeanManager;
	}

	/**
	 * Instantiates default instance of a bean (@Default qualifier). The bean instance created by this methods may or
	 * may not call the bean {@link PostConstruct} method. The instance is created using
	 * {@link BeanManager#getReference(Bean, java.lang.reflect.Type, CreationalContext)} method.
	 *
	 * @param <T>
	 *            bean type
	 * @param type
	 *            bean class.
	 * @param beanManager
	 *            BeanManager.
	 * @param annotation
	 *            bean qualifier.
	 * @return instantiated contextual instance.
	 */
	public static <T> T instantiateBean(Class<T> type, BeanManager beanManager, Annotation... annotation) {
		Set<Bean<?>> beans = beanManager.getBeans(type, annotation);
		return instantiateBean(type, beanManager, beans);
	}

	/**
	 * Instantiate named bean.
	 *
	 * @param beanName
	 *            the bean name
	 * @param type
	 *            the type
	 * @param beanManager
	 *            the bean manager
	 * @param <T>
	 *            the generic type
	 * @return the t
	 */
	public static <T> T instantiateBean(String beanName, Class<T> type, BeanManager beanManager) {
		Set<Bean<?>> beans = beanManager.getBeans(beanName);
		return instantiateBean(type, beanManager, beans);
	}

	/**
	 * Instantiate bean.
	 *
	 * @param <T>
	 *            the generic type
	 * @param type
	 *            the type
	 * @param beanManager
	 *            the bean manager
	 * @param beans
	 *            the beans
	 * @return the t
	 */
	@SuppressWarnings("unchecked")
	private static <T> T instantiateBean(Class<T> type, BeanManager beanManager, Set<Bean<?>> beans) {
		Bean<T> provider = (Bean<T>) beanManager.resolve(beans);
		if (isEmpty(beans)) {
			return null;
		}
		CreationalContext<T> cc = beanManager.createCreationalContext(provider);
		return (T) beanManager.getReference(provider, type, cc);
	}

	/**
	 * Instantiates default instance of a bean (@Default qualifier). The bean instance created by this methods calls the
	 * bean {@link PostConstruct} method. The instance is created using {@link Bean#create(CreationalContext)} method.
	 * Note that multiple instantiations of the same bean will result different instances no event if marked for
	 * {@link ApplicationScoped} or {@link Singleton}
	 *
	 * @param <T>
	 *            bean type
	 * @param type
	 *            bean class.
	 * @param beanManager
	 *            BeanManager.
	 * @param annotation
	 *            bean qualifier.
	 * @return instantiated contextual instance.
	 */
	public static <T> T instantiate(Class<T> type, BeanManager beanManager, Annotation... annotation) {
		Set<Bean<?>> beans = beanManager.getBeans(type, annotation);
		return instantiate(beanManager, beans);
	}

	/**
	 * Instantiate named bean. Instantiates default instance of a bean (@Default qualifier). The bean instance created
	 * by this methods calls the bean {@link PostConstruct} method. The instance is created using
	 * {@link Bean#create(CreationalContext)} method. Note that multiple instantiations of the same bean will result
	 * different instances no event if marked for {@link ApplicationScoped} or {@link Singleton}
	 *
	 * @param beanName
	 *            the bean name
	 * @param beanManager
	 *            the bean manager
	 * @param <T>
	 *            the generic type
	 * @return the t
	 */
	public static <T> T instantiate(String beanName, BeanManager beanManager) {
		Set<Bean<?>> beans = beanManager.getBeans(beanName);
		return instantiate(beanManager, beans);
	}

	/**
	 * Instantiate bean.
	 *
	 * @param beanManager
	 *            the bean manager
	 * @param beans
	 *            the beans
	 * @param <T>
	 *            the generic type
	 * @return the t
	 */
	@SuppressWarnings("unchecked")
	private static <T> T instantiate(BeanManager beanManager, Set<Bean<?>> beans) {
		Bean<T> provider = (Bean<T>) beanManager.resolve(beans);
		if (provider == null) {
			return null;
		}
		CreationalContext<T> cc = beanManager.createCreationalContext(provider);
		return provider.create(cc);
	}

	/**
	 * Instantiates default instance of a bean (@Default qualifier).
	 *
	 * @param <T>
	 *            bean type.
	 * @param type
	 *            bean class.
	 * @param beanManager
	 *            BeanManager.
	 * @return instantiated contextual instance.
	 */
	public static <T> T instantiateDefaultBean(Class<T> type, BeanManager beanManager) {
		return instantiateBean(type, beanManager, DEFAULT_LITERAL);
	}

	/**
	 * Invoke method declared in given class. The parameters of the method will be resolved using the given bean
	 * manager. If the method is non static then the class will be instantiated using the {@link BeanManager}.
	 *
	 * @param targetClass
	 *            the target class
	 * @param method
	 *            the method
	 * @param beanManager
	 *            the bean manager
	 * @throws Exception
	 *             if an exception occurs.
	 */
	public static void invokeMethod(Class<?> targetClass, Method method, BeanManager beanManager) throws Exception { //NOSONAR

		Object bean;
		// for static methods return the class
		if (Modifier.isStatic(method.getModifiers())) {
			bean = targetClass;
		} else {
			bean = resolveNonNullBean(targetClass, beanManager, getQualifers(targetClass, beanManager));
		}
		method.setAccessible(true);
		method.invoke(bean, buildArgumentsList(method, beanManager));
	}

	private static Object[] buildArgumentsList(Method targetMethod, BeanManager beanManager) {
		Class<?>[] parameterTypes = targetMethod.getParameterTypes();
		if (parameterTypes.length == 0) {
			// this should be null otherwise the invoked method will fail if passed empty array
			return null; // NOSONAR
		}
		Object[] parameterValues = new Object[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			Class<?> parameterType = parameterTypes[i];
			Annotation[] annotations = targetMethod.getParameterAnnotations()[i];
			parameterValues[i] = resolveNonNullBean(parameterType, beanManager, annotations);
		}
		return parameterValues;
	}

	private static Object resolveNonNullBean(Class<?> beanType, BeanManager beanManager, Annotation... annotations) {
		Object bean = instantiateBean(beanType, beanManager, annotations);
		return Objects.requireNonNull(bean, "Could not find bean of type " + beanType.getName());
	}

	/**
	 * Gets the qualifers.
	 *
	 * @param bean
	 *            the bean
	 * @param beanManager
	 *            the bean manager
	 * @return the qualifers
	 */
	public static Annotation[] getQualifers(Class<?> bean, BeanManager beanManager) {
		Annotation[] annotations = bean.getAnnotations();
		List<Annotation> result = new ArrayList<>();
		for (Annotation annotation : annotations) {
			if (!Named.class.equals(annotation.annotationType())
					&& beanManager.isQualifier(annotation.annotationType())) {
				result.add(annotation);
			}
		}
		return CollectionUtils.toArray(result, Annotation.class);
	}

	/**
	 * Provides an instance of the @Default qualifier.
	 *
	 * @return literal instance.
	 */
	public static AnnotationLiteral<Default> getDefaultLiteral() {
		return DEFAULT_LITERAL;
	}

	/**
	 * Provides an instance of the @Any qualifier.
	 *
	 * @return literal instance.
	 */
	public static AnnotationLiteral<Any> getAnyLiteral() {
		return ANY_LITERAL;
	}

	/**
	 * Instance of the @Default qualifier.
	 */
	private static final AnnotationLiteral<Default> DEFAULT_LITERAL = new AnnotationLiteral<Default>() { // NOSONAR
		private static final long serialVersionUID = 3592021840679865983L;
	};

	/**
	 * Instance of the @Any qualifier.
	 */
	private static final AnnotationLiteral<Any> ANY_LITERAL = new AnnotationLiteral<Any>() { // NOSONAR
		private static final long serialVersionUID = 3592021840679865983L;
	};

	/**
	 * Discovers and instantiates beans of given type, then passes them to the consumer. The method will do nothing, if
	 * no bean are found for the given type.
	 *
	 * @param <C>
	 *            the expected bean type
	 * @param beanManager
	 *            {@link BeanManager} that should be used for beans discovery and instantiation
	 * @param beanType
	 *            the type of the beans that should be registered
	 * @param register
	 *            accepts every bean instance
	 */
	@SuppressWarnings("unchecked")
	public static <C> void registerBeans(BeanManager beanManager, Type beanType, Consumer<C> register) {
		Set<Bean<?>> beans = beanManager.getBeans(beanType, CDI.getDefaultLiteral());
		if (isEmpty(beans)) {
			return;
		}

		for (Bean<?> bean : beans) {
			C instance = (C) instantiate(bean, beanManager);
			register.accept(instance);
		}
	}

	private static <C> C instantiate(Bean<C> bean, BeanManager beanManager) {
		return bean.create(beanManager.createCreationalContext(bean));
	}
}
