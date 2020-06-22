package com.sirma.itt.seip.util;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for reflection operations
 *
 * @author BBonev
 */
public class ReflectionUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String ERROR_OCCURED_WITH_REFLECTION_ACCESS = "Error occured with reflection access";

	/**
	 * Instantiates a new reflection utils.
	 */
	private ReflectionUtils() {
		// utilicy class
	}

	/**
	 * Gets the annotation from weld bean. <br>
	 * The methods tries to retrieve the annotation from the class of the given object first. If not found tries to
	 * fetch the getTargetClass method that is method from a Weld proxy class (@link
	 * org.jboss.interceptor.util.proxy.TargetInstanceProxy#getTargetClass()). If such method is found the annotation is
	 * locked on the object returned from the method call.
	 *
	 * @param <A>
	 *            the annotation type
	 * @param target
	 *            the target object to fetch the annotation from
	 * @param annotation
	 *            the annotation class
	 * @return the found annotation or <code>null</code> if the annotation is not present on the given class or the
	 *         given object is not a weld proxy or not present on the proxy
	 */
	public static <A extends Annotation> A getAnnotationFromWeldBean(Object target, Class<A> annotation) {
		A extension = target.getClass().getAnnotation(annotation);
		if (extension == null) {
			Method declaredMethod = null;
			try {
				declaredMethod = target.getClass().getDeclaredMethod("getTargetClass", (Class<?>[]) null);
				declaredMethod.setAccessible(true);
				Object invoke = declaredMethod.invoke(target, (Object[]) null);
				if (invoke instanceof Class) {
					Class<?> targetClass = (Class<?>) invoke;
					extension = targetClass.getAnnotation(annotation);
				}
			} catch (Exception e) {
				// probably not a weld proxy class
				LOGGER.warn(ERROR_OCCURED_WITH_REFLECTION_ACCESS, e);
			}
		}
		return extension;
	}

	/**
	 * Gets the weld bean class. The methods checks the name of the given class and if the name contains
	 * '$Proxy$_$$_Weld' is considered as a Weld proxy class so the actual class is invoked via proxy method.
	 *
	 * @param object
	 *            the object
	 * @return the weld bean class
	 */
	public static Class<?> getWeldBeanClass(Object object) {
		Class<? extends Object> clazz = object.getClass();
		if (clazz.toString().contains("$Proxy$_$$_Weld")) {
			Object result = invokeNoArgsMethod(object, "getTargetClass");
			if (result instanceof Class) {
				return (Class<?>) result;
			}
		}
		return clazz;
	}

	/**
	 * Invoke no args method from the given instance.
	 *
	 * @param object
	 *            the object
	 * @param methodName
	 *            the method name
	 * @return the result from invocation or null
	 */
	public static Object invokeNoArgsMethod(Object object, String methodName) {
		Method declaredMethod = null;
		try {
			declaredMethod = object.getClass().getDeclaredMethod(methodName, (Class<?>[]) null);
			declaredMethod.setAccessible(true);
			return declaredMethod.invoke(object, (Object[]) null);
		} catch (Exception e) {
			// probably not a weld proxy class
			throw new IllegalStateException("Failed to invoke method: " + methodName, e);
		}
	}

	/**
	 * New instance of the specified class.
	 *
	 * @param <T>
	 *            the generic type
	 * @param sourceClass
	 *            the source class
	 * @return the created instance using the default constructor
	 */
	public static <T> T newInstance(Class<T> sourceClass) {
		try {
			return sourceClass.newInstance();
		} catch (InstantiationException e) {
			throw new IllegalStateException(e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Gets the value of a field using reflection.
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
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Gets the value of a field using reflection.
	 *
	 * @param object
	 *            {@link Object}, the object which field should be set
	 * @param fieldName
	 *            {@link String}, name of the field to be set
	 * @return the field value
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getFieldValue(Object object, String fieldName) {
		try {
			Field field = getClassField(object.getClass(), fieldName);
			field.setAccessible(true);
			return (T) field.get(object);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Set field value in the specified object.
	 *
	 * @param object
	 *            {@link Object}, the object which field should be set
	 * @param fieldName
	 *            {@link String}, name of the field to be set
	 * @param value
	 *            {@link Object}, the new value
	 */
	public static void setFieldValue(Object object, String fieldName, Object value) {
		try {
			Field field = getClassField(object.getClass(), fieldName);
			boolean accessability = field.isAccessible();
			field.setAccessible(true);
			field.set(object, value);
			field.setAccessible(accessability);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Find specific field in the whole class hierarchy. The field is searched beginning from the
	 * <code>objectClass</code> until reaching {@link Object} . The first found field is returned.
	 *
	 * @param clazz
	 *            {@link Class}, class of the object which will be searched for the field
	 * @param fieldName
	 *            {@link String}, name of the field
	 * @return {@link Field}, founded field
	 * @throws NoSuchFieldException
	 *             thrown if there is no such field in the class hierarchy
	 */
	public static Field getClassField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
		Class<?> objectClass = clazz;
		if (objectClass == null) {
			throw new IllegalArgumentException("Object class must not be null");
		}
		Field field = null;
		do {
			try {
				field = objectClass.getDeclaredField(fieldName);
			} catch (NoSuchFieldException e) {
				objectClass = objectClass.getSuperclass();
				if (objectClass == null) {
					throw e;
				}
			}
		} while (field == null);
		return field;
	}

	/**
	 * Extract the type argument for a parameterized type.
	 *
	 * @param generic
	 *            Parameterized type.
	 * @return {@code null} if the provided type is not an instance of {@link ParameterizedType}, otherwise returns the
	 *         first element of {@link ParameterizedType#getActualTypeArguments()}.
	 */
	public static Class<?> getTypeArgument(Type generic) {
		if (!(generic instanceof ParameterizedType)) {
			return null;
		}

		ParameterizedType type = (ParameterizedType) generic;
		Type[] actual = type.getActualTypeArguments();
		if (actual == null || actual.length == 0) {
			return null;
		}
		return (Class<?>) actual[0];
	}

	/**
	 * Checks if the first type argument is assignable from the provided type. This method uses
	 * {@link ReflectionUtils#getTypeArgument(Type)} to extract the first type argument.
	 *
	 * @param generic
	 *            Type argument.
	 * @param target
	 *            Target type to check against.
	 * @return {@code true} if the provided type is assignable from the target class.
	 */
	public static boolean isTypeArgument(Type generic, Class<?> target) {
		Class<?> arg = ReflectionUtils.getTypeArgument(generic);
		if (arg == null) {
			return false;
		}
		return target.isAssignableFrom(arg);
	}
}
