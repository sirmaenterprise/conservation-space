package com.sirma.sep.content.idoc.handler;

import java.util.stream.Stream;

import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.WidgetConfiguration;

/**
 * Handler that performs version processing for the supported nodes.
 * <p>
 * Note that before this handlers are executed, search results should be stored in {@link WidgetConfiguration} via
 * {@link WidgetConfiguration#setSearchResults(Object)}. This results are retrieved by executing
 * {@link SearchContentNodeHandler}s. If the results are not set this handlers most likely will do nothing.
 *
 * @param <C>
 *            the type of the node
 * @author A. Kunchev
 */
public interface VersionContentNodeHandler<C extends ContentNode> extends ContentNodeHandler<C> {

	/**
	 * Handle the given nodes using a {@link VersionContentNodeHandler} implementations. <br>
	 * This is short for calling the code:
	 *
	 * <pre>
	 * Handlers.getInstance().handleWithResult(nodes, VersionContentNodeHandler.class);
	 * </pre>
	 *
	 * <br>
	 * If the processing should happen in parallel convert the stream to parallel before calling this method.<br>
	 * Note also that the method will consume the given stream.
	 *
	 * @param nodes
	 *            to process
	 * @param context
	 *            the handler context
	 */
	@SuppressWarnings("unchecked")
	static void handle(Stream<? extends ContentNode> nodes, HandlerContext context) {
		Handlers.getInstance(context).handle(nodes, VersionContentNodeHandler.class);
	}

}
