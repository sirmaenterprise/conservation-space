package com.sirma.sep.content.idoc.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.Test;

import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.handler.ContentNodeHandler;
import com.sirma.sep.content.idoc.handler.Handlers;
import com.sirma.sep.content.idoc.handler.SearchContentNodeHandler;
import com.sirma.sep.content.idoc.handler.ContentNodeHandler.HandlerContext;
import com.sirma.sep.content.idoc.handler.ContentNodeHandler.HandlerResult;

/**
 * Test for {@link Handlers}
 *
 * @author BBonev
 */
public class HandlersTest {

	@Test
	public void handleShouldCallOnlyHandlersOfTheGivenType() throws Exception {
		Function<ContentNode, HandlerResult> function = mock(Function.class);
		Handlers.getInstance().registerHandler(new Handler(node -> true, function));
		Handlers.getInstance().registerHandler(new SearchHandler1(node -> true, function));

		Handlers.getInstance().handle(Arrays.asList(mockNode("1")).stream(), SearchHandler1.class);
		verify(function).apply(any(ContentNode.class));
	}

	@Test
	public void handleWithResultShouldCallOneHandlerPerNodeAndReturnResult() throws Exception {
		Function<ContentNode, HandlerResult> function = mock(Function.class);
		when(function.apply(any())).then(a -> new HandlerResult(a.getArgumentAt(0, ContentNode.class), new Object()));
		Handlers.getInstance().registerHandler(new Handler(node -> true, function));
		Handlers.getInstance().registerHandler(new SearchHandler2(node -> true, function));

		List<HandlerResult> list = Handlers
				.getInstance()
					.handleWithResult(Arrays.asList(mockNode("1"), mockNode("2")).stream(), SearchHandler2.class)
					.collect(Collectors.toList());
		verify(function, times(2)).apply(any(ContentNode.class));
		assertEquals(2, list.size());
		assertTrue(list.get(0).getResult().isPresent());
		assertTrue(list.get(1).getResult().isPresent());
	}

	@Test
	public void handleWithResultShouldReturnNoValueHandlerIfNoHandlerIsMatched() throws Exception {
		Function<ContentNode, HandlerResult> function1 = mock(Function.class);
		Function<ContentNode, HandlerResult> function2 = mock(Function.class);
		when(function1.apply(any())).then(a -> new HandlerResult(a.getArgumentAt(0, ContentNode.class), new Object()));
		when(function2.apply(any())).then(a -> new HandlerResult(a.getArgumentAt(0, ContentNode.class), new Object()));
		Handlers.getInstance().registerHandler(new Handler(node -> true, function1));
		Handlers.getInstance().registerHandler(new SearchHandler3(node -> false, function2));

		List<HandlerResult> list = Handlers
				.getInstance()
					.handleWithResult(Arrays.asList(mockNode("1"), mockNode("2")).stream(), SearchHandler3.class)
					.collect(Collectors.toList());
		verify(function1, never()).apply(any(ContentNode.class));
		verify(function2, never()).apply(any(ContentNode.class));
		assertEquals(2, list.size());
		assertFalse(list.get(0).getResult().isPresent());
		assertFalse(list.get(1).getResult().isPresent());
	}

	@Test
	public void searchHandleShouldMapContentNodesAndResults() throws Exception {
		Function<ContentNode, HandlerResult> function1 = mock(Function.class);
		Function<ContentNode, HandlerResult> function2 = mock(Function.class);
		when(function1.apply(any())).then(a -> new HandlerResult(a.getArgumentAt(0, ContentNode.class)));
		when(function2.apply(any())).then(a -> new HandlerResult(a.getArgumentAt(0, ContentNode.class), new Object()));
		Handlers.getInstance().registerHandler(new Handler(node -> true, function1));
		Handlers.getInstance().registerHandler(new SearchHandler4(node -> true, function2));

		Map<String, HandlerResult> map = SearchContentNodeHandler
				.handle(Arrays.asList(mockNode("1"), mockNode("2")).stream(), new HandlerContext());

		verify(function1, never()).apply(any(ContentNode.class));
		verify(function2, times(2)).apply(any(ContentNode.class));
		assertEquals(2, map.size());
		Iterator<HandlerResult> it = map.values().iterator();
		assertTrue(it.next().getResult().isPresent());
		assertTrue(it.next().getResult().isPresent());
	}

	private static ContentNode mockNode(String id) {
		ContentNode node = mock(ContentNode.class);
		when(node.getId()).thenReturn(id);
		return node;
	}

	private static class Handler implements ContentNodeHandler<ContentNode> {

		private Predicate<ContentNode> accept;
		private Function<ContentNode, HandlerResult> handle;

		protected Handler(Predicate<ContentNode> accept, Function<ContentNode, HandlerResult> handle) {
			this.accept = accept;
			this.handle = handle;
		}

		@Override
		public boolean accept(ContentNode node) {
			return accept.test(node);
		}

		@Override
		public HandlerResult handle(ContentNode node, HandlerContext context) {
			return handle.apply(node);
		}
	}

	// to properly test the functionality we need different handlers for the tests, because Handlers is Singleton

	private static class SearchHandler1 extends Handler implements SearchContentNodeHandler<ContentNode> {
		protected SearchHandler1(Predicate<ContentNode> accept, Function<ContentNode, HandlerResult> handle) {
			super(accept, handle);
		}
	}

	private static class SearchHandler2 extends Handler implements SearchContentNodeHandler<ContentNode> {
		protected SearchHandler2(Predicate<ContentNode> accept, Function<ContentNode, HandlerResult> handle) {
			super(accept, handle);
		}
	}

	private static class SearchHandler3 extends Handler implements SearchContentNodeHandler<ContentNode> {
		protected SearchHandler3(Predicate<ContentNode> accept, Function<ContentNode, HandlerResult> handle) {
			super(accept, handle);
		}
	}

	private static class SearchHandler4 extends Handler implements SearchContentNodeHandler<ContentNode> {
		protected SearchHandler4(Predicate<ContentNode> accept, Function<ContentNode, HandlerResult> handle) {
			super(accept, handle);
		}
	}
}
