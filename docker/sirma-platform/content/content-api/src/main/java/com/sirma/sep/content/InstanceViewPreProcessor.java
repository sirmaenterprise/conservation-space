/**
 *
 */
package com.sirma.sep.content;

import com.sirma.itt.seip.plugin.Plugin;
import com.sirma.sep.content.event.InstanceViewAddedEvent;
import com.sirma.sep.content.event.InstanceViewUpdatedEvent;

/**
 * Extension that can provide means to observe for changes in the instance view and also to modify/update it. The
 * extension will be called in order to prevent concurrent modifications of the content. <br>
 * If no changes are expected then regular CDI observer of the events {@link InstanceViewAddedEvent} or
 * {@link InstanceViewUpdatedEvent} could be used.
 *
 * @author BBonev
 */
public interface InstanceViewPreProcessor extends Plugin {

	String TARGET_NAME = "InstanceViewPreProcessor";

	/**
	 * Process the instance view content. Any changes to the view should be finished before the end of the method call.
	 * <p>
	 * Any exception thrown by extension implementation will result it content persist termination and transaction
	 * rollback.
	 *
	 * @param context
	 *            the context
	 * @see ViewPreProcessorContext
	 */
	void process(ViewPreProcessorContext context);
}
