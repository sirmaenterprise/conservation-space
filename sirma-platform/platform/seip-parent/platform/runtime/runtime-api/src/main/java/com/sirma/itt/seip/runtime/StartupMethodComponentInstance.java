package com.sirma.itt.seip.runtime;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

import javax.enterprise.inject.spi.BeanManager;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.runtime.boot.Startup;

/**
 * Wrapper component instance components annotated over methods.
 *
 * @author BBonev
 */
public class StartupMethodComponentInstance extends AbstractStartupComponentInstance {

	private Method method;

	private MethodComponentInstance instance;

	/**
	 * Instantiates a new method component instance.
	 *
	 * @param annotation
	 *            the annotation
	 * @param actualClass
	 *            the actual class
	 * @param method
	 *            the method
	 * @param beanManager
	 *            the bean manager
	 */
	public StartupMethodComponentInstance(Startup annotation, Class<?> actualClass, Method method,
			BeanManager beanManager) {
		super(annotation, actualClass, beanManager);
		this.method = method;
		this.instance = new MethodComponentInstance(actualClass, method, beanManager);
	}

	@Override
	public String getName() {
		String name = getAnnotation().name();
		if (StringUtils.isBlank(name)) {
			name = getActualClass().getSimpleName() + "." + method.getName();
		}
		return name;
	}

	@Override
	public void execute() {
		instance.execute();
	}

	@Override
	public AnnotatedElement getAnnotated() {
		return method;
	}
}
