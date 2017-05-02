package com.sirma.itt.seip.annotations.parser;

import javax.enterprise.inject.spi.BeanManager;

import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.util.CDI;

/**
 * Context provider instantiator.
 * 
 * @see {@link com.sirma.itt.seip.annotations.parser.ContextBuilderProvider}
 * @author tdossev
 */
public class CDIContextBuilderExtension {

	/**
	 * Register all context providers.
	 *
	 * @param beanManager
	 *            the bean manager
	 */
	@Startup(phase = StartupPhase.DEPLOYMENT)
	static void registerExtensions(BeanManager beanManager) {
		CDI.registerBeans(beanManager, ContextBuilderProvider.class, ContextBuilder.getInstance()::registerProvider);
	}

}
