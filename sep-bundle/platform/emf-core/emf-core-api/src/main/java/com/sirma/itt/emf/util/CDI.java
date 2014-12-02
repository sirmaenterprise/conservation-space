package com.sirma.itt.emf.util;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Utilities and helpers for CDI
 *
 * @author Adrian Mitev
 */
public class CDI {

	private static final String BEAN_MANAGER_JNDI = "java:comp/"
			+ BeanManager.class.getSimpleName();

	/**
	 * Looks up BeanManager by jndi name.
	 *
	 * @return BeanManager instance.
	 */
	public static BeanManager lookupBeanManager() {
		try {
			InitialContext ic = new InitialContext();
			return (BeanManager) ic.lookup(BEAN_MANAGER_JNDI);
		} catch (NamingException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Instantiates default instance of a bean (@Default qualifier).
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
	public static <T> T instantiateBean(Class<T> type, BeanManager beanManager,
			Annotation... annotation) {
		Set<Bean<?>> beans = beanManager.getBeans(type, annotation);
		T providerInstance = instantiateBean(type, beanManager, beans);
		return providerInstance;
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
		T providerInstance = instantiateBean(type, beanManager, beans);
		return providerInstance;
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
		Iterator<Bean<?>> it = null;
		if ((beans == null) || ((it = beans.iterator()) == null) || !it.hasNext()) {
			return null;
		}
		Bean<T> provider = (Bean<T>) it.next();
		CreationalContext<T> cc = beanManager.createCreationalContext(provider);
		T providerInstance = (T) beanManager.getReference(provider, type, cc);
		return providerInstance;
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
	private static final AnnotationLiteral<Default> DEFAULT_LITERAL = new AnnotationLiteral<Default>() {
		private static final long serialVersionUID = 3592021840679865983L;
	};

	/**
	 * Instance of the @Any qualifier.
	 */
	private static final AnnotationLiteral<Any> ANY_LITERAL = new AnnotationLiteral<Any>() {
		private static final long serialVersionUID = 3592021840679865983L;
	};

}
