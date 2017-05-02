package com.sirma.itt.seip.runtime;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

import javax.enterprise.inject.spi.BeanManager;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupException;
import com.sirma.itt.seip.util.CDI;

/**
 * Wrapper component instance components annotated over methods.
 *
 * @author BBonev
 */
public class MethodComponentInstance extends AbstractComponentInstance {

	/** The method. */
	private Method method;

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
	public MethodComponentInstance(Startup annotation, Class<?> actualClass, Method method, BeanManager beanManager) {
		super(annotation, actualClass, beanManager);
		this.method = method;
	}

	@Override
	public String getName() {
		String name = getAnnotation().name();
		if (StringUtils.isNullOrEmpty(name)) {
			name = getActualClass().getSimpleName() + "." + method.getName();
		}
		return name;
	}

	@Override
	public void start() throws StartupException {
		try {
			CDI.invokeMethod(getActualClass(), method, getBeanManager());
		} catch (EmfRuntimeException e) {
			throw new StartupException(e);
		}
	}

	@Override
	public AnnotatedElement getAnnotated() {
		return method;
	}
}
