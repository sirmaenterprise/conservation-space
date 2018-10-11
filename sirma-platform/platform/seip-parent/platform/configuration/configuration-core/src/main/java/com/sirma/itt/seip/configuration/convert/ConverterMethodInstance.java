package com.sirma.itt.seip.configuration.convert;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.commons.lang.StringUtils;

import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;

/**
 * Converter instance that calls a given converter method that matches the converter specification. The implementation
 * could call a static or non static methods on a CDI bean instantiated using the provided {@link BeanManager}
 *
 * @param <T>
 *            the produced type
 * @author BBonev
 */
public class ConverterMethodInstance<T> implements ConfigurationValueConverter<T> {

	private final Class<?> definedIn;
	private final Method converterMethod;
	private final Class<T> resultType;
	private final BeanManager beanManager;
	private int contextParameterIndex = 0;

	/**
	 * Instantiates a new converter instance.
	 *
	 * @param definedIn
	 *            The class where the converter method is defined
	 * @param converterMethod
	 *            the converter method to be called
	 * @param beanManager
	 *            the bean manager to use to instantiate the bean if method is non static
	 */
	@SuppressWarnings("unchecked")
	public ConverterMethodInstance(Class<?> definedIn, Method converterMethod, BeanManager beanManager) {
		this.definedIn = definedIn;
		this.converterMethod = converterMethod;
		this.beanManager = beanManager;
		resultType = (Class<T>) converterMethod.getReturnType();

		if (Void.TYPE.isAssignableFrom(resultType)) {
			throw new IllegalArgumentException("Converter method does not have a valid return type");
		}
		Class<?>[] parameterTypes = converterMethod.getParameterTypes();
		contextParameterIndex = validateParameters(parameterTypes);
	}

	/**
	 * Validate parameters. The list should contain at least one argument of type {@link TypeConverterContext}.
	 *
	 * @param parameterTypes
	 *            the parameter types
	 * @return the index of the found parameter
	 */
	private static int validateParameters(Class<?>[] parameterTypes) {
		if (parameterTypes == null || parameterTypes.length == 0) {
			throw new IllegalArgumentException("Converter method does not have a an argument");
		}

		int contextIndex = -1;
		for (int i = 0; i < parameterTypes.length; i++) {
			if (TypeConverterContext.class.isAssignableFrom(parameterTypes[i])) {
				contextIndex = i;
				break;
			}
		}
		if (contextIndex < 0) {
			throw new IllegalArgumentException("No argument found that can accept " + TypeConverterContext.class);
		}
		return contextIndex;
	}

	@Override
	public Class<T> getType() {
		return resultType;
	}

	@Override
	public String getName() {
		return StringUtils.trimToNull(converterMethod.getAnnotation(ConfigurationConverter.class).value());
	}

	@Override
	public T convert(TypeConverterContext converterContext) {
		// check if the passed argument is assignable to the method arguments
		if (!converterMethod.getParameterTypes()[contextParameterIndex].isInstance(converterContext)) {
			return null;
		}
		String name = getName();
		// check if there is a filter for the produced configurations and do not call it if there is
		// and they do not match
		if (name != null && !converterContext.getConfiguration().getName().equals(name)
				&& !nullSafeEquals(name, converterContext.getConfiguration().getConverter())) {
			return null;
		}

		Object bean = instantiateBean();
		if (bean == null) {
			return null;
		}
		return callConverter(bean, converterContext);
	}

	/**
	 * Call converter.
	 *
	 * @param bean
	 *            the bean
	 * @param converterContext
	 *            the converter context
	 * @return the converted value or <code>null</code>
	 */
	private T callConverter(Object bean, TypeConverterContext converterContext) {
		try {
			// most of the methods will be protected or package, so we need this to call them
			converterMethod.setAccessible(true);

			Object result = converterMethod.invoke(bean, buildArgumentsList(converterContext));
			if (getType().isInstance(result)) {
				return getType().cast(result);
			}
		} catch (InvocationTargetException e) { // NOSONAR
			throw new ConverterException("Failed invoking converter method " + getMethodInfo() + " for configuration: "
					+ converterContext.getConfiguration().getName(), e.getTargetException());
		} catch (Exception e) {
			throw new ConverterException("Failed invoking converter method " + getMethodInfo() + " for configuration: "
					+ converterContext.getConfiguration().getName(), e);
		}
		return null;
	}

	/**
	 * Builds the arguments list combining it with the given {@link TypeConverterContext}.
	 *
	 * @param converterContext
	 *            the converter context
	 * @return the object[]
	 */
	private Object[] buildArgumentsList(TypeConverterContext converterContext) {
		Class<?>[] parameterTypes = converterMethod.getParameterTypes();
		if (parameterTypes.length == 1) {
			return new Object[] { converterContext };
		}
		Object[] parameterValues = new Object[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			Object parameter = null;
			if (i == contextParameterIndex) {
				parameter = converterContext;
			} else {
				Class<?> parameterType = parameterTypes[i];
				Annotation[] annotations = converterMethod.getParameterAnnotations()[i];
				parameter = instantiateParameter(parameterType, annotations);
			}
			parameterValues[i] = parameter;
		}
		return parameterValues;
	}

	/**
	 * Instantiate parameter of the given type and list of annotations.
	 *
	 * @param parameterType
	 *            the parameter type
	 * @param annotations
	 *            the annotations
	 * @return the object
	 */
	private Object instantiateParameter(Class<?> parameterType, Annotation[] annotations) {
		Set<Bean<?>> beans = beanManager.getBeans(parameterType, annotations);
		if (beans.isEmpty()) {
			throw new ConverterException("Could not find parameter of type " + parameterType.getName()
					+ " for converter method " + getMethodInfo());
		}
		Bean<?> bean = beans.iterator().next();
		CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);
		return beanManager.getReference(bean, parameterType, creationalContext);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object instantiateBean() {
		// for static methods return the class
		if (Modifier.isStatic(converterMethod.getModifiers())) {
			return definedIn;
		}
		// otherwise try to instantiate the bean
		Set<Bean<?>> beans = beanManager.getBeans(definedIn);
		Iterator<Bean<?>> it = null;
		if (beans == null || (it = beans.iterator()) == null || !it.hasNext()) {
			return null;
		}
		Bean provider = it.next();
		CreationalContext cc = beanManager.createCreationalContext(provider);
		Object reference = beanManager.getReference(provider, definedIn, cc);
		// forces calling post construct method if any
		if (reference != null) {
			reference.toString();
		}
		return reference;
	}

	@Override
	public String toString() {
		return new StringBuilder(256)
				.append("ConverterMethod [name=")
					.append(getName())
					.append(", method=")
					.append(getMethodInfo())
					.append("]")
					.toString();
	}

	private StringBuilder getMethodInfo() {
		return new StringBuilder(100)
				.append(resultType.getSimpleName())
					.append(' ')
					.append(definedIn.getName())
					.append(".")
					.append(converterMethod.getName())
					.append(Arrays
							.asList(converterMethod.getParameterTypes())
								.stream()
								.map(Class::getSimpleName)
								.collect(Collectors.joining(", ", "(", ")")));
	}

}
