package com.sirma.sep.content.idoc.handler;

import java.util.stream.Stream;

import com.sirma.sep.content.idoc.ContentNode;

/**
 * Handler that performs revert operation on supported {@link ContentNode}s. This operation could be done only for nodes
 * that are first processed by {@link VersionContentNodeHandler}s.
 *
 * @param <C>the
 *            type of the node
 * @author A. Kunchev
 */
public interface RevertContentNodeHandler<C extends ContentNode> extends ContentNodeHandler<C> {

	/**
	 * Handle the given nodes using a {@link RevertContentNodeHandler} implementations. <br>
	 * This is short for calling the code:
	 *
	 * <pre>
	 * Handlers.getInstance().handleWithResult(nodes, RevertContentNodeHandler.class);
	 * </pre>
	 *
	 * <br>
	 * If the processing should happen in parallel convert the stream to parallel before calling this method.<br>
	 * Note also that the method will consume the given stream.
	 *
	 * @param nodes
	 *            stream of nodes to process. Parallel stream could be passed as well
	 * @param context
	 *            {@link HandlerContext} used to store specific data required while the process is executed. Also could
	 *            be used to share data between the different handlers, if needed
	 */
	@SuppressWarnings("unchecked")
	static void handle(Stream<? extends ContentNode> nodes, HandlerContext context) {
		Handlers.getInstance(context).handle(nodes, RevertContentNodeHandler.class);
	}

}
