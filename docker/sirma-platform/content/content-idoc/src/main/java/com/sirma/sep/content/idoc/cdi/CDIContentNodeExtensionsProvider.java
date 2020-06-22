package com.sirma.sep.content.idoc.cdi;

import java.lang.reflect.Type;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.TypeLiteral;

import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.util.CDI;
import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.ContentNodeBuilder;
import com.sirma.sep.content.idoc.ContentNodeFactory;
import com.sirma.sep.content.idoc.handler.ContentNodeHandler;
import com.sirma.sep.content.idoc.handler.Handlers;

/**
 * Extension to load all {@link ContentNodeBuilder} and {@link ContentNodeHandler} implementations VIA CDI and register
 * them to the {@link ContentNodeFactory} and {@link Handlers}.
 *
 * @author BBonev
 */
public class CDIContentNodeExtensionsProvider {

	private CDIContentNodeExtensionsProvider() {
		// default constructor
	}

	/**
	 * Uses {@link TypeLiteral}, because {@link ContentNodeHandler} is a parameterized type and {@link BeanManager}
	 * can't discover the extensions using only class, the parameters also should be described.
	 */
	private static final Type CONTENT_NODE_HANDLER_TYPE = new TypeLiteral<ContentNodeHandler<? extends ContentNode>>() {
		private static final long serialVersionUID = -6014182590306371051L;
	}.getType();

	/**
	 * Register all content node builders and handlers.
	 *
	 * @param beanManager
	 *            the bean manager
	 */
	@Startup(phase = StartupPhase.DEPLOYMENT)
	static void registerExtensions(BeanManager beanManager) {
		CDI.registerBeans(beanManager, ContentNodeBuilder.class, ContentNodeFactory.getInstance()::registerBuilder);
		CDI.registerBeans(beanManager, CONTENT_NODE_HANDLER_TYPE, Handlers.getInstance()::registerHandler);
	}

}
