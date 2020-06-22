package com.sirma.sep.content.idoc.handler;

import java.util.Optional;

import com.sirma.itt.seip.context.Context;
import com.sirma.sep.content.idoc.ContentNode;

/**
 * Defines a generic {@link ContentNode} handler. This is base interface and does not define any specific handler
 * purpose. A handler should not implement this interface directly but one of it's sub interfaces. The suggested use
 * should be:
 *
 * <pre>
 * <code>
 * Idoc idoc = Idoc.parse(..);
 * // pass all widgets via the search handlers to load all their data
 * Handlers.handle(idoc.getWidgets(), SearchContentNodeHandler.class);
 * </code>
 * </pre>
 *
 * or
 *
 * <pre>
 * <code>
 * Idoc idoc = Idoc.parse(..);
 * // pass all widgets via the search handlers to load all their data
 * SearchContentNodeHandler.handle(idoc.getWidgets());
 * </code>
 * </pre>
 *
 * @param <C>
 *            content node handler type
 * @author BBonev
 */
public interface ContentNodeHandler<C extends ContentNode> {

	/**
	 * Test if the given handler can handle the given content node
	 *
	 * @param node
	 *            the node to test
	 * @return <code>true</code> if the current handler could handle the given node
	 */
	boolean accept(ContentNode node);

	/**
	 * Execute the handler against the given content node and produce a result if any
	 *
	 * @param node
	 *            the node to handle
	 * @param context
	 *            the handler context
	 * @return the handler result
	 */
	HandlerResult handle(C node, HandlerContext context);

	/**
	 * Handler context that can be used to provide additional information when calling the handlers
	 *
	 * @author BBonev
	 */
	class HandlerContext extends Context<String, Object> {
		private static final long serialVersionUID = -7438870178281429123L;
		private final String currentInstanceId;

		/**
		 * Instantiate new {@link HandlerContext} instance without setting the current instance id
		 */
		public HandlerContext() {
			this(null);
		}

		/**
		 * Instantiate new {@link HandlerContext} instance and specify the current instance id.
		 *
		 * @param currentInstanceId
		 *            current instance identifier. It's the id of the instance that is inferred when widget
		 *            configuration specifies current instance.
		 */
		public HandlerContext(String currentInstanceId) {
			this.currentInstanceId = currentInstanceId;
		}

		/**
		 * The instance identifier to be used for widgets that need current instance
		 *
		 * @return the currentInstanceId
		 */
		public String getCurrentInstanceId() {
			return currentInstanceId;
		}
	}
	/**
	 * Represents a handler result value. The object also carry the content node that was used to produce the given
	 * result
	 *
	 * @author BBonev
	 */
	class HandlerResult {
		private final Optional<Object> result;
		private final ContentNode node;

		/**
		 * Initialize handler result instance that carries no result
		 *
		 * @param node
		 *            that was used to call the handler
		 */
		public HandlerResult(ContentNode node) {
			this.node = node;
			this.result = Optional.empty();
		}

		/**
		 * Instantiate a handler result using the trigger node and the result value.
		 *
		 * @param node
		 *            that was used to call the handler
		 * @param result
		 *            the of the handler if any
		 */
		public HandlerResult(ContentNode node, Object result) {
			this.node = node;
			this.result = Optional.ofNullable(result);
		}

		/**
		 * Retrieve the actual handler result value. The method result is not safe.
		 *
		 * @param <R>
		 *            the result type
		 * @return an optional that contains the actual result or empty optional
		 */
		@SuppressWarnings("unchecked")
		public <R> Optional<R> getResult() {
			return (Optional<R>) result;
		}

		/**
		 * The original content node
		 *
		 * @return the node instance.
		 */
		public ContentNode getNode() {
			return node;
		}
	}
}
