/*
 *
 */
package com.sirma.itt.seip.tasks;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.enterprise.inject.spi.BeanManager;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.ConfigurationException;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.build.ConfigurationProvider;
import com.sirma.itt.seip.configuration.convert.ConverterException;
import com.sirma.itt.seip.configuration.convert.TypeConverterContext;
import com.sirma.itt.seip.util.CDI;

/**
 * Wrapper object to instantiate and call scheduled methods.
 *
 * @author BBonev
 */
class SchedulerMethodCaller {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final Class<?> definedIn;
	private final Method method;
	private final BeanManager beanManager;
	private final String identifier;
	private final Schedule annotation;
	private final int contextIndex;

	private SchedulerService schedulerService;
	private ConfigurationProvider configurationProvider;

	/**
	 * Instantiates a new scheduler method caller.
	 *
	 * @param definedIn
	 *            the defined in
	 * @param method
	 *            the method
	 * @param beanManager
	 *            the bean manager
	 */
	SchedulerMethodCaller(Class<?> definedIn, Method method, BeanManager beanManager) {
		this.definedIn = definedIn;
		this.method = method;
		this.beanManager = beanManager;
		annotation = method.getAnnotation(Schedule.class);

		validateConfigurationAnnoations();

		if ("".equals(annotation.identifier())) {
			identifier = definedIn.getSimpleName() + "." + method.getName();
		} else {
			identifier = annotation.identifier();
		}
		contextIndex = validateParameters(method.getParameterTypes(), SchedulerContext.class);
	}

	private void validateConfigurationAnnoations() {
		if (annotation == null || "".equals(annotation.expression()) && "".equals(annotation.expressionConfig())
				&& !method.isAnnotationPresent(ConfigurationPropertyDefinition.class)) {
			throw new IllegalArgumentException(
					"Schedule expression or expression configuration is required! Non of them are found.");
		}
	}

	private static int validateParameters(Class<?>[] parameterTypes, Class<?> searchFor) {
		if (parameterTypes == null || parameterTypes.length == 0) {
			return -1;
		}

		return getParameterIndex(parameterTypes, searchFor);
	}

	private static int getParameterIndex(Class<?>[] parameterTypes, Class<?> searchFor) {
		for (int i = 0; i < parameterTypes.length; i++) {
			if (searchFor.isAssignableFrom(parameterTypes[i])) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Gets unique identifier for the scheduler instance.
	 *
	 * @return the identifier
	 */
	String getIdentifier() {
		return identifier;
	}

	/**
	 * Schedule the current instance for execution.
	 *
	 * @param scheduler
	 *            the scheduler service
	 * @param configProvider
	 *            the configuration provider
	 */
	void schedule(final SchedulerService scheduler, final ConfigurationProvider configProvider) {
		schedulerService = scheduler;
		configurationProvider = configProvider;

		String expression = getExpressionFromConfiguration(configProvider);

		if ("".equals(expression)) {
			String expressionConfig = annotation.expressionConfig();
			LOGGER.warn("Not going to schedule \n{}\n\tbecause no expression is defined or found for configuration {}",
					this, expressionConfig);
			return;
		}

		SchedulerConfiguration configuration = scheduler.buildEmptyConfiguration(SchedulerEntryType.CRON);
		configuration.setIdentifier(getIdentifier());
		configuration.setCronExpression(expression);
		configuration.setIncrementalDelay(annotation.incrementalDelay());
		configuration.setMaxRetryCount(annotation.maxRetries());
		configuration.setPersistent(true);
		configuration.setRetryDelay(annotation.retryDelay());
		configuration.setTransactionMode(annotation.transactionMode());
		configuration.setContinueOnError(!annotation.stopOnError());
		configuration.setRunAs(annotation.system() ? RunAs.SYSTEM : RunAs.ALL_TENANTS);

		SchedulerContext context = new SchedulerContext(2);
		context.put(AutomaticSchedulerAction.EXECUTABLE, getIdentifier());

		scheduler.schedule(AutomaticSchedulerAction.NAME, configuration, context);
	}

	private String getExpressionFromConfiguration(final ConfigurationProvider configProvider) {
		String expression = annotation.expression();
		String configName = annotation.expressionConfig();
		if (StringUtils.isBlank(configName)) {
			ConfigurationPropertyDefinition propertyDefinition = method
					.getAnnotation(ConfigurationPropertyDefinition.class);
			if (propertyDefinition != null) {
				configName = propertyDefinition.name();
			}
		}

		if (StringUtils.isNotBlank(configName)) {
			ConfigurationProperty<String> property = configProvider.getProperty(configName);
			if (!property.getDefinition().isSystemConfiguration()) {
				throw new ConfigurationException(String.format(
						"The configuration `%s` that controls the automatic schedule `%s` should be defined as system!",
						property.getName(), getIdentifier()));
			}
			addChangeListener(property);

			if (property.isSet()) {
				expression = property.get();
			}
		}

		return expression;
	}

	/**
	 * Adds the change listener
	 *
	 * @param property
	 *            the property
	 */
	private void addChangeListener(ConfigurationProperty<String> property) {
		property.addConfigurationChangeListener(this::onConfigurationChange);
	}

	/**
	 * Callback for on configuration change.
	 *
	 * @param property
	 *            the property
	 */
	private void onConfigurationChange(ConfigurationProperty<String> property) {
		if (schedulerService != null && configurationProvider != null) {
			LOGGER.info("Detected change in property {}. Will update scheduler associated with it.", property.getName());
			schedule(schedulerService, configurationProvider);
		}
	}

	/**
	 * Invoke the scheduled method passing the given context if needed.
	 *
	 * @param context
	 *            the context
	 */
	void invoke(SchedulerContext context) {
		Object bean = instantiateBean(definedIn, method, beanManager);
		if (bean == null) {
			LOGGER.warn("Could not instantiate {}", definedIn);
			return;
		}
		LOGGER.trace("Invoking {} on {}", this, bean);
		callMethod(bean, method, beanManager, context, contextIndex);
	}

	/**
	 * Call converter.
	 *
	 * @param bean
	 *            the bean
	 * @param methodToCall
	 *            the method to call
	 * @param manager
	 *            the bean manager
	 * @param predefinedParameter
	 *            the predefined parameter
	 * @param predefinedParameterIndex
	 *            the predefined parameter index
	 */
	private static void callMethod(Object bean, Method methodToCall, BeanManager manager, Object predefinedParameter,
			int predefinedParameterIndex) {
		try {
			methodToCall.setAccessible(true);
			methodToCall.invoke(bean,
					buildArgumentsList(methodToCall, manager, predefinedParameter, predefinedParameterIndex));
		} catch (Exception e) {
			throw new IllegalStateException("Failed invoking bean method " + methodToCall.getDeclaringClass().getName()
					+ "." + methodToCall.getName(), e);
		}
	}

	/**
	 * Builds the arguments list combining it with the given {@link TypeConverterContext}.
	 */
	private static Object[] buildArgumentsList(Method methodToBeCalled, BeanManager manager, Object predefinedParameter,
			int predefiedParameterIndex) {
		Class<?>[] parameterTypes = methodToBeCalled.getParameterTypes();
		if (parameterTypes.length == 1) {
			return new Object[] { predefinedParameter };
		}

		Object[] parameterValues = new Object[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			Object parameter;
			if (i == predefiedParameterIndex) {
				parameter = predefinedParameter;
			} else {
				Class<?> parameterType = parameterTypes[i];
				Annotation[] annotations = methodToBeCalled.getParameterAnnotations()[i];
				parameter = instantiateParameter(methodToBeCalled, manager, parameterType, annotations);
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
	 * @throws ConverterException
	 *             the converter exception
	 */
	private static Object instantiateParameter(Method methodToBeCalled, BeanManager manager, Class<?> parameterType,
			Annotation[] annotations) {
		Object bean = CDI.instantiateBean(parameterType, manager, annotations);
		if (bean == null) {
			throw new IllegalStateException(
					"Could not find parameter of type " + parameterType.getName() + " for method "
							+ methodToBeCalled.getDeclaringClass().getName() + "." + methodToBeCalled.getName());
		}
		return bean;
	}

	/**
	 * Instantiate the given class to that the given method could be executed.
	 *
	 * @param instantiate
	 *            the class instantiate
	 * @param methodToBeCalled
	 *            the method to be called
	 * @param manager
	 *            the bean manager to use to instantiate class if the method is non static.
	 * @return the instantiated object or the class itself if the method is static.
	 */
	private static Object instantiateBean(Class<?> instantiate, Method methodToBeCalled, BeanManager manager) {
		// for static methods return the class
		if (Modifier.isStatic(methodToBeCalled.getModifiers())) {
			return instantiate;
		}
		return CDI.instantiateBean(instantiate, manager, CDI.getQualifers(instantiate, manager));
	}

	@Override
	public String toString() {
		return method.getAnnotation(Schedule.class) + " " + definedIn.getName() + "." + method.getName();
	}

}
