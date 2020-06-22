package com.sirma.sep.content.idoc.handler;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.handler.ContentNodeHandler.HandlerContext;
import com.sirma.sep.content.idoc.handler.ContentNodeHandler.HandlerResult;

/**
 * {@link ContentNodeHandler}s manager. Provides means to register handlers and execute {@link ContentNode}s over the
 * them.
 *
 * @author BBonev
 */
public class Handlers {

	private static final Handlers INSTANCE = new Handlers(new CopyOnWriteArrayList<>(), new HandlerContext());

	private final Collection<ContentNodeHandler<? super ContentNode>> nodeHandlers;
	private final HandlerContext context;

	private Handlers(Collection<ContentNodeHandler<? super ContentNode>> nodeHandlers, HandlerContext context) {
		this.nodeHandlers = nodeHandlers;
		this.context = context == null ? new HandlerContext() : context;
	}

	/**
	 * Returns a single instance that does not operate any {@link HandlerContext}
	 *
	 * @return a singleton instance
	 */
	public static Handlers getInstance() {
		return INSTANCE;
	}

	/**
	 * Returns new instance that uses the given {@link HandlerContext} when running the handlers. The returned instance
	 * will have all registered handlers.
	 *
	 * @param context
	 *            the context to use
	 * @return new {@link Handlers} instance that operate using the given context
	 */
	public static Handlers getInstance(HandlerContext context) {
		return new Handlers(INSTANCE.nodeHandlers, context);
	}

	/**
	 * Run handlers of the given type over the given nodes<br>
	 * If the processing should happen in parallel convert the stream to parallel before calling this method.<br>
	 * Note also that the method will consume the given stream.
	 *
	 * @param <C>
	 *            content node types
	 * @param <H>
	 *            the type of handlers to call
	 * @param nodes
	 *            the stream of content nodes to process
	 * @param filter
	 *            {@link ContentNodeHandler} class that acts as filter. Only handlers of the given types will be called
	 */
	public <C extends ContentNode, H extends ContentNodeHandler<? extends ContentNode>> void handle(Stream<C> nodes,
			Class<H> filter) {
		Objects.requireNonNull(nodes, "Stream of nodes is required");
		Objects.requireNonNull(filter, "ContentNodeHandler class is required");

		List<ContentNodeHandler<? super ContentNode>> contentHandlers = getFilteredHandlers(filter);

		nodes.forEach(node -> contentHandlers
				.stream()
					.filter(handler -> handler.accept(node))
					.forEach(handler -> handler.handle(node, context)));
	}

	private <H extends ContentNodeHandler<? extends ContentNode>> List<ContentNodeHandler<? super ContentNode>> getFilteredHandlers(
			Class<H> filter) {
		return nodeHandlers.stream().filter(filter::isInstance).collect(Collectors.toList());
	}

	/**
	 * Run handlers of the given type over the given nodes<br>
	 * If the processing should happen in parallel convert the stream to parallel before calling this method.<br>
	 * Note also that the method will consume the given stream.
	 *
	 * @param <C>
	 *            content node types
	 * @param <H>
	 *            the type fo handlers to call
	 * @param nodes
	 *            the stream of content nodes to process
	 * @param filter
	 *            {@link ContentNodeHandler} class that acts as filter. Only handlers of the given types will be called
	 * @return stream of handler results
	 */
	public <C extends ContentNode, H extends ContentNodeHandler<? extends ContentNode>> Stream<HandlerResult> handleWithResult(
			Stream<C> nodes, Class<H> filter) {
		Objects.requireNonNull(nodes, "Stream of nodes is required");
		Objects.requireNonNull(filter, "ContentNodeHandler class is required");

		List<ContentNodeHandler<? super ContentNode>> contentHandlers = getFilteredHandlers(filter);

		return nodes.map(callHanldersWithResult(contentHandlers, context));
	}

	private static Function<ContentNode, HandlerResult> callHanldersWithResult(
			List<ContentNodeHandler<? super ContentNode>> contentHandlers, HandlerContext ctx) {
		return node -> contentHandlers
				.stream()
					.filter(handler -> handler.accept(node))
					.map(handler -> handler.handle(node, ctx))
					.findFirst()
					.orElseGet(() -> new HandlerResult(node));
	}

	/**
	 * Register content node handler instance
	 *
	 * @param handler
	 *            to register
	 */
	public void registerHandler(ContentNodeHandler<? super ContentNode> handler) {
		nodeHandlers.add(handler);
	}

	/**
	 * Unregister content node handler instance
	 *
	 * @param handler
	 *            to unregister
	 */
	public void unregisterHandler(ContentNodeHandler<? super ContentNode> handler) {
		nodeHandlers.remove(handler);
	}

}
