package com.sirma.itt.emf.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.exceptions.EmfRuntimeException;

/**
 * Helper class for reflection operations
 * 
 * @author BBonev
 */
public class ReflectionUtils {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionUtils.class);

	/**
	 * Gets the annotation from weld bean. <br>
	 * The methods tries to retrieve the annotation from the class of the given object first. If not
	 * found tries to fetch the getTargetClass method that is method from a Weld proxy class (@link
	 * org.jboss.interceptor.util.proxy.TargetInstanceProxy#getTargetClass()). If such method is
	 * found the annotation is locked on the object returned from the method call.
	 * 
	 * @param <A>
	 *            the annotation type
	 * @param target
	 *            the target object to fetch the annotation from
	 * @param annotation
	 *            the annotation class
	 * @return the found annotation or <code>null</code> if the annotation is not present on the
	 *         given class or the given object is not a weld proxy or not present on the proxy
	 */
	public static <A extends Annotation> A getAnnotationFromWeldBean(Object target,
			Class<A> annotation) {
		A extension = target.getClass().getAnnotation(annotation);
		if (extension == null) {
			boolean accessible = true;
			Method declaredMethod = null;
			try {
				declaredMethod = target.getClass().getDeclaredMethod("getTargetClass",
						(Class<?>[]) null);
				accessible = declaredMethod.isAccessible();
				if (!accessible) {
					declaredMethod.setAccessible(true);
				}
				Object invoke = declaredMethod.invoke(target, (Object[]) null);
				if (invoke instanceof Class) {
					Class<?> targetClass = (Class<?>) invoke;
					extension = targetClass.getAnnotation(annotation);
				}
			} catch (NoSuchMethodException e) {
				// probably not a weld proxy class
				LOGGER.warn("Error occured with reflection access", e);
			} catch (SecurityException e) {
				LOGGER.warn("Error occured with reflection access", e);
			} catch (IllegalAccessException e) {
				LOGGER.warn("Error occured with reflection access", e);
			} catch (IllegalArgumentException e) {
				LOGGER.warn("Error occured with reflection access", e);
			} catch (InvocationTargetException e) {
				LOGGER.warn("Error occured with reflection access", e);
			} finally {
				if ((declaredMethod != null) && !accessible) {
					declaredMethod.setAccessible(accessible);
				}
			}
		}
		return extension;
	}

	/**
	 * Gets the weld bean class. The methods checks the name of the given class and if the name
	 * contains '$Proxy$_$$_Weld' is considered as a Weld proxy class so the actual class is invoked
	 * via proxy method.
	 * 
	 * @param object
	 *            the object
	 * @return the weld bean class
	 */
	public static Class<?> getWeldBeanClass(Object object) {
		Class<? extends Object> clazz = object.getClass();
		if (clazz.toString().contains("$Proxy$_$$_Weld")) {
			boolean accessible = true;
			Method declaredMethod = null;
			try {
				declaredMethod = object.getClass().getDeclaredMethod("getTargetClass",
						(Class<?>[]) null);
				accessible = declaredMethod.isAccessible();
				if (!accessible) {
					declaredMethod.setAccessible(true);
				}
				Object invoke = declaredMethod.invoke(object, (Object[]) null);
				if (invoke instanceof Class) {
					return (Class<?>) invoke;
				}
			} catch (NoSuchMethodException e) {
				// probably not a weld proxy class
				LOGGER.warn("Error occured with reflection access", e);
			} catch (SecurityException e) {
				LOGGER.warn("Error occured with reflection access", e);
			} catch (IllegalAccessException e) {
				LOGGER.warn("Error occured with reflection access", e);
			} catch (IllegalArgumentException e) {
				LOGGER.warn("Error occured with reflection access", e);
			} catch (InvocationTargetException e) {
				LOGGER.warn("Error occured with reflection access", e);
			} finally {
				if ((declaredMethod != null) && !accessible) {
					declaredMethod.setAccessible(accessible);
				}
			}
		}
		return clazz;
	}

	/**
	 * New instance of the specified class.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param sourceClass
	 *            the source class
	 * @return the t
	 */
	public static <T> T newInstance(Class<T> sourceClass) {
		try {
			return sourceClass.newInstance();
		} catch (InstantiationException e) {
			throw new EmfRuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new EmfRuntimeException(e);
		}
	}

	/**
	 * Gets the field value.
	 * 
	 * @param field
	 *            the field
	 * @param referenceInstance
	 *            the reference instance
	 * @return the field value
	 */
	public static Object getFieldValue(Field field, Object referenceInstance) {
		try {
			return field.get(referenceInstance);
		} catch (IllegalArgumentException e) {
			throw new EmfRuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new EmfRuntimeException(e);
		}
	}

}
