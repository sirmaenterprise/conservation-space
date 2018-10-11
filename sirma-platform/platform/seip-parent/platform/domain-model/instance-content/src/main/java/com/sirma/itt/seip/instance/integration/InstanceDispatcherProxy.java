/**
 *
 */
package com.sirma.itt.seip.instance.integration;

import java.io.Serializable;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.sep.content.Content;

/**
 * Wrapping {@link InstanceDispatcher} implementation that processes all plugin implementations of
 * {@link InstanceDispatcher}s and calls them to find the one that returns non <code>null</code> system id.
 *
 * @author BBonev
 */
@ApplicationScoped
public class InstanceDispatcherProxy implements InstanceDispatcher {

	@Inject
	@ExtensionPoint(InstanceDispatcher.TARGET_NAME)
	private Iterable<InstanceDispatcher> dispatchers;

	@Override
	public String getContentManagementSystem(Serializable instance, Content content) {
		return callDispatchers(dispatcher -> dispatcher.getContentManagementSystem(instance, content));
	}

	@Override
	public String getViewManagementSystem(Serializable instance, Content content) {
		return callDispatchers(dispatcher -> dispatcher.getViewManagementSystem(instance, content));
	}

	@Override
	public String getDataSourceSystem(Serializable instance) {
		return callDispatchers(dispatcher -> dispatcher.getDataSourceSystem(instance));
	}

	private String callDispatchers(Function<InstanceDispatcher, String> toDispatch) {
		for (InstanceDispatcher dispatcher : dispatchers) {
			String result = toDispatch.apply(dispatcher);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

}
