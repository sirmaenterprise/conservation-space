package com.sirma.itt.seip.configuration.convert;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.apache.commons.lang.StringUtils;

import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;

/**
 * Converter instance that can handle a converter instance defined on a class level. Note that he returned class from
 * the method {@link #getType()} will not return the type that is returned by the actual converter instance but will be
 * inferred from the converter method. This is because the instance should be instantiated during deployment phase and
 * that is not possible.
 *
 * @author BBonev
 * @param <T>
 *            the converter return type
 */
public class ConverterClassInstance<T> implements ConfigurationValueConverter<T> {

	private Class<?> converterClass;
	private BeanManager beanManager;
	private final Class<T> resultType;
	private final Method converterMethod;

	/**
	 * Instantiates a new converter class instance.
	 *
	 * @param converterClass
	 *            the converter class
	 * @param beanManager
	 *            the bean manager
	 */
	@SuppressWarnings({ "unchecked", "pmd:ConstructorCallsOverridableMethod" })
	public ConverterClassInstance(Class<?> converterClass, BeanManager beanManager) {
		this.converterClass = converterClass;
		this.beanManager = beanManager;
		if (!ConfigurationValueConverter.class.isAssignableFrom(converterClass)
				|| !converterClass.isAnnotationPresent(ConfigurationConverter.class)) {
			throw new IllegalArgumentException("Cannot create converter instance. The class should implement "
					+ ConfigurationValueConverter.class.getName() + " and annoation "
					+ ConfigurationConverter.class.getName());
		}
		try {
			converterMethod = converterClass.getMethod("convert", TypeConverterContext.class);
		} catch (NoSuchMethodException | SecurityException e) {
			// this should not happen as the method is defined in the interface
			throw new IllegalArgumentException(e);
		}
		resultType = (Class<T>) converterMethod.getReturnType();
	}

	@Override
	public Class<T> getType() {
		return resultType;
	}

	@Override
	public T convert(TypeConverterContext converterContext) {
		// check if the passed argument is assignable to the method arguments
		if (!converterMethod.getParameterTypes()[0].isInstance(converterContext)) {
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

	@SuppressWarnings("unchecked")
	private T callConverter(Object bean, TypeConverterContext converterContext) {
		if (bean instanceof ConfigurationValueConverter) {
			return ((ConfigurationValueConverter<T>) bean).convert(converterContext);
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object instantiateBean() {
		// otherwise try to instantiate the bean
		Set<Bean<?>> beans = beanManager.getBeans(converterClass);
		Iterator<Bean<?>> it = null;
		if (beans == null || (it = beans.iterator()) == null || !it.hasNext()) {
			return null;
		}
		Bean provider = it.next();
		CreationalContext cc = beanManager.createCreationalContext(provider);
		return provider.create(cc);
	}

	@Override
	public String getName() {
		return StringUtils.trimToNull(converterClass.getAnnotation(ConfigurationConverter.class).value());
	}

}
