package com.sirma.itt.seip.runtime;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.enterprise.inject.spi.BeanManager;

import com.sirma.itt.seip.runtime.boot.StartupException;
import com.sirma.itt.seip.util.CDI;

/**
 * Component instance for method components.
 * 
 * @author BBonev
 * @author nvelkov
 */
public class MethodComponentInstance extends AbstractComponentInstance {

	private Method method;

	/**
	 * Init the method component instance.
	 * 
	 * @param actualClass
	 *            the actual class
	 * @param method
	 *            the annotated method
	 * @param beanManager
	 *            the bean manager
	 */
	public MethodComponentInstance(Class<?> actualClass, Method method, BeanManager beanManager) {
		super(actualClass, beanManager);
		this.method = method;
	}

	@Override
	public void execute() throws StartupException {
		try {
			CDI.invokeMethod(getActualClass(), method, getBeanManager());
		} catch (InvocationTargetException e) { // NOSONAR
			throw new StartupException(e.getTargetException());
		} catch (Exception e) {
			throw new StartupException(e);
		}

	}

	@Override
	public AnnotatedElement getAnnotated() {
		return method;
	}

}