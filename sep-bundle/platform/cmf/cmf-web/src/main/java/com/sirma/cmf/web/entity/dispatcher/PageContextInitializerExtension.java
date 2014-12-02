package com.sirma.cmf.web.entity.dispatcher;

import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.SupportablePlugin;

/**
 * Extensions for page context initializer plugins.
 * 
 * @param <I>
 *            the generic type
 * @author svelikov
 */
public interface PageContextInitializerExtension<I extends Instance> extends SupportablePlugin {

	/** The target name. */
	String TARGET_NAME = "PageContextInitializer";

	/**
	 * Inits the page context for provided instance.
	 * 
	 * @param instance
	 *            the instance
	 * @return navigation string
	 */
	String initContextFor(I instance);

}
