package com.sirma.itt.seip.runtime;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.AnnotatedElement;

import javax.enterprise.inject.spi.BeanManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupException;
import com.sirma.itt.seip.runtime.boot.StartupListener;
import com.sirma.itt.seip.util.CDI;

/**
 * Wrapper object for components defined by annotation on type
 *
 * @author BBonev
 */
public class StartupClassComponentInstance extends AbstractStartupComponentInstance {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/**
	 * Instantiates a new class component instance.
	 *
	 * @param annotation
	 *            the annotation
	 * @param actualClass
	 *            the actual class
	 * @param beanManager
	 *            the bean manager
	 */
	public StartupClassComponentInstance(Startup annotation, Class<?> actualClass, BeanManager beanManager) {
		super(annotation, actualClass, beanManager);
	}

	/**
	 * Start.
	 *
	 * @throws StartupException
	 *             the startup exception
	 */
	@Override
	public void execute() throws StartupException {
		Object object = CDI.instantiateBean(getActualClass(), getBeanManager(),
				CDI.getQualifers(getActualClass(), getBeanManager()));
		// force post construct invocation
		if (object != null) {
			object.toString();
		} else {
			LOGGER.warn("Could not instantiate bean {}", getActualClass().getName());
		}

		if (object instanceof StartupListener) {
			((StartupListener) object).onStartup();
		}
	}

	@Override
	public AnnotatedElement getAnnotated() {
		return getActualClass();
	}
}