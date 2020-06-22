package com.sirma.sep.content.idoc.handler;

import static java.util.stream.Collectors.toConcurrentMap;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import com.sirma.sep.content.idoc.ContentNode;

/**
 * Handler that performs search operations for the supported nodes
 *
 * @param <C>
 *            the type of the node
 * @author BBonev
 */
public interface SearchContentNodeHandler<C extends ContentNode> extends ContentNodeHandler<C> {

	/**
	 * Handle the given nodes using a {@link SearchContentNodeHandler} implementations. <br>
	 * This is short for calling the code:
	 *
	 * <pre>
	 * Stream<HandlerResult> results = Handlers.getInstance().handleWithResult(nodes, SearchContentNodeHandler.class);
	 * return results.filter(result -> result.getResult().isPresent() && result.getNode().getId() != null).collect(
	 * 		toConcurrentMap(result -> result.getNode().getId(), Function.identity()));
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
	 * @return a mapping of the content ids and their results
	 */
	@SuppressWarnings("unchecked")
	static Map<String, HandlerResult> handle(Stream<? extends ContentNode> nodes, HandlerContext context) {
		Stream<HandlerResult> results = Handlers.getInstance(context).handleWithResult(nodes,
				SearchContentNodeHandler.class);
		return results.filter(result -> result.getResult().isPresent() && result.getNode().getId() != null).collect(
				toConcurrentMap(result -> result.getNode().getId(), Function.identity()));
	}
}
